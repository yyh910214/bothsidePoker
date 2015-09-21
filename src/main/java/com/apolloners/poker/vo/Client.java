/**
 * 2015. 9. 12.
 * Copyright by yyh / Hubigo AIAL
 * Client.java
 */
package com.apolloners.poker.vo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apolloners.poker.common.CommonCode;
import com.apolloners.poker.room.GameRoom;
import com.apolloners.poker.room.WaitingRoom;

public class Client {
	protected static Logger logger = LoggerFactory.getLogger(Client.class);
	private GameRoom gameRoom;
	private WaitingRoom waitingRoom;
	private Channel channel;	//쓰기 할 경우 사용할 채널
	
	private boolean isPlaying;
	private String userId;
	
	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	/**
	 * @return the isPlaying
	 */
	public boolean isPlaying() {
		return isPlaying;
	}
	/**
	 * @param isPlaying the isPlaying to set
	 */
	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the gameRoom
	 */
	public GameRoom getGameRoom() {
		return gameRoom;
	}
	/**
	 * @param gameRoom the gameRoom to set
	 */
	public void setGameRoom(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
	}
	/**
	 * @return the waitingRoom
	 */
	public WaitingRoom getWaitingRoom() {
		return waitingRoom;
	}
	/**
	 * @param waitingRoom the waitingRoom to set
	 */
	public void setWaitingRoom(WaitingRoom waitingRoom) {
		this.waitingRoom = waitingRoom;
	}
	
	public void write(final String message)	{
		final ChannelFuture f = channel.writeAndFlush(message);
		
		f.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				assert f == future;
				logger.debug(userId + " send : " + message);
			}
		});
	}
	
}
