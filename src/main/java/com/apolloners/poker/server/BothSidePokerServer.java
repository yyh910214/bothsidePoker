/**
 * 2015. 9. 11.
 * Copyright by yyh / Hubigo AIAL
 * BothSidePokerServer.java
 */
package com.apolloners.poker.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.example.discard.TimeServerHandler;

public class BothSidePokerServer {
	
	public static void main(String[] args) throws Exception	{
		int port = 20304;
		if(args.length > 0)	{
			port = Integer.parseInt(args[0]);
		}
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try	{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				 @Override
				 public void initChannel(SocketChannel ch) throws Exception	{
					 ch.pipeline().addLast(new TimeServerHandler());
				 }
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture f = b.bind(port).sync();	// 서버를 바인딩
			f.channel().closeFuture().sync();
		} finally	{
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
