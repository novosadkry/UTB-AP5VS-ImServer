package utb.fai;

public interface Command {
    void execute(SocketHandler handler, ActiveHandlers activeHandlers, String[] args);
}
