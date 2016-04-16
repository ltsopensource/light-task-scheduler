package com.github.ltsopensource.spring.quartz.invoke;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/16.
 */
public class JobExecutionContextImpl implements JobExecutionContext {

    private JobDataMap jobDataMap;

    public JobExecutionContextImpl(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    @Override
    public Scheduler getScheduler() {
        return new Scheduler() {
            @Override
            public String getSchedulerName() throws SchedulerException {
                return null;
            }

            @Override
            public String getSchedulerInstanceId() throws SchedulerException {
                return null;
            }

            @Override
            public SchedulerContext getContext() throws SchedulerException {
                return new SchedulerContext();
            }

            @Override
            public void start() throws SchedulerException {

            }

            @Override
            public void startDelayed(int seconds) throws SchedulerException {

            }

            @Override
            public boolean isStarted() throws SchedulerException {
                return false;
            }

            @Override
            public void standby() throws SchedulerException {

            }

            @Override
            public boolean isInStandbyMode() throws SchedulerException {
                return false;
            }

            @Override
            public void shutdown() throws SchedulerException {

            }

            @Override
            public void shutdown(boolean waitForJobsToComplete) throws SchedulerException {

            }

            @Override
            public boolean isShutdown() throws SchedulerException {
                return false;
            }

            @Override
            public SchedulerMetaData getMetaData() throws SchedulerException {
                return null;
            }

            @Override
            public List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException {
                return null;
            }

            @Override
            public void setJobFactory(JobFactory factory) throws SchedulerException {

            }

            @Override
            public ListenerManager getListenerManager() throws SchedulerException {
                return null;
            }

            @Override
            public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
                return null;
            }

            @Override
            public Date scheduleJob(Trigger trigger) throws SchedulerException {
                return null;
            }

            @Override
            public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException {

            }

            @Override
            public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException {

            }

            @Override
            public boolean unscheduleJob(TriggerKey triggerKey) throws SchedulerException {
                return false;
            }

            @Override
            public boolean unscheduleJobs(List<TriggerKey> triggerKeys) throws SchedulerException {
                return false;
            }

            @Override
            public Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) throws SchedulerException {
                return null;
            }

            @Override
            public void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException {

            }

            @Override
            public void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling) throws SchedulerException {

            }

            @Override
            public boolean deleteJob(JobKey jobKey) throws SchedulerException {
                return false;
            }

            @Override
            public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException {
                return false;
            }

            @Override
            public void triggerJob(JobKey jobKey) throws SchedulerException {

            }

            @Override
            public void triggerJob(JobKey jobKey, JobDataMap data) throws SchedulerException {

            }

            @Override
            public void pauseJob(JobKey jobKey) throws SchedulerException {

            }

            @Override
            public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {

            }

            @Override
            public void pauseTrigger(TriggerKey triggerKey) throws SchedulerException {

            }

            @Override
            public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {

            }

            @Override
            public void resumeJob(JobKey jobKey) throws SchedulerException {

            }

            @Override
            public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {

            }

            @Override
            public void resumeTrigger(TriggerKey triggerKey) throws SchedulerException {

            }

            @Override
            public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {

            }

            @Override
            public void pauseAll() throws SchedulerException {

            }

            @Override
            public void resumeAll() throws SchedulerException {

            }

            @Override
            public List<String> getJobGroupNames() throws SchedulerException {
                return null;
            }

            @Override
            public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
                return null;
            }

            @Override
            public List<? extends Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException {
                return null;
            }

            @Override
            public List<String> getTriggerGroupNames() throws SchedulerException {
                return null;
            }

            @Override
            public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
                return null;
            }

            @Override
            public Set<String> getPausedTriggerGroups() throws SchedulerException {
                return null;
            }

            @Override
            public JobDetail getJobDetail(JobKey jobKey) throws SchedulerException {
                return null;
            }

            @Override
            public Trigger getTrigger(TriggerKey triggerKey) throws SchedulerException {
                return null;
            }

            @Override
            public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws SchedulerException {
                return null;
            }

            @Override
            public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException {

            }

            @Override
            public boolean deleteCalendar(String calName) throws SchedulerException {
                return false;
            }

            @Override
            public Calendar getCalendar(String calName) throws SchedulerException {
                return null;
            }

            @Override
            public List<String> getCalendarNames() throws SchedulerException {
                return null;
            }

            @Override
            public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
                return false;
            }

            @Override
            public boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException {
                return false;
            }

            @Override
            public boolean checkExists(JobKey jobKey) throws SchedulerException {
                return false;
            }

            @Override
            public boolean checkExists(TriggerKey triggerKey) throws SchedulerException {
                return false;
            }

            @Override
            public void clear() throws SchedulerException {

            }
        };
    }

    @Override
    public Trigger getTrigger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getCalendar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRecovering() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRefireCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JobDataMap getMergedJobDataMap() {
        return jobDataMap;
    }

    @Override
    public JobDetail getJobDetail() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Job getJobInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getFireTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getScheduledFireTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getPreviousFireTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextFireTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFireInstanceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResult(Object result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJobRunTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        throw new UnsupportedOperationException();
    }
}
