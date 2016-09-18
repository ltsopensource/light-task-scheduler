package com.github.ltsopensource.alarm.email;

import com.github.ltsopensource.alarm.AbstractAlarmNotifier;
import com.github.ltsopensource.alarm.AlarmNotifyException;
import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class EmailAlarmNotifier extends AbstractAlarmNotifier<EmailAlarmMessage> {

    private MailManager mailManager;

    public EmailAlarmNotifier(AppContext appContext) {
        this.mailManager = getMailManager(appContext);
    }

    private MailManager getMailManager(AppContext appContext) {
        Config config = appContext.getConfig();
        String host = config.getParameter(ExtConfig.MAIL_SMTP_HOST);
        String port = config.getParameter(ExtConfig.MAIL_SMTP_PORT);
        String userName = config.getParameter(ExtConfig.MAIL_USERNAME);
        String password = config.getParameter(ExtConfig.MAIL_PASSWORD);
        String adminAddress = config.getParameter(ExtConfig.MAIL_ADMIN_ADDR);
        boolean sslEnabled = config.getParameter(ExtConfig.MAIL_SSL_ENABLED, true);
        return new SMTPMailManagerImpl(host, port, userName, password, adminAddress, sslEnabled);
    }

    @Override
    protected void doNotice(EmailAlarmMessage message) {
        try {
            mailManager.send(message.getTo(), message.getTitle(), message.getMsg());
        } catch (Exception e) {
            throw new AlarmNotifyException("EmailAlarmNotifier send error", e);
        }
    }
}
