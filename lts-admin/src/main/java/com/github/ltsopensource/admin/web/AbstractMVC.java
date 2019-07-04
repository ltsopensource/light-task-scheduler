package com.github.ltsopensource.admin.web;

import com.github.ltsopensource.admin.web.support.DateEditor;
import com.github.ltsopensource.admin.web.support.MapEditor;
import java.util.Date;
import java.util.Map;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author Robert HG (254963746@qq.com) on 5/9/15.
 */
public class AbstractMVC {

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws Exception {
        //For attributes that need to be converted to Date type for conversion to Date type, use DateEditor to process attributes and use DateEditor for processing.
        binder.registerCustomEditor(Date.class, new DateEditor());
        binder.registerCustomEditor(Map.class, "extParams", new MapEditor());
    }

}
