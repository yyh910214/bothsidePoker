/**
 * 2015. 9. 11.
 * Copyright by yyh / Hubigo AIAL
 * BothSidePokerServerHandler.java
 */
package com.apolloners.poker.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apolloners.poker.common.CommonCode;
import com.apolloners.poker.common.Protocol;
import com.apolloners.poker.room.GameRoom;
import com.apolloners.poker.room.WaitingRoom;
import com.apolloners.poker.vo.Client;

public class BothSidePokerServerHandler extends ChannelInboundHandlerAdapter {

	protected static final Logger logger = LoggerFactory.getLogger(BothSidePokerServerHandler.class);

	private static WaitingRoom waitingRoom;
	private static ConcurrentHashMap<String, Client> clientMap;
	
	static {
		waitingRoom = new WaitingRoom();
		clientMap = new ConcurrentHashMap<String, Client>();
	}

	public BothSidePokerServerHandler() {
	}

	public BothSidePokerServerHandler(WaitingRoom waitingRoom) {
		this();
		this.waitingRoom = waitingRoom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelRegistered(io.netty
	 * .channel.ChannelHandlerContext)
	 */
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// 대기방 입장
		Client client = new Client();
		client.setChannel(ctx.channel());
		enterWaitingRoom(client);

		clientMap.put(((InetSocketAddress)ctx.channel().remoteAddress()).toString(), client);
		// 성공 메시지 전송
		
		final ChannelFuture f = client.getChannel().writeAndFlush(CommonCode.SUCCESS);
		
		f.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				assert f == future;
				logger.debug("send is success");
			}
		});
		
		logger.info("Client is Registerd");
	}

	public void enterWaitingRoom(Client client) {
		// 처음 대기방 입장
		waitingRoom.enterRoom(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel
	 * .ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
//		ByteBuf in = (ByteBuf) msg;
//		 StringBuilder sb = new StringBuilder();
		try {
//			 while (in.isReadable()) {
//				 sb.append((char)in.readByte());
//			 }

			action(clientMap.get(((InetSocketAddress)ctx.channel().remoteAddress()).toString()), (String)msg);
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.netty.channel.ChannelHandlerAdapter#exceptionCaught(io.netty.channel
	 * .ChannelHandlerContext, java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
	}

	protected void action(Client client, String message) {
		if(client == null)	{
			logger.debug("client is null");
		}
		logger.debug(client.getUserId());
		logger.debug(client.getUserId() + "send message : " + message);
		logger.debug(message);
		String[] messages = message.split("\\|");
		switch (Protocol.valueOf(messages[0])) {
		case NAME:
			client.setUserId(messages[1]);
			break;
		case CREATE:
			createGameRoom(client, messages[1]);
			break;
		case JOIN:
			enterGameRoom(client, messages[1]);
			break;
		case REFRESH:
			refreshWaitingList(client);
			break;
		case EXIT:
			exitGameRoom(client);
			break;
		case READY:
			client.getGameRoom().readyGame();
			break;
		case START:
			client.getGameRoom().startGame();
			break;
		case BET:
			client.getGameRoom().doBet(client, messages[1], Integer.parseInt(messages[2]));
			break;
		case DIE:
			client.getGameRoom().doDie(client);
			break;
		}
	}

	protected void createGameRoom(Client client, String title) {
		GameRoom gameRoom = waitingRoom.createGameRoom(title, client);
		if (gameRoom != null) {
			client.setGameRoom(gameRoom);
			client.setPlaying(true);
			client.getChannel()
					.writeAndFlush(Protocol.CREATE + CommonCode.DELIMITER
							+ CommonCode.SUCCESS);

			logger.info(client.getUserId() + " create Gameroom");
		} else {
			client.getChannel().writeAndFlush(
					Protocol.CREATE + CommonCode.DELIMITER
							+ CommonCode.EXCEED_ROOM);
			logger.info(client.getUserId() + "fail to create Gameroom");
		}
	}

	public void enterGameRoom(Client client, String roomNo) {
		GameRoom gameRoom = waitingRoom.getGameRoom(Integer.parseInt(roomNo));

		if (gameRoom == null) {
			client.getChannel()
					.writeAndFlush(Protocol.JOIN + CommonCode.DELIMITER
							+ CommonCode.NOT_EXIST);
			logger.info(client.getUserId()
					+ " fail to enter room(room is not exist)");
		}

		int result = gameRoom.enterRoom(client);
		if (result == 1) {
			client.setGameRoom(gameRoom);
			client.setPlaying(true);
			client.getChannel().writeAndFlush(
					Protocol.JOIN + CommonCode.DELIMITER + CommonCode.SUCCESS);
			logger.info(client.getUserId() + " is entered room");
		} else if (result == -1) {
			client.getChannel().writeAndFlush(
					Protocol.JOIN + CommonCode.DELIMITER
							+ CommonCode.EXCEED_PERSON);
			logger.info(client.getUserId()
					+ " fail to enter room(exceed person)");
		}
	}

	public void exitGameRoom(Client client) {
		GameRoom gameRoom = client.getGameRoom();

		if (gameRoom != null) {
			gameRoom.exitRoom(client);
			client.setGameRoom(null);
			client.setPlaying(false);
			logger.info(client.getUserId() + " exit the game room.");
		}

		client.getChannel().writeAndFlush(
				Protocol.EXIT + CommonCode.DELIMITER + CommonCode.SUCCESS);
	}

	public void refreshWaitingList(Client client) {
		String roomList = waitingRoom.getRefreshJsonString();
		client.getChannel().writeAndFlush(
				Protocol.REFRESH + CommonCode.DELIMITER + roomList);
	}
}
