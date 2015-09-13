/**
 * 2015. 8. 26.
 * Copyright by yyh / Hubigo AIAL
 * WaitingRoom.java
 */
package com.apolloners.poker.room;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apolloners.poker.vo.Client;


public class WaitingRoom {
	
	protected static Logger logger = LoggerFactory.getLogger(WaitingRoom.class);
	
	private List<Client> waitingClients;	// 대기실 클라이언트 명단.
	private Map<Integer ,GameRoom> gameRooms;
	
	private int maxPerson = 20;
	private int maxRoom = 10;
	
	public WaitingRoom()	{
		this.waitingClients = Collections.synchronizedList(new LinkedList<Client>());
		this.gameRooms = Collections.synchronizedMap(new HashMap<Integer, GameRoom>());
	}

	/**
	 * To be considerate synchronized
	 */
	public synchronized int enterRoom(Client client) {
		if(waitingClients.size() >= maxPerson)	{
			return -1;
		}
		waitingClients.add(client);
		return 1;
	}
	
	public GameRoom getGameRoom(int roomNo)	{
		return this.gameRooms.get(roomNo);
	}
	
	
	public synchronized GameRoom createGameRoom(String title, Client master)	{
		if(gameRooms.size() > maxRoom)	{
			return null;
		}
		GameRoom gameRoom = new GameRoom(title, master);
		gameRooms.put(gameRoom.getRoomNo(), gameRoom);
		
		return gameRoom;
	}
	
	public void removeGameRoom(GameRoom gameRoom)	{
		logger.info(gameRoom.getRoomNo() + "th room removed.");
		gameRooms.remove(gameRoom.getRoomNo());
	}
	
	protected JSONArray getRoomListJson()	{
		JSONArray roomArray = new JSONArray();
		JSONObject jsonObject;
		
		Iterator<Map.Entry<Integer, GameRoom>> iter = this.gameRooms.entrySet().iterator();
		while(iter.hasNext())	{
			jsonObject = new JSONObject();
			GameRoom room = iter.next().getValue();
			
			jsonObject.put("no", room.getRoomNo());
			jsonObject.put("title", room.getTitle());
			jsonObject.put("masterId", room.getMasterId());
			
			roomArray.add(jsonObject);
		}
		
		return roomArray;
	}
	
	protected JSONArray getWaitingListJson()	{
		JSONArray clientArray = new JSONArray();

		for(Client client : waitingClients)	{
			clientArray.add(client.getUserId());
		}
		
		return clientArray;
	}
	
	public String getRefreshJsonString()	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("users", getWaitingListJson());
		jsonObject.put("rooms", getRoomListJson());
		
		return jsonObject.toJSONString();
	}

	/**
	 * 대기실 나가기
	 */
	public void exitRoom(Client client) {
		waitingClients.remove(client);
	}

	public int getNumberOfPerson()	{
		return waitingClients.size();
	}
	
	public boolean isFull()	{
		return getNumberOfPerson() >= maxPerson;
	}
}
