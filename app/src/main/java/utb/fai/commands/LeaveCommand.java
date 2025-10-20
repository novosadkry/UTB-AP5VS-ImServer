package utb.fai.commands;

import utb.fai.ActiveHandlers;
import utb.fai.Command;
import utb.fai.SocketHandler;

public class LeaveCommand implements Command {
    @Override
    public void execute(SocketHandler handler, ActiveHandlers activeHandlers, String[] args) {
        if (args.length < 1) {
            System.err.println("leave command requires a chat room name argument");
            return;
        }
        String chatRoomName = args[0];

        handler.leaveChatRoom(chatRoomName);
    }
}
