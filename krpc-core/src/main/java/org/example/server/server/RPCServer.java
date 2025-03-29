package org.example.server.server;

public interface RPCServer {
    // Start listening
    void start(int port);
    void stop();
}
