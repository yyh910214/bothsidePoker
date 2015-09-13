/**
 * 2015. 9. 13.
 * Copyright by yyh / Hubigo AIAL
 * Deck.java
 */
package com.apolloners.poker.game;

import com.apolloners.poker.vo.Card;

public class Deck {
	private Card[] cards;
	private int index;

	public Deck() {
		cards = new Card[50];
		index = 0;
		init();
	}

	private void init() {
		int count = 0;
		for (int i = 1; i <= 10; ++i) {
			int j;
			if (i % 2 == 0) {
				j = 1;
			} else {
				j = 2;
			}

			for (; j <= 10; ++j) {
				Card card = new Card();
				card.setFront(i);
				card.setBack(j);
				cards[count++] = card;
			}
		}

		shuffle();
	}

	public void shuffle() {
		for (int i = 0; i < 50; ++i) {
			int source = (int) (Math.random() * 50);
			int target = (int) (Math.random() * 50);

			Card temp = cards[source];
			cards[source] = cards[target];
			cards[target] = temp;
		}
	}
	
	public Card nextCard()	{
		return cards[index++];
	}
	
	public int nextFront()	{
		return cards[index+1].getFront();
	}
}
