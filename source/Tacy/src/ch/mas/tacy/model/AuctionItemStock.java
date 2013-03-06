package ch.mas.tacy.model;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionType;


public class AuctionItemStock extends ItemStock<Auction> {

	/**
	 * returns the quantity of the given category on the given day
	 * @param category
	 * @param day
	 * @return
	 */
	public int getQuantityByCategory(AuctionCategory category, int day){

		synchronized (stockLock) {

			int quantity = 0;

			for (Auction auction : stock.keySet()) {
				if(auction.getCategory() == category && auction.getAuctionDay() == day){
					quantity += stock.get(auction);
				}
			}
			return quantity;
		}
	}

	/**
	 * returns the quantity of the given auction type on the given day
	 * @param type
	 * @param day
	 * @return
	 */
	public int getQuantityByAuctionType(AuctionType type, int day){

		synchronized (stockLock) {

			int quantity = 0;

			for (Auction auction : stock.keySet()) {
				if(auction.getType() == type && auction.getAuctionDay() == day){
					quantity += stock.get(auction);
				}
			}
			return quantity;
		}
	}

	/**
	 * returns true if there is at least one item of the given type
	 * @param type
	 * @return
	 */
	public boolean containsItemsOfGivenAuctionType(AuctionType type){

		synchronized (stockLock) {

			int quantity = 0;

			for (Auction auction : stock.keySet()) {
				if(auction.getType() == type){
					quantity += stock.get(auction);
				}
			}
			return quantity > 0;
		}
	}



}
