package com.github.ltsopensource.core.compiler;

import com.github.ltsopensource.core.commons.utils.ClassHelper;
import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author william.liangf
 */
public abstract class AbstractCompiler implements Compiler {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

    public Class<?> compile(String code) {
        code = code.trim();
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("No such class name in " + code);
        }
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        try {
            return Class.forName(className, true, ClassHelper.getCallerClassLoader(getClass()));
        } catch (ClassNotFoundException e) {
            if (!code.endsWith("}")) {
                throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
            }
            try {
                return doCompile(className, code);
            } catch (RuntimeException t) {
                throw t;
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", code: \n" + code + "\n, stack: " + StringUtils.toString(t));
            }
        }
    }

    protected abstract Class<?> doCompile(String name, String source) throws Throwable;

    public static void main(String[] args) {
        String code = "package com.github.ltsopensource.core.support.bean;\n" +
                "import com.github.ltsopensource.core.support.bean.BeanCopier;\n" +
                "import com.github.ltsopensource.queue.domain.JobPo;\n" +
                "public class JobPo2JobPoBeanCopier1 implements BeanCopier<JobPo,JobPo> {\n" +
                "public void copyProps(JobPo source, JobPo target){\n" +
                "target.setJobId(source.getJobId());\n" +
                "target.setJobType(source.getJobType());\n" +
                "target.setPriority(source.getPriority());\n" +
                "target.setTaskId(source.getTaskId());\n" +
                "target.setRealTaskId(source.getRealTaskId());\n" +
                "target.setGmtCreated(source.getGmtCreated());\n" +
                "target.setGmtModified(source.getGmtModified());\n" +
                "target.setSubmitNodeGroup(source.getSubmitNodeGroup());\n" +
                "target.setTaskTrackerNodeGroup(source.getTaskTrackerNodeGroup());\n" +
                "com.github.ltsopensource.core.support.bean.PropConverter<JobPo, java.util.Map<java.lang.String, java.lang.String>> extParamsConverter = (com.github.ltsopensource.core.support.bean.PropConverter<JobPo, java.util.Map<java.lang.String, java.lang.String>> )com.github.ltsopensource.core.support.bean.BeanCopierFactory.getConverter(1,\"extParams\");\n" +
                "target.setExtParams((java.util.Map<java.lang.String, java.lang.String>)extParamsConverter.convert(source));\n" +
                "com.github.ltsopensource.core.support.bean.PropConverter<JobPo, java.util.Map<java.lang.String, java.lang.String>> internalExtParamsConverter = (com.github.ltsopensource.core.support.bean.PropConverter<JobPo, java.util.Map<java.lang.String, java.lang.String>> )com.github.ltsopensource.core.support.bean.BeanCopierFactory.getConverter(1,\"internalExtParams\");\n" +
                "target.setInternalExtParams((java.util.Map<java.lang.String, java.lang.String>)internalExtParamsConverter.convert(source));\n" +
                "target.setTaskTrackerIdentity(source.getTaskTrackerIdentity());\n" +
                "target.setNeedFeedback(source.isNeedFeedback());\n" +
                "target.setCronExpression(source.getCronExpression());\n" +
                "target.setTriggerTime(source.getTriggerTime());\n" +
                "target.setRetryTimes(source.getRetryTimes());\n" +
                "target.setMaxRetryTimes(source.getMaxRetryTimes());\n" +
                "target.setRepeatCount(source.getRepeatCount());\n" +
                "target.setRepeatedCount(source.getRepeatedCount());\n" +
                "target.setRepeatInterval(source.getRepeatInterval());\n" +
                "target.setRelyOnPrevCycle(source.getRelyOnPrevCycle());\n" +
                "target.setLastGenerateTriggerTime(source.getLastGenerateTriggerTime());\n" +
                "}}";

        Compiler compiler = new JavassistCompiler();
        compiler.compile(code);

    }
}

