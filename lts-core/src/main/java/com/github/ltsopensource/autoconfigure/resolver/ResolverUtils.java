package com.github.ltsopensource.autoconfigure.resolver;

import com.github.ltsopensource.core.commons.utils.PrimitiveTypeUtils;

import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 4/20/16.
 */
public class ResolverUtils {

    public static Resolver getResolver(Class<?> clazz) {

        if (clazz == Class.class) {

            return ClassResolver.INSTANCE;

        } else if (PrimitiveTypeUtils.isPrimitiveClass(clazz)) {

            return PrimitiveTypeResolver.INSTANCE;

        } else if (clazz.isEnum()) {

            return EnumResolver.INSTANCE;

        } else if (clazz.isArray()) {

            return ArrayResolver.INSTANCE;

        } else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class
                || clazz == ArrayList.class) {

            return CollectionResolver.INSTANCE;

        } else if (Collection.class.isAssignableFrom(clazz)) {

            return CollectionResolver.INSTANCE;

        } else if (Map.class.isAssignableFrom(clazz)) {

            return MapResolver.INSTANCE;

        } else {
            return JavaBeanResolver.INSTANCE;
        }
    }

}
