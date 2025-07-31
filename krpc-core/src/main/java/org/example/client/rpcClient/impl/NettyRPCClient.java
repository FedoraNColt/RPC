package org.example.client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.example.client.netty.initializer.NettyClientInitializer;
import org.example.client.rpcClient.RPCClient;
import org.example.client.serviceCenter.ServiceCenter;
import org.example.common.message.RPCRequest;
import org.example.common.message.RPCResponse;

import java.net.InetSocketAddress;

public class NettyRPCClient implements RPCClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private final ServiceCenter serviceCentre;

    public NettyRPCClient(ServiceCenter serviceCentre) throws InterruptedException {
        this.serviceCentre = serviceCentre;
    }

    // Initialize the Netty client bootstrap and event loop group
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // Set up the client-side channel pipeline
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        // Discover service instance location
        InetSocketAddress address = serviceCentre.serviceDiscovery(request.getInterfaceName());
        if (address == null) {
            throw new RuntimeException("No available service instance found for " + request.getInterfaceName());
        }
        
        String host = address.getHostName();
        int port = address.getPort();
        Channel channel = null;
        try {
            // Establish connection to the server and wait for completion
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channel = channelFuture.channel();
            
            // Send the RPC request
            channel.writeAndFlush(request);
            System.out.println("RPCRequest is sent to " + host + ":" + port);
            
            // Wait for the channel to close (indicating response received)
            channel.closeFuture().sync();
            
            // Retrieve response from channel attributes (set by the client handler)
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
            RPCResponse response = channel.attr(key).get();

            System.out.println(response);
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to " + host + ":" + port, e);
        } finally {
            // Ensure channel is properly closed
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close().sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Shutdown the Netty client and release resources
     */
    public static void shutdown() {
        if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
