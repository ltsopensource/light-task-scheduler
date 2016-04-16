package com.github.ltsopensource.spring.boot.annotation;

import com.github.ltsopensource.core.cluster.NodeType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MasterNodeListener {

    NodeType[] nodeTypes() default {};

}
