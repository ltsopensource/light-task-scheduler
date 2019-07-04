package com.github.ltsopensource.admin.support;

import java.util.Locale;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
@Log4j2
public class I18nManager {

    private static MessageSource messageSource;

    @Autowired
    public I18nManager(MessageSource messageSource) {
        I18nManager.messageSource = messageSource;
    }

    public static String getMessage(String messageKey) {
        return resolveMessage(null, messageKey, null);
    }

    public static String getMessage(String messageKey, Object... args) {
        return resolveMessage(null, messageKey, args);
    }

    public static String getMessage(Locale locale, String messageKey, Object... args) {
        return resolveMessage(locale, messageKey, args);
    }

    private static String resolveMessage(Locale locale, String key, Object[] args) {
        if (locale == null) {
            locale = new Locale("zh_CN");
        }
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Throwable t) {
            log.error("i18n error, message not found by key :" + key, t);
            return key;
        }
    }
}
