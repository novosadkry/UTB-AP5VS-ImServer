package utb.fai;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.concurrent.*;

public class SocketHandler {
	/** mySocket je socket, o který se bude tento SocketHandler starat */
    private Socket mySocket;

	/** client ID je øetìzec ve formátu <IP_adresa>:<port> */
	private String clientID;
    private String clientName;

    private CommandHandler commandHandler;

	/**
	 * activeHandlers je reference na mnoinu vech právì bìících SocketHandlerù.
	 * Potøebujeme si ji udrovat, abychom mohli zprávu od tohoto klienta
	 * poslat vem ostatním!
	 */
	private ActiveHandlers activeHandlers;
    private HashSet<ChatRoomHandlers> chatRoomHandlers = new HashSet<>();

	/**
	 * messages je fronta pøíchozích zpráv, kterou musí mít kaý klient svoji
	 * vlastní - pokud bude je pøetíená nebo nefunkèní klientova sí,
	 * èekají zprávy na doruèení právì ve frontì messages
	 */
    private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(20);

	/**
	 * startSignal je synchronizaèní závora, která zaøizuje, aby oba tasky
	 * OutputHandler.run() a InputHandler.run() zaèaly ve stejný okamik.
	 */
    private CountDownLatch startSignal = new CountDownLatch(2);

	/** outputHandler.run() se bude starat o OutputStream mého socketu */
    private OutputHandler outputHandler = new OutputHandler();
	/** inputHandler.run() se bude starat o InputStream mého socketu */
    private InputHandler inputHandler = new InputHandler();
	/**
	 * protoe v outputHandleru nedovedu detekovat uzavøení socketu, pomùe mi
	 * inputFinished
	 */
    private volatile boolean inputFinished = false;

	public SocketHandler(Socket mySocket, ActiveHandlers activeHandlers) {
		this.mySocket = mySocket;
		clientID = mySocket.getInetAddress().toString() + ":" + mySocket.getPort();
		this.activeHandlers = activeHandlers;
        this.commandHandler = new CommandHandler(activeHandlers);
	}

    public class OutputHandler implements Runnable {
		public void run() {
			OutputStreamWriter writer;
			try {
				System.err.println("DBG>Output handler starting for " + clientID);
				startSignal.countDown();
				startSignal.await();
				System.err.println("DBG>Output handler running for " + clientID);
				writer = new OutputStreamWriter(mySocket.getOutputStream(), "UTF-8");
                // Causes concurrency issues during testing ??
				// writer.write("\nYou are connected from " + clientID + "\n");
				writer.flush();
				while (!inputFinished) {
					String m = messages.take();// blokující ètení - pokud není ve frontì zpráv nic, uspi se!
					writer.write(m + "\r\n"); // pokud nìjaké zprávy od ostatních máme,
					writer.flush(); // poleme je naemu klientovi
					System.err.println("DBG>Message sent to " + clientID + ":" + m + "\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println("DBG>Output handler for " + clientID + " has finished.");

		}
	}

	public class InputHandler implements Runnable {
		public void run() {
			try {
				System.err.println("DBG>Input handler starting for " + clientID);
				startSignal.countDown();
				startSignal.await();
				System.err.println("DBG>Input handler running for " + clientID);
				String request = "";
				/**
				 * v okamiku, kdy nás Thread pool spustí, pøidáme se do mnoiny
				 * vech aktivních handlerù, aby chodily zprávy od ostatních i nám
				 */
				activeHandlers.add(SocketHandler.this);
                joinChatRoom("public");
				BufferedReader reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream(), "UTF-8"));
				while ((request = reader.readLine()) != null) { // pøila od mého klienta nìjaká zpráva?
                    if (request.startsWith("#")) {
                        commandHandler.parseAndExecute(SocketHandler.this, request.substring(1));
                        continue;
                    }

                    if (clientName == null) {
                        clientName = request;
                        continue;
                    }

					// ano - poli ji vem ostatním klientùm
                    request = String.format("[%s] >> %s", clientName, request);
					System.out.println(request);
                    for (ChatRoomHandlers chatRoom : chatRoomHandlers) {
                        chatRoom.sendMessageToAll(SocketHandler.this, request);
                    }
				}
				inputFinished = true;
				messages.offer("OutputHandler, wakeup and die!");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				// remove yourself from the set of activeHandlers
                activeHandlers.remove(SocketHandler.this);
                for (ChatRoomHandlers chatRoom : chatRoomHandlers) {
                    chatRoom.remove(SocketHandler.this);
                }
			}
			System.err.println("DBG>Input handler for " + clientID + " has finished.");
		}
	}

    /** Not the best solution imo, but works */

    public void joinChatRoom(String chatRoomName) {
        var chatRoom = ChatRoomHandlers.getChatRoom(chatRoomName);
        if (chatRoom == null) {
            chatRoom = new ChatRoomHandlers(chatRoomName);
            ChatRoomHandlers.addChatRoom(chatRoom);
        }

        chatRoom.remove(this);
        chatRoom.add(this);
        this.chatRoomHandlers.add(chatRoom);
    }

    public void leaveChatRoom(String chatRoomName) {
        for (var chatRoom : chatRoomHandlers) {
            if (chatRoom.getChatRoomName().equals(chatRoomName)) {
                chatRoom.remove(this);
                this.chatRoomHandlers.remove(chatRoom);
                if (chatRoom.get().isEmpty()) {
                    ChatRoomHandlers.removeChatRoom(chatRoomName);
                }
                return;
            }
        }
    }

    public HashSet<ChatRoomHandlers> getChatRooms() {
        return chatRoomHandlers;
    }

    public String getClientID() {
        return clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public OutputHandler getOutputHandler() {
        return outputHandler;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public boolean offerMessage(String message) {
        return messages.offer(message);
    }
}
