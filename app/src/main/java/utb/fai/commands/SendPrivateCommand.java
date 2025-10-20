package utb.fai.commands;

import utb.fai.ActiveHandlers;
import utb.fai.Command;
import utb.fai.SocketHandler;

import java.util.Arrays;

public class SendPrivateCommand implements Command {
    @Override
    public void execute(SocketHandler handler, ActiveHandlers activeHandlers, String[] args) {
        if (args.length < 2) {
            System.err.println("sendPrivate command requires a recipient name and a message");
            return;
        }
        String recipientName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        for (SocketHandler h : activeHandlers.get()) {
            if (h.getClientName().equals(recipientName)) {
                h.offerMessage(String.format("[%s] >> %s", handler.getClientName(), message));
                return;
            }
        }

        handler.offerMessage("User " + recipientName + " not found.");
    }
}
