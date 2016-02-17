package com.lts.alarm.email;

import org.junit.Test;

/**
 * Created by hugui.hg on 2/17/16.
 */
public class SMTPMailManagerImplTest {

    @Test
    public void testSend() throws Exception {
        String host = "smtp.qq.com";
        // 授权码从这里获取 http://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=1001256
        MailManager mailManager = new SMTPMailManagerImpl(host, "254963746@qq.com", "这里是授权码", "254963746@qq.com", true);
        mailManager.send("test@qq.com", "测试", "fdsafhakdsjfladslfj呵呵呵");
    }
}