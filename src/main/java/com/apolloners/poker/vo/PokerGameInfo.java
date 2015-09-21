/**
 * 2015. 9. 14.
 * Copyright by yyh / Hubigo AIAL
 * PokerGameInfo.java
 */
package com.apolloners.poker.vo;

import com.apolloners.poker.common.CommonCode;

public class PokerGameInfo {
	private Card card;
	private int chip;
	private int turnChip; // 매 턴 배팅하는 칩
	private String betType;
	/**
	 * @return the card
	 */
	public Card getCard() {
		return card;
	}
	/**
	 * @param card the card to set
	 */
	public void setCard(Card card) {
		this.card = card;
	}
	/**
	 * @return the chip
	 */
	public int getChip() {
		return chip;
	}
	/**
	 * @param chip the chip to set
	 */
	public void setChip(int chip) {
		this.chip = chip;
	}
	/**
	 * @return the betType
	 */
	public String getBetType() {
		return betType;
	}
	/**
	 * @param betType the betType to set
	 */
	public void setBetType(String betType) {
		this.betType = betType;
	}
	
	public void decreaseChip(int bettingChip)	{
		this.chip -= bettingChip;
	}
	
	public void increaseChip(int bettingChip)	{
		this.chip += bettingChip;
	}
	/**
	 * @return the turnChip
	 */
	public int getTurnChip() {
		return turnChip;
	}
	/**
	 * @param turnChip the turnChip to set
	 */
	public void setTurnChip(int turnChip) {
		this.turnChip = turnChip;
	}
	
	public void addTurnChip(int turnChip)	{
		this.turnChip += turnChip;
		this.decreaseChip(turnChip);
	}
	
	/**
	 * 배팅 면의 숫자를 알려줌.
	 * DUAL일 경우 -1 리턴
	 */
	public int getBettingCardNumber()	{
		if(betType == CommonCode.BACK)	{
			return card.getBack();
		} else if(betType == CommonCode.FRONT)	{
			return card.getFront();
		} else	{
			return -1;
		}
	}
	
}
