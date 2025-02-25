package Client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import Client.netty.initializer.NettyClientInitializer;
import Client.rpcClient.RPCClient;
import Client.serviceCentre.ServiceCentre;
import Client.serviceCentre.ZKServiceCentre;
import common.message.RPCRequest;
import common.message.RPCResponse;

import java.net.InetSocketAddress;

public class NettyRPCClient implements RPCClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private ServiceCentre serviceCentre;

    public NettyRPCClient() throws InterruptedException {
        this.serviceCentre = new ZKServiceCentre();
    }

    // Initialize the netty server
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // Configure Netty's message handling mechanism
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        // Get host and port from the service discovery
        InetSocketAddress address = serviceCentre.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            // Create a ChannelFuture object, representing this operation event
            // The sync method blocks until the connect operation is complete
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            // Send the request to the server
            channel.writeAndFlush(request);
            System.out.println("RPCRequest is sent to " + host + ":" + port);
            // sync() blocks to get response
            channel.closeFuture().sync();
            // Obtain results in a blocking manner
            // Assign an alias to the channel to retrieve content from the channel with a specific name (this is set in the handler)
            // AttributeKey is thread-isolated and does not have thread safety issues
            // In the current scenario, choosing to obtain results in a blocking manner
            // In other scenarios, a listener can be added to asynchronously obtain results using channelFuture.addListener...
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
            RPCResponse response = channel.attr(key).get();

            System.out.println(response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
