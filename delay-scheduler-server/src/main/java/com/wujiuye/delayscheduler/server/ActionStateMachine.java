package com.wujiuye.delayscheduler.server;

import com.github.wujiuye.raft.CommandLog;
import com.github.wujiuye.raft.StateMachine;
import com.github.wujiuye.transport.netty.commom.JsonUtils;
import com.wujiuye.delayscheduler.core.ActionLog;
import com.wujiuye.delayscheduler.server.stroage.KeyValueStorage;

import java.nio.charset.StandardCharsets;

/**
 * @author wujiuye 2021/01/12
 */
public class ActionStateMachine implements StateMachine {

    private final KeyValueStorage storage;
    private final CommandHandler[] commandHandlers;

    public ActionStateMachine(KeyValueStorage storage) {
        this.storage = storage;
        this.commandHandlers = new CommandHandler[]{
                new SaveCommandHandler(), new DelCommandHandler()
        };
    }

    @Override
    public void apply(CommandLog execLog) {
        byte[] commandBytes = execLog.getCommand();
        String command = new String(commandBytes, StandardCharsets.UTF_8);
        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.match(command)) {
                commandHandler.execCommand(command);
            }
        }
    }

    public interface CommandHandler {

        /**
         * 匹配命令
         *
         * @param command
         * @return
         */
        boolean match(String command);

        /**
         * 执行命令
         *
         * @param command
         */
        void execCommand(String command);

    }

    private static String[] split(String source, String regex, int cnt) {
        String[] arrays = new String[cnt];
        for (int i = 0; i < cnt; i++) {
            int index = source.indexOf(regex);
            if (index == -1 || i == cnt - 1) {
                arrays[i] = source.trim();
                break;
            }
            arrays[i] = source.substring(0, index).trim();
            source = source.substring(index + 1);
        }
        return arrays;
    }

    public class SaveCommandHandler implements CommandHandler {

        @Override
        public boolean match(String command) {
            return command.startsWith(Command.SAVE.substring(0, 4));
        }

        @Override
        public void execCommand(String command) {
            String[] keyValue = split(command.substring(4), ",", 3);
            ActionLog actionLog = JsonUtils.fromJson(keyValue[2], ActionLog.class);
            storage.save(keyValue[0], keyValue[1], actionLog);
        }

    }

    public class DelCommandHandler implements CommandHandler {

        @Override
        public boolean match(String command) {
            return command.startsWith(Command.DEL.substring(0, 3));
        }

        @Override
        public void execCommand(String command) {
            String[] key = split(command.substring(3), ",", 2);
            storage.del(key[0], key[1]);
        }

    }

}
