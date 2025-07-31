package org.example.server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import org.example.server.netty.initializer.NettyServerInitializer;
import org.example.server.provider.ServiceProvider;
import org.example.server.server.RPCServer;

@AllArgsConstructor
public class NettyRPCServer implements RPCServer {
    private ServiceProvider serviceProvider;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ChannelFuture serverChannelFuture;

    public NettyRPCServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void start(int port) {
        System.out.println("Starting Netty RPC Server on port " + port);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));

            serverChannelFuture = bootstrap.bind(port).sync();
            System.out.println("Netty RPC Server started successfully on port " + port);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            
            serverChannelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server startup interrupted: " + e.getMessage());
            throw new RuntimeException("Failed to start server", e);
        } catch (Exception e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            throw new RuntimeException("Failed to start server", e);
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        System.out.println("Shutting down Netty RPC Server...");
        
        try {
            if (serverChannelFuture != null && serverChannelFuture.channel().isOpen()) {
                serverChannelFuture.channel().close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error closing server channel: " + e.getMessage());
        }
        
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        
        System.out.println("Netty RPC Server shutdown completed.");
    }
}
