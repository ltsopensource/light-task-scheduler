package com.github.ltsopensource.remoting.protocol;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class RemotingCommandHelper {

    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 1, RPC

    public static void markResponseType(RemotingCommand remotingCommand) {
        int bits = 1 << RPC_TYPE;
        remotingCommand.setFlag(remotingCommand.getFlag() | bits);
    }

    public static boolean isResponseType(RemotingCommand remotingCommand) {
        int bits = 1 << RPC_TYPE;
        return (remotingCommand.getFlag() & bits) == bits;
    }

    public static void markOnewayRPC(RemotingCommand remotingCommand) {
        int bits = 1 << RPC_ONEWAY;
        remotingCommand.setFlag(remotingCommand.getFlag() | bits);
    }

    public static boolean isOnewayRPC(RemotingCommand remotingCommand) {
        int bits = 1 << RPC_ONEWAY;
        return (remotingCommand.getFlag() & bits) == bits;
    }

    public static RemotingCommandType getRemotingCommandType(RemotingCommand remotingCommand) {
        if (RemotingCommandHelper.isResponseType(remotingCommand)) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
        return RemotingCommandType.REQUEST_COMMAND;
    }

}
