package com.lts.alarm.email;

import com.lts.alarm.AbstractAlarmNotifier;
import com.lts.alarm.AlarmNotifyException;
import com.lts.core.AppContext;
import com.lts.core.cluster.Config;

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
        String host = config.getParameter("mail.smtp.host");
        String port = config.getParameter("mail.smtp.port");
        String userName = config.getParameter("mail.username");
        String password = config.getParameter("mail.password");
        String adminAddress = config.getParameter("mail.adminAddress");
        boolean sslEnabled = config.getParameter("mail.sslEnabled", true);
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
