package utb.fai.commands;

import utb.fai.ActiveHandlers;
import utb.fai.Command;
import utb.fai.SocketHandler;

public class SetMyNameCommand implements Command {
    @Override
    public void execute(SocketHandler handler, ActiveHandlers activeHandlers, String[] args) {
        if (args.length < 1) {
            System.err.println("setMyName command requires a name argument");
            return;
        }
        String newName = args[0];

        for (SocketHandler h : activeHandlers.get()) {
            if (h != handler && newName.equals(h.getClientName())) {
                handler.offerMessage("Name " + newName + " is already taken. Choose another name.");
                return;
            }
        }

        handler.setClientName(newName);
        System.out.println("Client " + handler.getClientID() + " set name to " + newName);
    }
}
