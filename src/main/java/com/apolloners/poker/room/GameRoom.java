/**
 * 2015. 8. 26.
 * Copyright by yyh / Hubigo AIAL
 * GameRoom.java
 */
package com.apolloners.poker.room;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apolloners.poker.common.CommonCode;
import com.apolloners.poker.common.Protocol;
import com.apolloners.poker.game.Deck;
import com.apolloners.poker.vo.Card;
import com.apolloners.poker.vo.Client;

public class GameRoom {
	
	protected static final Logger logger = LoggerFactory.getLogger(GameRoom.class);
	
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
	
	private Deck deck;
	
	private boolean playing;
	private boolean playOrder;	// true : master first, false : guest first
	private boolean attackOrder;	// true : Attack, false : Defense

	private Card firstCard, secondCard;	// Turn point of the clients
	private int masterWin, guestWin;	// Number of win round
	
	private List<Integer> masterPoints, guestPoints;
	
	private int round;
	
	public GameRoom(String title, Client master)	{
		this.title = title;
		this.master = master;
		this.guest = null;
		this.deck = new Deck();
		this.roomNo = roomIndex.getAndIncrement();
		masterId = master.getUserId();
	}
	/* (non-Javadoc)
	 * @see genius.room.Room#enterRoom(genius.client.Client)
	 */
	public int enterRoom(Client client) {
		if(this.master == null)	{
			this.master = client;
		} else	{
			this.guest = client;
			this.master.getChannel().writeAndFlush(Protocol.JOIN + CommonCode.DELIMITER + CommonCode.ID + 
					CommonCode.DELIMITER + this.guest.getUserId());
		}
		return 1;
	}
	
	public void startGame()	{
		logger.info("Start Game Room No." + this.roomNo);
		this.playing = true;
		this.guest.getChannel().writeAndFlush(Protocol.START.name());
		this.master.getChannel().writeAndFlush(Protocol.START.name());
		
		this.playOrder = (Math.random() < 0.5);

		this.round = 0;
		this.masterPoints = new ArrayList<Integer>();
		this.guestPoints = new ArrayList<Integer>();
		doTurnStart();
	}
	
	public void readyGame()	{
		this.master.getChannel().writeAndFlush(Protocol.READY.name());
	}
	
	protected void doTurnStart()	{
		round++;
		if(playOrder)	{
			first = this.master;
			second = this.guest;
		} else	{
			first = this.guest;
			second = this.master;
		}
		
		this.firstCard = deck.nextCard();
		this.secondCard = deck.nextCard();
		
		this.first.getChannel().writeAndFlush(Protocol.SHUFFLE.name()
				+ CommonCode.DELIMITER + firstCard.getFront()
				+ CommonCode.DELIMITER + firstCard.getBack()
				+ CommonCode.DELIMITER + secondCard.getFront()
				+ CommonCode.DELIMITER + deck.nextFront());
		
		this.second.getChannel().writeAndFlush(Protocol.SHUFFLE.name()
				+ CommonCode.DELIMITER + firstCard.getFront()
				+ CommonCode.DELIMITER + firstCard.getBack()
				+ CommonCode.DELIMITER + secondCard.getFront()
				+ CommonCode.DELIMITER + deck.nextFront());
		
		//attackOrder = true;
		first.getChannel().writeAndFlush(Protocol.ATTACK.name());
	}
	
	public void bet(Client client, String betType, int chip)	{
		Client otherClient;
		if(client == this.master)	{
			otherClient = this.guest;
		} else	{
			otherClient = this.master;
		}
	}
	
	
	/**
	 * Progress Input Data
	 * Reduce the point from a client.
	 */
	public void doInput(Client client, String[] messages)	{
		int point = Integer.parseInt(pointString);
		
		if(master == client)	{
			this.masterPoints.add(point);
		} else if(guest == client)	{
			this.guestPoints.add(point);
		}
		
		if(attackOrder)	{	// attack Protocol case
			firstPoint = point;
			second.write(Protocol.DEFENSE.name() + CommonCode.DELIMITER + pointString);
			attackOrder = false;
			
			logger.debug(first.getUserId() + "'s Attack point is " + firstPoint);
			// 점수 기록 필요함.
		} else	{	// Defense Protocol case
			secondPoint = point;
			StringBuilder firstMsg = new StringBuilder();
			StringBuilder secondMsg = new StringBuilder();
			firstMsg.append(Protocol.FINISH).append(CommonCode.DELIMITER);
			secondMsg.append(Protocol.FINISH).append(CommonCode.DELIMITER);
			
			logger.debug(second.getUserId() + "'s Defense point is " + secondPoint);		 
			
			if(secondPoint < firstPoint)	{
				firstMsg.append(CommonCode.WIN);
				secondMsg.append(CommonCode.LOSE);
				if(playOrder)	{
					this.masterWin++;
				} else	{
					this.guestWin++;
				}
			} else if(secondPoint > firstPoint)	{
				firstMsg.append(CommonCode.LOSE);
				secondMsg.append(CommonCode.WIN);
				
				if(playOrder)	{
					this.guestWin++;
				} else	{
					this.masterWin++;
				}
				
				playOrder = !playOrder;	// change the play order
			} else	{
				firstMsg.append(CommonCode.DRAW);
				secondMsg.append(CommonCode.DRAW);
				
				playOrder = (Math.random() < 0.5);	// If the game result is draw, get random order.
			}
			
			firstMsg.append(CommonCode.DELIMITER).append(secondPoint);
			secondMsg.append(CommonCode.DELIMITER).append(firstPoint);
			
			first.write(firstMsg.toString());
			second.write(secondMsg.toString());

			if(round == MAX_ROUND ||
					masterWin == 5 || guestWin == 5)	{
				doGameEnd();
			} else	{
				doTurnStart();	
			}
		}
	}
	
	/**
	 * Calculate winner
	 * Send the End Protocol
	 */
	protected void doGameEnd()	{
		if(masterWin > guestWin)	{
			master.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.WIN);
			guest.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.LOSE);
		} else if(guestWin > masterWin)	{
			master.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.LOSE);
			guest.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.WIN);
		} else	{
			master.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.DRAW);
			guest.write(Protocol.END.name() + CommonCode.DELIMITER + CommonCode.DRAW);
		}
		
	}

	public void exitRoom(Client client) {
		WaitingRoom waitingRoom = client.getWaitingRoom();
		if(client == this.master)	{
			this.master = this.guest;
			if(this.master == null)	{
				waitingRoom.removeGameRoom(this);
			} else	{
				this.masterId = this.master.getUserId();
			}
		} else	{
			this.master.getChannel().writeAndFlush(Protocol.EXIT);
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
