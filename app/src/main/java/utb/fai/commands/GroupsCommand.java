package utb.fai.commands;

import utb.fai.ActiveHandlers;
import utb.fai.ChatRoomHandlers;
import utb.fai.Command;
import utb.fai.SocketHandler;

public class GroupsCommand implements Command {
    @Override
    public void execute(SocketHandler handler, ActiveHandlers activeHandlers, String[] args) {
        StringBuilder groupList = new StringBuilder();

        for (ChatRoomHandlers chatRoom : handler.getChatRooms()) {
            if (!groupList.isEmpty()) {
                groupList.append(", ");
            }
            groupList.append(chatRoom.getChatRoomName());
        }

        handler.offerMessage(groupList.toString());
    }
}
