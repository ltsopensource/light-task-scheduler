package com.github.ltsopensource.alarm.email;

public interface MailManager {
    void send(String to, String title, String message) throws Exception;
}

