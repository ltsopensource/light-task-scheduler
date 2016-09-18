package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LogLevel;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaLogger extends IoFilterAdapter {
    /**
     * The logger name
     */
    private final String name;

    /**
     * The logger
     */
    private final Logger logger;

    /**
     * The log level for the exceptionCaught event. Default to WARN.
     */
    private LogLevel exceptionCaughtLevel = LogLevel.WARN;

    /**
     * The log level for the messageSent event. Default to INFO.
     */
    private LogLevel messageSentLevel = LogLevel.INFO;

    /**
     * The log level for the messageReceived event. Default to INFO.
     */
    private LogLevel messageReceivedLevel = LogLevel.INFO;

    /**
     * The log level for the sessionCreated event. Default to INFO.
     */
    private LogLevel sessionCreatedLevel = LogLevel.INFO;

    /**
     * The log level for the sessionOpened event. Default to INFO.
     */
    private LogLevel sessionOpenedLevel = LogLevel.INFO;

    /**
     * The log level for the sessionIdle event. Default to INFO.
     */
    private LogLevel sessionIdleLevel = LogLevel.INFO;

    /**
     * The log level for the sessionClosed event. Default to INFO.
     */
    private LogLevel sessionClosedLevel = LogLevel.INFO;

    /**
     * Default Constructor.
     */
    public MinaLogger() {
        this(MinaLogger.class.getName());
    }

    /**
     * Create a new NoopFilter using a class name
     *
     * @param clazz the cass which name will be used to create the logger
     */
    public MinaLogger(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Create a new NoopFilter using a name
     *
     * @param name the name used to create the logger. If null, will default to "NoopFilter"
     */
    public MinaLogger(String name) {
        if (name == null) {
            this.name = MinaLogger.class.getName();
        } else {
            this.name = name;
        }

        logger = LoggerFactory.getLogger(this.name);
    }

    /**
     * @return The logger's name
     */
    public String getName() {
        return name;
    }

    /**
     * Log if the logger and the current event log level are compatible. We log
     * a message and an exception.
     *
     * @param eventLevel the event log level as requested by the user
     * @param message    the message to log
     * @param cause      the exception cause to log
     */
    private void log(LogLevel eventLevel, String message, Throwable cause) {
        switch (eventLevel) {
            case TRACE:
                logger.trace(message, cause);
                return;
            case DEBUG:
                logger.debug(message, cause);
                return;
            case INFO:
                logger.info(message, cause);
                return;
            case WARN:
                logger.warn(message, cause);
                return;
            case ERROR:
                logger.error(message, cause);
                return;
            default:
                return;
        }
    }

    /**
     * Log if the logger and the current event log level are compatible. We log
     * a formated message and its parameters.
     *
     * @param eventLevel the event log level as requested by the user
     * @param message    the formated message to log
     * @param param      the parameter injected into the message
     */
    private void log(LogLevel eventLevel, String message, Object param) {
        switch (eventLevel) {
            case TRACE:
                logger.trace(message, param);
                return;
            case DEBUG:
                logger.debug(message, param);
                return;
            case INFO:
                logger.info(message, param);
                return;
            case WARN:
                logger.warn(message, param);
                return;
            case ERROR:
                logger.error(message, param);
                return;
            default:
                return;
        }
    }

    /**
     * Log if the logger and the current event log level are compatible. We log
     * a simple message.
     *
     * @param eventLevel the event log level as requested by the user
     * @param message    the message to log
     */
    private void log(LogLevel eventLevel, String message) {
        switch (eventLevel) {
            case TRACE:
                logger.trace(message);
                return;
            case DEBUG:
                logger.debug(message);
                return;
            case INFO:
                logger.info(message);
                return;
            case WARN:
                logger.warn(message);
                return;
            case ERROR:
                logger.error(message);
                return;
            default:
                return;
        }
    }

    @Override
    public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
        log(exceptionCaughtLevel, "EXCEPTION :", cause);
        nextFilter.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        log(messageReceivedLevel, "RECEIVED: {}", message);
        nextFilter.messageReceived(session, message);
    }

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        log(messageSentLevel, "SENT: {}", writeRequest.getOriginalRequest().getMessage());
        nextFilter.messageSent(session, writeRequest);
    }

    @Override
    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
        log(sessionCreatedLevel, "CREATED");
        nextFilter.sessionCreated(session);
    }

    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
        log(sessionOpenedLevel, "OPENED");
        nextFilter.sessionOpened(session);
    }

    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        log(sessionIdleLevel, "IDLE");
        nextFilter.sessionIdle(session, status);
    }

    @Override
    public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
        log(sessionClosedLevel, "CLOSED");
        nextFilter.sessionClosed(session);
    }

    /**
     * Set the LogLevel for the ExceptionCaught event.
     *
     * @param level The LogLevel to set
     */
    public void setExceptionCaughtLogLevel(LogLevel level) {
        exceptionCaughtLevel = level;
    }

    /**
     * Get the LogLevel for the ExceptionCaught event.
     *
     * @return The LogLevel for the ExceptionCaught eventType
     */
    public LogLevel getExceptionCaughtLogLevel() {
        return exceptionCaughtLevel;
    }

    /**
     * Set the LogLevel for the MessageReceived event.
     *
     * @param level The LogLevel to set
     */
    public void setMessageReceivedLogLevel(LogLevel level) {
        messageReceivedLevel = level;
    }

    /**
     * Get the LogLevel for the MessageReceived event.
     *
     * @return The LogLevel for the MessageReceived eventType
     */
    public LogLevel getMessageReceivedLogLevel() {
        return messageReceivedLevel;
    }

    /**
     * Set the LogLevel for the MessageSent event.
     *
     * @param level The LogLevel to set
     */
    public void setMessageSentLogLevel(LogLevel level) {
        messageSentLevel = level;
    }

    /**
     * Get the LogLevel for the MessageSent event.
     *
     * @return The LogLevel for the MessageSent eventType
     */
    public LogLevel getMessageSentLogLevel() {
        return messageSentLevel;
    }

    /**
     * Set the LogLevel for the SessionCreated event.
     *
     * @param level The LogLevel to set
     */
    public void setSessionCreatedLogLevel(LogLevel level) {
        sessionCreatedLevel = level;
    }

    /**
     * Get the LogLevel for the SessionCreated event.
     *
     * @return The LogLevel for the SessionCreated eventType
     */
    public LogLevel getSessionCreatedLogLevel() {
        return sessionCreatedLevel;
    }

    /**
     * Set the LogLevel for the SessionOpened event.
     *
     * @param level The LogLevel to set
     */
    public void setSessionOpenedLogLevel(LogLevel level) {
        sessionOpenedLevel = level;
    }

    /**
     * Get the LogLevel for the SessionOpened event.
     *
     * @return The LogLevel for the SessionOpened eventType
     */
    public LogLevel getSessionOpenedLogLevel() {
        return sessionOpenedLevel;
    }

    /**
     * Set the LogLevel for the SessionIdle event.
     *
     * @param level The LogLevel to set
     */
    public void setSessionIdleLogLevel(LogLevel level) {
        sessionIdleLevel = level;
    }

    /**
     * Get the LogLevel for the SessionIdle event.
     *
     * @return The LogLevel for the SessionIdle eventType
     */
    public LogLevel getSessionIdleLogLevel() {
        return sessionIdleLevel;
    }

    /**
     * Set the LogLevel for the SessionClosed event.
     *
     * @param level The LogLevel to set
     */
    public void setSessionClosedLogLevel(LogLevel level) {
        sessionClosedLevel = level;
    }

    /**
     * Get the LogLevel for the SessionClosed event.
     *
     * @return The LogLevel for the SessionClosed eventType
     */
    public LogLevel getSessionClosedLogLevel() {
        return sessionClosedLevel;
    }
}