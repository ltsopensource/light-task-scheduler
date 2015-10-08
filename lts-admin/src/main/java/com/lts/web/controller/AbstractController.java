package com.lts.web.controller;

import com.lts.web.support.spring.DateEditor;
import com.lts.web.support.spring.MapEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 5/9/15.
 */
public class AbstractController {

    @InitBinder
    protected void initBinder(HttpServletRequest request,
                              ServletRequestDataBinder binder) throws Exception {
        //对于需要转换为Date类型的属性，使用DateEditor进行处理
        binder.registerCustomEditor(Date.class, new DateEditor());
        binder.registerCustomEditor(Map.class, "extParams", new MapEditor());
    }

}
