package utb.fai;

import utb.fai.commands.*;

import java.util.HashMap;

public class CommandHandler {
    private final ActiveHandlers activeHandlers;
    private final HashMap<String, Command> commands = new HashMap<>();

    public CommandHandler(ActiveHandlers activeHandlers) {
        this.activeHandlers = activeHandlers;
        commands.put("setMyName", new SetMyNameCommand());
        commands.put("sendPrivate", new SendPrivateCommand());
        commands.put("join", new JoinCommand());
        commands.put("leave", new LeaveCommand());
        commands.put("groups", new GroupsCommand());
    }

    public void parseAndExecute(SocketHandler handler, String message) {
        String[] parts = message.split(" ", 2);
        String commandName = parts[0];

        var command = commands.get(commandName);

        if (command != null) {
            String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];
            command.execute(handler, activeHandlers, args);
        } else {
            handler.offerMessage("Unknown command: " + commandName);
        }
    }
}
