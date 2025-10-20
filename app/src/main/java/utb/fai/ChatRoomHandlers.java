package utb.fai;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoomHandlers extends ActiveHandlers {
    private static final HashMap<String, ChatRoomHandlers> chatRooms = new HashMap<>();

    private final String chatRoomName;

    public ChatRoomHandlers(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public static synchronized void addChatRoom(ChatRoomHandlers chatRoom) {
        chatRooms.put(chatRoom.chatRoomName, chatRoom);
    }

    public static synchronized void removeChatRoom(String chatRoomName) {
        chatRooms.remove(chatRoomName);
    }

    public static synchronized ArrayList<ChatRoomHandlers> getChatRooms() {
        return new ArrayList<>(chatRooms.values());
    }

    public static synchronized ChatRoomHandlers getChatRoom(String chatRoomName) {
        return chatRooms.get(chatRoomName);
    }
}
