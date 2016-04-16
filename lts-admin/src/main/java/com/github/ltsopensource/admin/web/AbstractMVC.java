package com.github.ltsopensource.admin.web;

import com.github.ltsopensource.admin.web.support.DateEditor;
import com.github.ltsopensource.admin.web.support.MapEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Date;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 5/9/15.
 */
public class AbstractMVC {

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws Exception {
        //对于需要转换为Date类型的属性，使用DateEditor进行处理
        binder.registerCustomEditor(Date.class, new DateEditor());
        binder.registerCustomEditor(Map.class, "extParams", new MapEditor());
    }

}
