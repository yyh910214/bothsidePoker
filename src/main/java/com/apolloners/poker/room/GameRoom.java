/**
 * 2015. 8. 26.
 * Copyright by yyh / Hubigo AIAL
 * GameRoom.java
 */
package com.apolloners.poker.room;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apolloners.poker.common.CommonCode;
import com.apolloners.poker.common.Protocol;
import com.apolloners.poker.game.Deck;
import com.apolloners.poker.vo.Client;
import com.apolloners.poker.vo.PokerGameInfo;

public class GameRoom {

	protected static final Logger logger = LoggerFactory
			.getLogger(GameRoom.class);

	private static final int MAX_ROUND = 9;

	private static AtomicInteger roomIndex = new AtomicInteger(1);

	private int roomNo;
	private String title;
	private String masterId;

	private Client master;
	private Client guest;

	/**
	 * make A Game Class, later
	 */

	private Client first, second;
	private PokerGameInfo masterGameInfo, guestGameInfo;

	private Deck deck;
	private int gameChip;

	private boolean playing;
	private boolean playOrder; // true : master first, false : guest first
	private boolean attackOrder; // true : Attack, false : Defense

	private int round;

	public GameRoom(String title, Client master) {
		this.title = title;
		this.master = master;
		this.guest = null;
		this.deck = new Deck();
		this.roomNo = roomIndex.getAndIncrement();
		masterId = master.getUserId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see genius.room.Room#enterRoom(genius.client.Client)
	 */
	public int enterRoom(Client client) {
		logger.info(client.getUserId() + "Enter the GameRoom" + this.roomNo);
		if (this.master == null) {
			this.master = client;
		} else {
			this.guest = client;
			this.master.write(
					Protocol.JOIN + CommonCode.DELIMITER + CommonCode.ID
							+ CommonCode.DELIMITER + this.guest.getUserId());
		}
		return 1;
	}

	public void startGame() {
		logger.info("Start Game Room No." + this.roomNo);
		this.playing = true;
		this.guest.write(Protocol.START.name());
		this.master.write(Protocol.START.name());

		this.playOrder = (Math.random() < 0.5);

		this.round = 0;
		masterGameInfo = new PokerGameInfo();
		masterGameInfo.setChip(30);
		guestGameInfo = new PokerGameInfo();
		guestGameInfo.setChip(30);
		doTurnStart();
	}

	public void readyGame() {
		this.master.write(Protocol.READY.name());
	}

	protected void doTurnStart() {
		logger.info("Room " + this.roomNo + "Turn Start!!");
		round++;
		if (playOrder) {
			first = this.master;
			second = this.guest;

			this.guestGameInfo.setCard(deck.nextCard());
			this.masterGameInfo.setCard(deck.nextCard());
		} else {
			first = this.guest;
			second = this.master;

			this.masterGameInfo.setCard(deck.nextCard());
			this.guestGameInfo.setCard(deck.nextCard());
		}

		initInfo();
		this.master.write(
				Protocol.SHUFFLE.name() + CommonCode.DELIMITER
						+ masterGameInfo.getCard().getFront()
						+ CommonCode.DELIMITER
						+ masterGameInfo.getCard().getBack()
						+ CommonCode.DELIMITER
						+ guestGameInfo.getCard().getFront()
						+ CommonCode.DELIMITER + deck.nextFront());

		this.guest.write(
				Protocol.SHUFFLE.name() + CommonCode.DELIMITER
						+ guestGameInfo.getCard().getFront()
						+ CommonCode.DELIMITER
						+ guestGameInfo.getCard().getBack()
						+ CommonCode.DELIMITER
						+ masterGameInfo.getCard().getFront()
						+ CommonCode.DELIMITER + deck.nextFront());

		// attackOrder = true;
		first.write(Protocol.ATTACK.name());
	}

	public void initInfo()	{
		this.masterGameInfo.setTurnChip(0);
		this.guestGameInfo.setTurnChip(0);
		
		this.masterGameInfo.addTurnChip(1);
		this.guestGameInfo.addTurnChip(1);
		
		this.masterGameInfo.setBetType("");
		this.guestGameInfo.setBetType("");
	}
	public void doBet(Client client, String betType, int chip) {
		Client otherClient;
		PokerGameInfo info, otherInfo;
		if (client == this.master) {
			otherClient = this.guest;
			info = masterGameInfo;
			otherInfo = guestGameInfo;
		} else {
			otherClient = this.master;
			info = guestGameInfo;
			otherInfo = masterGameInfo;
		}

		if (info.getBetType() == "") {
			info.setBetType(betType);
		}
		
		info.addTurnChip(chip);
		
		otherClient.write(
				Protocol.TURNINFO.name() + CommonCode.DELIMITER
						+ info.getBetType() + CommonCode.DELIMITER + chip);
		
		// CALL일경우
		if(info.getTurnChip() == otherInfo.getTurnChip())	{
			PokerGameInfo winnerInfo = getWinnerGameInfo(info, otherInfo);
			if(winnerInfo == info)	{
				logger.debug("Client win");
				sendFinish(client, otherClient, CommonCode.CALL);
			} else if(winnerInfo == otherInfo){
				logger.debug("other win");
				sendFinish(otherClient, client, CommonCode.CALL);
			} else	{
				logger.debug("draw");
				sendFinish(master, guest, CommonCode.DRAW);
			}
		} else	{
			otherClient.write(Protocol.ATTACK.name());
		}

		

	}
	
	public PokerGameInfo getWinnerGameInfo(PokerGameInfo info, PokerGameInfo otherInfo)	{
		PokerGameInfo winnerInfo;
		int cardNumber = info.getBettingCardNumber();
		int otherCardNumber = otherInfo.getBettingCardNumber();
		
		logger.debug(info.getBetType() + " cardNumber : " + cardNumber + ", infoBack : " + info.getCard().getBack() + ", infoFront : " + info.getCard().getFront());
		logger.debug(otherInfo.getBetType() + " otherNumber : " + otherCardNumber + ", otherBack : " + otherInfo.getCard().getBack() + ", toherFront : " + otherInfo.getCard().getFront());
		
		if(cardNumber < 0)	{
			info.addTurnChip(info.getTurnChip()-1);
			if(info.getCard().getBack() > otherCardNumber
					&& info.getCard().getFront() > otherCardNumber)	{
				// 양면 배팅 승리.
				otherInfo.addTurnChip(10);
				winnerInfo = info;
				logger.debug("info Dual Win");
			} else	{
				winnerInfo = otherInfo;
				logger.debug("info dual lose");
			}
		} else if(otherCardNumber < 0)	{
			otherInfo.addTurnChip(otherInfo.getTurnChip()-1);
			if(otherInfo.getCard().getBack() > cardNumber
					&& otherInfo.getCard().getFront() > cardNumber)	{
				// 양면 배팅 승리.
				info.addTurnChip(10);
				winnerInfo = otherInfo;
				logger.debug("other dual win");
			} else	{
				winnerInfo = info;
				logger.debug("other dual lose");
			}
		} else	{
			if(cardNumber < otherCardNumber)	{
				winnerInfo = otherInfo;
			} else if(otherCardNumber < cardNumber)	{
				winnerInfo = info;
			} else	{
				winnerInfo = null;
			}
		}
		
		return winnerInfo;
	}
	
	public void doDie(Client client)	{
		Client otherClient;
		PokerGameInfo info, otherInfo;
		if(client == this.master)	{	
			otherClient = this.guest;
			info = this.masterGameInfo;
			otherInfo = this.guestGameInfo;
		} else	{
			otherClient = this.master;
			info = this.guestGameInfo;
			otherInfo = this.masterGameInfo;
		}
		
		otherClient.write(
				Protocol.TURNINFO.name() + CommonCode.DELIMITER
						+ CommonCode.DIE + CommonCode.DELIMITER + 0);
		
		if(otherInfo.getBetType().equals(CommonCode.DUAL))	{
			info.addTurnChip(10);
			otherInfo.addTurnChip(otherInfo.getTurnChip()-1);
			// 상대가 양면 배팅일 때 다이한 경우 10개를 추가로 잃음.
			logger.debug("DIE DUAL1");
		} else if(info.getBetType().equals(CommonCode.DUAL))	{
			info.addTurnChip(info.getTurnChip()-1);
			logger.debug("DIE DUAL2");
			// 양면 배팅인 상태로 다이할 경우 칩 2배 잃음
		}
		
		sendFinish(otherClient, client, CommonCode.DIE);
	}
	
	/**
	 * Send the Finish Message
	 */
	public void sendFinish(Client winner, Client loser, String type)	{
		gameChip += (masterGameInfo.getTurnChip() + guestGameInfo.getTurnChip());
		if(type.equals(CommonCode.DRAW))	{
			logger.debug("master chip : " + masterGameInfo.getChip() + ", guest chip : " + guestGameInfo.getChip());
			logger.debug("DRAW");
			
			winner.write(Protocol.FINISH + CommonCode.DELIMITER
					+ type + CommonCode.DELIMITER
					+ CommonCode.DRAW + CommonCode.DELIMITER
					+ gameChip);
			
			loser.write(Protocol.FINISH + CommonCode.DELIMITER
					+ type + CommonCode.DELIMITER
					+ CommonCode.DRAW + CommonCode.DELIMITER
					+ gameChip);
			
			doTurnStart();
		} else	{
			if(winner == master)	{
				playOrder = true;
				masterGameInfo.increaseChip(gameChip);
			} else 	{
				playOrder = false;
				guestGameInfo.increaseChip(gameChip);
			}
			
			winner.write(Protocol.FINISH + CommonCode.DELIMITER
					+ type + CommonCode.DELIMITER
					+ CommonCode.WIN + CommonCode.DELIMITER
					+ gameChip);
			
			loser.write(Protocol.FINISH + CommonCode.DELIMITER
					+ type + CommonCode.DELIMITER
					+ CommonCode.LOSE + CommonCode.DELIMITER
					+ gameChip);
			
			gameChip = 0;
			logger.debug("master chip : " + masterGameInfo.getChip() + ", guest chip : " + guestGameInfo.getChip());
			
			if(masterGameInfo.getChip() <= 0
					|| guestGameInfo.getChip() <= 0)	{
				doGameEnd();
			} else	{
				doTurnStart();
			}
		}
		
	}

	

	/**
	 * Calculate winner Send the End Protocol
	 */
	protected void doGameEnd() {
		if(masterGameInfo.getChip() > guestGameInfo.getChip())	{
			master.write(Protocol.END
					+ CommonCode.DELIMITER + CommonCode.WIN);
			guest.write(Protocol.END
					+ CommonCode.DELIMITER + CommonCode.LOSE);
		} else	{
			guest.write(Protocol.END
					+ CommonCode.DELIMITER + CommonCode.WIN);
			master.write(Protocol.END
					+ CommonCode.DELIMITER + CommonCode.LOSE);
		}
	}

	public void exitRoom(Client client) {
		WaitingRoom waitingRoom = client.getWaitingRoom();
		if (client == this.master) {
			this.master = this.guest;
			if (this.master == null) {
				waitingRoom.removeGameRoom(this);
			} else {
				this.masterId = this.master.getUserId();
			}
		} else {
			this.master.write(Protocol.EXIT.name());
		}

		this.guest = null;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the masterId
	 */
	public String getMasterId() {
		return masterId;
	}

	/**
	 * @return the roomNo
	 */
	public int getRoomNo() {
		return roomNo;
	}

	/**
	 * @return the playing
	 */
	public boolean isPlaying() {
		return playing;
	}

}
