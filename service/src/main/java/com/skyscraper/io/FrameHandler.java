package com.skyscraper.io;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrameHandler implements ChannelInboundHandler {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public FrameHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
                log.info("Websocket client connected!");
                handshakeFuture.setSuccess();
            } catch (Throwable e) {
                log.info("Websocket client failed to connect!");
                handshakeFuture.setFailure(e);
                ctx.close();
            } finally {
                ReferenceCountUtil.release(msg);
            }
        } else if (msg instanceof final FullHttpResponse rsp) {
            ReferenceCountUtil.release(msg);
            throw new IllegalStateException("unexpected HTTP response, status: " + rsp.status());
        } else if (msg instanceof TextWebSocketFrame) {
            ctx.fireChannelRead(((TextWebSocketFrame) msg).content());
        } else if (msg instanceof BinaryWebSocketFrame) {
            log.info("GOT BINARY FRAME");
            ctx.fireChannelRead(((BinaryWebSocketFrame) msg).content());
        } else if (msg instanceof PongWebSocketFrame) {
            ReferenceCountUtil.release(msg);
        } else if (msg instanceof final PingWebSocketFrame ping) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(ping.content()));
        } else if (msg instanceof CloseWebSocketFrame) {
            ReferenceCountUtil.release(msg);
            ctx.close();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent && IdleState.ALL_IDLE == ((IdleStateEvent) evt).state()) {
            log.info("user event triggered, sending ping");
            ctx.channel().writeAndFlush(new PingWebSocketFrame());
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        ctx.fireChannelWritabilityChanged();
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("problem in FrameHandler: {}", cause.getLocalizedMessage());
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}