package com.github.ltsopensource.admin.web.support;

import com.github.ltsopensource.core.commons.utils.DateUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date转换器
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class DateEditor extends PropertyEditorSupport {

    //    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DateFormat dateFormat;
    private boolean allowEmpty = true;

    public DateEditor() {
    }

    public DateEditor(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public DateEditor(DateFormat dateFormat, boolean allowEmpty) {
        this.dateFormat = dateFormat;
        this.allowEmpty = allowEmpty;
    }

    /**
     * Parse the Date from the given text, using the specified DateFormat.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else {
            try {
                if (this.dateFormat != null)
                    setValue(this.dateFormat.parse(text));
                else {
                    setValue(DateUtils.parse(text));
                }
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Format the Date as String, using the specified DateFormat.
     */
    @Override
    public String getAsText() {
        Date value = (Date) getValue();
        DateFormat dateFormat = this.dateFormat;
        if (dateFormat == null)
            dateFormat = TIME_FORMAT;
        return (value != null ? dateFormat.format(value) : "");
    }
}
