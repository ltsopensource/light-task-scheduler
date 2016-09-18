package com.github.ltsopensource.alarm.email;

import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 2/17/16.
 */
public class SMTPMailManagerImplTest {

    @Test
    public void testSend() throws Exception {
        String host = "smtp.qq.com";
        // 授权码从这里获取 http://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=1001256
        // lts12345
        MailManager mailManager = new SMTPMailManagerImpl(host, "2179816070@qq.com", "这里是授权码", "LTS通知中心(notice@lts.com)", true);
        mailManager.send("254963746@qq.com", "测试", "fdsafhakdsjfladslfj呵呵呵");
    }
}