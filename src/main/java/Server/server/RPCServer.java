package Server.server;

public interface RPCServer {
    // Start listening
    void start(int port);
    void stop();
}
