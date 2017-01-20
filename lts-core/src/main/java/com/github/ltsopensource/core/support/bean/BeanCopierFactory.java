package com.github.ltsopensource.core.support.bean;

import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.BeanUtils;
import com.github.ltsopensource.core.commons.utils.ClassHelper;
import com.github.ltsopensource.core.commons.utils.ReflectionUtils;
import com.github.ltsopensource.core.compiler.AbstractCompiler;
import com.github.ltsopensource.core.compiler.Compiler;
import com.github.ltsopensource.core.exception.LtsRuntimeException;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 *         为了兼容javaassist 去掉泛型
 */
public final class BeanCopierFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCopierFactory.class);

    private static Compiler COMPILER = AbstractCompiler.getCompiler();
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final ConcurrentMap<Integer, Map<String, PropConverter<?, ?>>> SEQ_PROP_CVT_MAP = new ConcurrentHashMap<Integer, Map<String, PropConverter<?, ?>>>();

    public static <Source, Target> BeanCopier<Source, Target> createCopier(
            Class<?> sourceClass, Class<?> targetClass) {
        return createCopier(sourceClass, targetClass, false, null);
    }

    public static <Source, Target> BeanCopier<Source, Target> createCopier(
            Class<?> sourceClass, Class<?> targetClass, boolean deepCopy) {
        return createCopier(sourceClass, targetClass, deepCopy, null);
    }

    public static <Source, Target> BeanCopier<Source, Target> createCopier(
            Class<?> sourceClass, Class<?> targetClass, Map<String, PropConverter<?, ?>> propCvtMap) {
        return createCopier(sourceClass, targetClass, false, propCvtMap);
    }

    @SuppressWarnings("unchecked")
    public static <Source, Target> BeanCopier<Source, Target> createCopier(
            Class<?> sourceClass, Class<?> targetClass,
            boolean deepCopy, Map<String, PropConverter<?, ?>> propCvtMap
    ) {

        Assert.notNull(sourceClass, "sourceClass can't be null");
        Assert.notNull(targetClass, "targetClass can't be null");

        Integer sequence = SEQ.incrementAndGet();
        if (propCvtMap != null) {
            SEQ_PROP_CVT_MAP.putIfAbsent(sequence, propCvtMap);
        }

        try {
            Class<?> beanCopierClazz = COMPILER.compile(getClassCode(sequence, sourceClass, targetClass, deepCopy, propCvtMap));
            final BeanCopierAdapter beanCopier = (BeanCopierAdapter) beanCopierClazz.newInstance();
            return new BeanCopier<Source, Target>() {
                @Override
                public void copyProps(Source source, Target target) {
                    beanCopier.copyProps(source, target);
                }
            };
        } catch (Exception e) {
            throw new LtsRuntimeException("Generate BeanCopier error, sourceClass=" + sourceClass.getName() + ", targetClass=" + targetClass.getName(), e);
        }
    }

    /**
     * 生成BeanCopier的实现类源码
     */
    private static String getClassCode(Integer sequence, Class<?> sourceClass, Class<?> targetClass, boolean deepCopy, final Map<String, PropConverter<?, ?>> propCvtMap) throws Exception {

        JavaSourceBean javaSourceBean = new JavaSourceBean();
        javaSourceBean.setPackageName(BeanCopierFactory.class.getPackage().getName());

        javaSourceBean.addImport(BeanCopierAdapter.class.getName());
        javaSourceBean.addImport(sourceClass.getName());
        javaSourceBean.addImport(targetClass.getName());
        javaSourceBean.addImport(PropConverter.class.getName());

        String beanCopierClassName = sourceClass.getSimpleName() + "2" + targetClass.getSimpleName() + BeanCopier.class.getSimpleName() + sequence;
        String classDefinitionCode = "public class " + beanCopierClassName +
                " extends " + BeanCopierAdapter.class.getName();

        javaSourceBean.setClassDefinition(classDefinitionCode);

        javaSourceBean.addMethod(getMethodImplCode(sequence, sourceClass, targetClass, deepCopy, propCvtMap));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(javaSourceBean.toString());
        }
        return javaSourceBean.toString();
    }


    /**
     * 构建方法体实现
     */
    private static String getMethodImplCode(Integer sequence, Class<?> sourceClass, Class<?> targetClass, boolean deepCopy, final Map<String, PropConverter<?, ?>> propCvtMap) throws Exception {

        StringBuilder methodCode = new StringBuilder();
        methodCode.append("public void copyProps(").append(Object.class.getName()).append(" sourceObj, ").append(Object.class.getName()).append(" targetObj){\n");
        methodCode.append(sourceClass.getName()).append(" source = ").append("(").append(sourceClass.getName()).append(")sourceObj;\n");
        methodCode.append(targetClass.getName()).append(" target = ").append("(").append(targetClass.getName()).append(")targetObj;\n");

        // 这里查找了包括父类的属性
        Field[] targetFields = ReflectionUtils.findFields(targetClass);

        for (Field field : targetFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                // 是否含有set方法
                String methodNameSuffix = capitalize(field.getName());
                Class<?> targetFieldClass = field.getType();

                Method setMethod = ReflectionUtils.findMethod(targetClass, "set" + methodNameSuffix, targetFieldClass);
                if (setMethod == null) {
                    setMethod = ReflectionUtils.findMethod(targetClass, "set" + field.getName(), targetFieldClass);
                    if (setMethod != null) {
                        methodNameSuffix = field.getName();
                    }
                }
                if (setMethod != null) {

                    // 查看这个属性是否有 PropConverter
                    if (propCvtMap != null && propCvtMap.containsKey(field.getName())) {

                        String converterName = field.getName() + "Converter";
                        String converterType = PropConverter.class.getName();

                        methodCode.append(converterType).append(" ").append(converterName).append(" = (").append(converterType).append(")")
                                .append(BeanCopierFactory.class.getName()).append(".getConverter(").append(sequence).append(",").append("\"").append(field.getName()).append("\");\n");
                        methodCode.append("target.").append(setMethod.getName()).append("(")
                                .append("(").append(targetFieldClass.getName()).append(")").append(converterName).append(".convert(").append("source").append(")")
                                .append(");\n");
                        continue;
                    }

                    // set方法存在,看是否 sourceClass 有get方法或者is方法
                    Method getMethod = ReflectionUtils.findMethod(sourceClass, "get" + methodNameSuffix);
                    if (getMethod == null && (targetFieldClass == boolean.class || targetFieldClass == Boolean.class)) {
                        getMethod = ReflectionUtils.findMethod(sourceClass, "is" + methodNameSuffix);
                    }
                    if (getMethod == null) {
                        continue;
                    }
                    // 查看返回值是否相同,不完全相同是否可以转换
                    if (getMethod.getReturnType() == targetFieldClass) {
                        if (!deepCopy) {
                            methodCode.append("target.").append(setMethod.getName()).append("(").append("source.").append(getMethod.getName()).append("()").append(");\n");
                        } else {
                            if (ClassHelper.isPrimitiveType(targetFieldClass) || ClassHelper.isPrimitiveWrapperType(targetFieldClass)
                                    || targetFieldClass == String.class) {
                                methodCode.append("target.").append(setMethod.getName()).append("(").append("source.").append(getMethod.getName()).append("()").append(");\n");
                            } else {
                                // 深度复制,对于非基本类型的采用流的方式拷贝
                                methodCode.append("target.").append(setMethod.getName()).append("(")
                                        .append("(").append(targetFieldClass.getName()).append(")")
                                        .append(BeanUtils.class.getName()).append(".deepClone(")
                                        .append("source.").append(getMethod.getName()).append("()")
                                        .append(")")
                                        .append(");\n");
                            }
                        }
                    } else if (ClassHelper.isPrimitiveType(targetFieldClass) && ClassHelper.getPrimitiveTypeByWrapper(getMethod.getReturnType()) == targetFieldClass) {
                        // 类似 target.setInt(source.getInt() == null ? 0 : source.getInt());
                        methodCode.append("target.").append(setMethod.getName()).append("(");
                        methodCode.append("source.").append(getMethod.getName()).append("() == null ? ").append(String.valueOf(ClassHelper.getPrimitiveDftValue(targetFieldClass))).append(" : ").append("source.").append(getMethod.getName()).append("()");
                        methodCode.append(");\n");
                    } else if (ClassHelper.isPrimitiveWrapperType(targetFieldClass) && ClassHelper.getWrapperTypeByPrimitive(getMethod.getReturnType()) == targetFieldClass) {
                        methodCode.append("target.").append(setMethod.getName()).append("(").append("source.").append(getMethod.getName()).append("()").append(");\n");
                    }
                }
            }
        }

        methodCode.append("}");
        return methodCode.toString();
    }

    private static String capitalize(String str) {
        return String.valueOf(Character.toTitleCase(str.charAt(0))) + str.substring(1);
    }

    /**
     * 会被动态类使用
     */
    public static PropConverter<?, ?> getConverter(int sequence, String propName) {
        Map<String, PropConverter<?, ?>> map = SEQ_PROP_CVT_MAP.get(sequence);
        return map.get(propName);
    }
}
