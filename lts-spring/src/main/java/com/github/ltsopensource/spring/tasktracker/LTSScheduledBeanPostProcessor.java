package com.github.ltsopensource.spring.tasktracker;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.QuietUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.core.properties.TaskTrackerProperties;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.domain.Response;
import com.github.ltsopensource.spring.quartz.QuartzLTSConfig.JobProperties;
import com.github.ltsopensource.spring.tasktracker.JobRunnerBuilder;
import com.github.ltsopensource.spring.tasktracker.JobRunnerHolder;

public class LTSScheduledBeanPostProcessor implements BeanPostProcessor, EmbeddedValueResolverAware
, ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LTSScheduledBeanPostProcessor.class);
	private StringValueResolver embeddedValueResolver;
	private final Map<Class<?>, Boolean> nonAnnotatedClasses = new ConcurrentHashMap<Class<?>, Boolean>(64);
	private JobClientProperties jobClientProperties;
	private TaskTrackerProperties taskTrackerProperties;
	private JobProperties jobProperties;
	private List<Job> jobs = new ArrayList<Job>();
	private AtomicBoolean jobSubmitted = new AtomicBoolean(false);
	
	public LTSScheduledBeanPostProcessor(){};
	
	public LTSScheduledBeanPostProcessor(JobClientProperties jobClientProperties
			, JobProperties jobProperties, TaskTrackerProperties taskTrackerProperties){
        this.jobClientProperties = jobClientProperties;
        jobClientProperties.checkProperties();
        
        this.taskTrackerProperties = taskTrackerProperties;
        taskTrackerProperties.checkProperties();
        
        this.jobProperties = jobProperties;
        jobProperties.checkProperties();
	}
	
	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (!this.nonAnnotatedClasses.containsKey(targetClass)) {
			final Set<Method> annotatedMethods = new LinkedHashSet<Method>(1);
			ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					LTSScheduled scheduled = AnnotationUtils.getAnnotation(method, LTSScheduled.class);
					if (scheduled == null) {
						return;
					}
					JobRunnerHolder.add(scheduled.value()
						, JobRunnerBuilder.build(bean, method, method.getParameterTypes()));
					processLTSScheduled(scheduled, method, bean);
					annotatedMethods.add(method);
				}
			});
			if (annotatedMethods.isEmpty()) {
				this.nonAnnotatedClasses.put(targetClass, Boolean.TRUE);
			}
		}
		return bean;
	}
	
	private void processLTSScheduled(LTSScheduled scheduled, Method method, Object bean) {
		Assert.isTrue(void.class.equals(method.getReturnType()),
				"Only void-returning methods may be annotated with @Scheduled");
		Assert.isTrue(method.getParameterTypes().length == 0,
				"Only no-arg methods may be annotated with @Scheduled");
		
		if (AopUtils.isJdkDynamicProxy(bean)) {
			try {
				// Found a @Scheduled method on the target class for this JDK proxy ->
				// is it also present on the proxy itself?
				method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
			}
			catch (SecurityException ex) {
				ReflectionUtils.handleReflectionException(ex);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalStateException(String.format(
						"@Scheduled method '%s' found on bean target class '%s' but not " +
						"found in any interface(s) for a dynamic proxy. Either pull the " +
						"method up to a declared interface or switch to subclass (CGLIB) " +
						"proxies by setting proxy-target-class/proxyTargetClass to 'true'",
						method.getName(), method.getDeclaringClass().getSimpleName()));
			}
		}
		else if (AopUtils.isCglibProxy(bean)) {
			// Common problem: private methods end up in the proxy instance, not getting delegated.
			if (Modifier.isPrivate(method.getModifiers())) {
				LOGGER.warn(String.format(
						"@Scheduled method '%s' found on CGLIB proxy for target class '%s' but cannot " +
						"be delegated to target bean. Switch its visibility to package or protected.",
						method.getName(), method.getDeclaringClass().getSimpleName()));
			}
		}
		boolean processedSchedule = false;
		String errorMessage =
				"Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";

		// Determine initial delay
		long initialDelay = scheduled.initialDelay();
		String initialDelayString = scheduled.initialDelayString();
		if (StringUtils.hasText(initialDelayString)) {
			Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both");
			if (this.embeddedValueResolver != null) {
				initialDelayString = this.embeddedValueResolver.resolveStringValue(initialDelayString);
			}
			try {
				initialDelay = Integer.parseInt(initialDelayString);
			}
			catch (NumberFormatException ex) {
				throw new IllegalArgumentException(
						"Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into integer");
			}
		}

		// Check cron expression
		String cron = scheduled.cron();
		if (StringUtils.hasText(cron)) {
			Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
			processedSchedule = true;
			if (this.embeddedValueResolver != null) {
				cron = this.embeddedValueResolver.resolveStringValue(cron);
			}
			Job job = this.buildJob(scheduled, method, bean);
			job.setCronExpression(cron);
			jobs.add(job);
		}
		// At this point we don't need to differentiate between initial delay set or not anymore
		if (initialDelay < 0) {
			initialDelay = 0;
		}

		// Check fixed delay
		long fixedDelay = scheduled.fixedDelay();
		if (fixedDelay >= 0) {
			Assert.isTrue(!processedSchedule, errorMessage);
			processedSchedule = true;
			Job job = this.buildJob(scheduled, method, bean);
			job.setRepeatInterval(fixedDelay);
			jobs.add(job);
		}
		String fixedDelayString = scheduled.fixedDelayString();
		if (StringUtils.hasText(fixedDelayString)) {
			Assert.isTrue(!processedSchedule, errorMessage);
			processedSchedule = true;
			if (this.embeddedValueResolver != null) {
				fixedDelayString = this.embeddedValueResolver.resolveStringValue(fixedDelayString);
			}
			try {
				fixedDelay = Integer.parseInt(fixedDelayString);
			}
			catch (NumberFormatException ex) {
				throw new IllegalArgumentException(
						"Invalid fixedDelayString value \"" + fixedDelayString + "\" - cannot parse into integer");
			}
			Job job = this.buildJob(scheduled, method, bean);
			job.setRepeatInterval(fixedDelay);
			jobs.add(job);
		}

		// Check fixed rate
		long fixedRate = scheduled.fixedRate();
		if (fixedRate >= 0) {
			Assert.isTrue(!processedSchedule, errorMessage);
			processedSchedule = true;
			Job job = this.buildJob(scheduled, method, bean);
			job.setRepeatInterval(fixedRate);
			job.setRelyOnPrevCycle(false);
			jobs.add(job);
		}
		String fixedRateString = scheduled.fixedRateString();
		if (StringUtils.hasText(fixedRateString)) {
			Assert.isTrue(!processedSchedule, errorMessage);
			processedSchedule = true;
			if (this.embeddedValueResolver != null) {
				fixedRateString = this.embeddedValueResolver.resolveStringValue(fixedRateString);
			}
			try {
				fixedRate = Integer.parseInt(fixedRateString);
			}
			catch (NumberFormatException ex) {
				throw new IllegalArgumentException(
						"Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into integer");
			}
			Job job = this.buildJob(scheduled, method, bean);
			job.setRepeatInterval(fixedRate);
			job.setRelyOnPrevCycle(false);
			jobs.add(job);
		}

		// Check whether we had any attribute set
		Assert.isTrue(processedSchedule, errorMessage);
		
	}
	
	private Job buildJob(LTSScheduled scheduled, Method method, Object bean){
        Job job = new Job();
        job.setTaskId(scheduled.value());
        job.setRepeatCount(-1);
        job.setSubmitNodeGroup(jobClientProperties.getNodeGroup());
        job.setTaskTrackerNodeGroup(taskTrackerProperties.getNodeGroup());
        job.setParam("description", bean.getClass().getName() + "#" + method.getName());
        setJobProp(job);
        return job;
	}
	
	private void setJobProp(Job job) {
		if (jobProperties == null) {
			return;
		}
		if (jobProperties.getMaxRetryTimes() != null) {
			job.setMaxRetryTimes(jobProperties.getMaxRetryTimes());
		}
		if (jobProperties.getNeedFeedback() != null) {
			job.setNeedFeedback(jobProperties.getNeedFeedback());
		}
		if (jobProperties.getRelyOnPrevCycle() != null) {
			job.setRelyOnPrevCycle(jobProperties.getRelyOnPrevCycle());
		}
		if (jobProperties.getReplaceOnExist() != null) {
			job.setReplaceOnExist(jobProperties.getReplaceOnExist());
		}
	}
	
	private void submitJobs(JobClient<?,?> jobClient, List<Job> jobs) {
		List<Job> failedJobs = null;
		try {
			Response response = jobClient.submitJob(jobs);
			if (!response.isSuccess()) {
				LOGGER.warn("Submit Quartz Jobs to LTS failed: {}", JSON.toJSONString(response));
				failedJobs = response.getFailedJobs();
			}
		} catch (Throwable e) {
			LOGGER.warn("Submit Quartz Jobs to LTS error", e);
			failedJobs = jobs;
		}

		if (CollectionUtils.isNotEmpty(failedJobs)) {
			// 没提交成功要重试 3S 之后重试
			LOGGER.info("=============LTS=========== Sleep 3 Seconds and retry");
			QuietUtils.sleep(3000);
			submitJobs(jobClient, failedJobs);
			return;
		}

		// 如果成功了, 关闭jobClient 
//		jobClient.stop();//提交完不关闭，随系统关闭，其他地方可以继续使用
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//Spring Cloud/Apollo可能在Boot阶段多次refresh
		if(!jobs.isEmpty() && !jobSubmitted.compareAndSet(false, true)){
			return;
		}
		JobClient<?,?> jobClient = event.getApplicationContext().getBean(JobClient.class);
		Assert.notNull(jobClient, "JobClient must be defined");
		submitJobs(jobClient, jobs);
	}


	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	public JobClientProperties getJobClientProperties() {
		return jobClientProperties;
	}

	public void setJobClientProperties(JobClientProperties jobClientProperties) {
		this.jobClientProperties = jobClientProperties;
	}

	public TaskTrackerProperties getTaskTrackerProperties() {
		return taskTrackerProperties;
	}

	public void setTaskTrackerProperties(TaskTrackerProperties taskTrackerProperties) {
		this.taskTrackerProperties = taskTrackerProperties;
	}

	public JobProperties getJobProperties() {
		return jobProperties;
	}

	public void setJobProperties(JobProperties jobProperties) {
		this.jobProperties = jobProperties;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
}
