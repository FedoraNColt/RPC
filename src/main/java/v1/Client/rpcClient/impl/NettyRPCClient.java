package v1.Client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import v1.Client.netty.initializer.NettyClientInitializer;
import v1.Client.rpcClient.RPCClient;
import v1.Client.serviceCentre.ServiceCentre;
import v1.Client.serviceCentre.ZKServiceCentre;
import v1.common.message.RPCRequest;
import v1.common.message.RPCResponse;

import java.net.InetSocketAddress;

public class NettyRPCClient implements RPCClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private ServiceCentre serviceCentre;

    public NettyRPCClient() {
        this.serviceCentre = new ZKServiceCentre();
    }

    // Initialize the netty server
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        // Get host and port from the service discovery
        InetSocketAddress address = serviceCentre.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(request);
            System.out.println("RPCRequest is sent to " + host + ":" + port);
            // sync() blocks to get response
            channel.closeFuture().sync();
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
