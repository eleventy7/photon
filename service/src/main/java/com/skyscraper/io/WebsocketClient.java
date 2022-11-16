package com.skyscraper.io;

import com.skyscraper.config.VenueConfig;
import com.skyscraper.enums.Venue;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.net.URI;

@Slf4j
public abstract class WebsocketClient implements MarketDataSink {
    @Getter
    final VenueConfig venueConfig;
    private final String url;
    private final SimpleChannelInboundHandler<ByteBuf> handler;
    private Channel channel;
    private EventLoopGroup group;
    @Setter
    private int maxFramePayloadLength = 65536;

    public WebsocketClient(String url, BaseMarketDataHandler handler) {
        this.venueConfig = handler.getVenueConfig();
        this.url = url;
        this.handler = handler;
    }

    public void connect() throws Exception {
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().build();
        } else {
            sslCtx = null;
        }

        group = new NioEventLoopGroup();
        try {
            final FrameHandler frameHandler =
                    new FrameHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri,
                                    WebSocketVersion.V13,
                                    null,
                                    false,
                                    new DefaultHttpHeaders(),
                                    maxFramePayloadLength));

            Bootstrap b = new Bootstrap()
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(16384),
                                    new WebSocketFrameAggregator(1048576),
                                    frameHandler,
                                    handler);
                        }
                    });

            channel = b.connect(uri.getHost(), port).sync().channel();
            frameHandler.handshakeFuture().sync();
        } catch (Exception e) {
            log.error("problem starting {} websocket: {}", venueConfig.getVenue(), e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.closeFuture().sync();
            group.shutdownGracefully();
        } catch (InterruptedException e) {
            log.error("problem disconnecting {} websocket: {}", venueConfig.getVenue(), e.getMessage());
        }
    }

    public void send(byte[] bytes) {
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
    }

    public void send(ByteArrayOutputStream byteArrayOutputStream) {
        send(byteArrayOutputStream.toByteArray());
    }

    // prefer the above methods to this one
    public void send(String text) {
        channel.writeAndFlush(new TextWebSocketFrame(text));
    }
}
