package com.lts.web.support.spring;

import com.lts.core.commons.utils.JSONUtils;
import com.lts.web.support.AjaxUtils;
import com.lts.web.vo.RestfulResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
@Component
public class ExceptionHandlerResolver implements HandlerExceptionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger("[LTS-Admin]");

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        // ajax
        if (AjaxUtils.isAjaxRequest(request)) {
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                RestfulResponse restfulResponse = new RestfulResponse();
                restfulResponse.setSuccess(false);
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                restfulResponse.setMsg(sw.toString());
                writer.write(JSONUtils.toJSONString(restfulResponse));
                writer.flush();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.error(ex.getMessage(), ex);
//            request.setAttribute("message", ex.getMessage());
//            return new ModelAndView("common/error");
        }
        return null;
    }
}

