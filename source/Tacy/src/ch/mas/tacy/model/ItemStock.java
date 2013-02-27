package ch.mas.tacy.model;

import java.util.HashMap;
import java.util.Map;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.TACAgent;

/**
 * Simple implementation of a item stock
 *
 */
public class ItemStock<T> {
	protected final Object stockLock = new Object();
	protected final Map<T, Integer> stock = new HashMap<T, Integer>();


	/**
	 * Increment the stock items by the given amount
	 * @param itemType
	 * @param quantity
	 */
	public void incrementQuantity(T itemType, int quantity){
		synchronized (stockLock) {
			int currentQuantity = stock.containsKey(itemType) ? stock.get(itemType) : 0;
			stock.put(itemType, currentQuantity + quantity);
		}
	}


	/**
	 * Set the quantity of the given item type in this stock
	 * @param itemType item type
	 * @param quantity
	 */
	public void setQuantity(T itemType, int quantity){
		synchronized (stockLock) {
			stock.put(itemType, quantity);
		}
	}


	/**
	 * Returns the quantity of the given item type in this stock
	 * @param itemType
	 * @return
	 */
	public int getQuantity(T itemType) {
		synchronized (stockLock) {
			return stock.containsKey(itemType) ? stock.get(itemType) : 0;
		}
	}

}
