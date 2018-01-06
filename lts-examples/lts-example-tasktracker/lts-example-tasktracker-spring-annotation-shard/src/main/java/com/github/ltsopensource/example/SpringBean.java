package com.github.ltsopensource.example;

import org.springframework.stereotype.Component;

/**
 * 测试bean 注入
 * @author Robert HG (254963746@qq.com) on 8/4/15.
 */
@Component
public class SpringBean {

    public void hello(){
        System.out.println("我是SpringBean，我执行了");
    }
}
