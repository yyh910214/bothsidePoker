/**
 * 2015. 9. 11.
 * Copyright by yyh / Hubigo AIAL
 * DiscardServer.java
 */
package io.netty.example.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DiscardServer {
	private int port;
	
	public DiscardServer(int port)	{
		this.port = port;
	}
	
	public void run() throws Exception	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();	//외부로 부터의 이벤트를 받을 루프.
		EventLoopGroup workerGroup = new NioEventLoopGroup(); //실제로 처리할 이벤트 루프 그룹
		
		try	{
			ServerBootstrap b = new ServerBootstrap();	//서버 구성을 편리하게 해줄 유틸.
			
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
			 
			 ChannelFuture f = b.bind(port).sync();
			 
			 f.channel().closeFuture().sync();
		} finally	{
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception	{
		int port;
		if(args.length > 0)	{
			port = Integer.parseInt(args[0]);
		} else	{
			port = 20304;
		}
		
		new DiscardServer(port).run();
	}
}
