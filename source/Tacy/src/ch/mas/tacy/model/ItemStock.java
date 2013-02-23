package ch.mas.tacy.model;

import java.util.HashMap;
import java.util.Map;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.TACAgent;

/**
 * Simple implementation of a item stock
 *
 */
public class ItemStock {
	private final Object stockLock = new Object();
	private final Map<Auction, Integer> stock = new HashMap<Auction, Integer>();



	public ItemStock(){
		// init empty stock 
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);
			stock.put(auction, 0);
		}
	}


	/**
	 * Increment the stock items by the given amount
	 * @param auction
	 * @param quantity
	 */
	public void incrementQuantity(Auction auction, int quantity){
		synchronized (stockLock) {
			stock.put(auction, stock.get(auction) + quantity);
		}
	}


	/**
	 * Set the quantity of the given item type
	 * @param auction item type
	 * @param quantity
	 */
	public void setQuantity(Auction auction, int quantity){
		synchronized (stockLock) {
			stock.put(auction, quantity);
		}
	}

}
