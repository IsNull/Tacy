package ch.mas.tacy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionType;
import ch.mas.tacy.model.agentware.TACAgent;

/**
 * A ClientPackage represents a full package for a single Client
 *
 * Primary purpose of this class is to keep track of the current status of a package
 * (what does the client want and what does the client have?)
 * 
 */
public class ClientPackage {

	//represents the client to whom this ClientPackage is assigned to
	private final int client;


	/** first vaule represents day, second determines which type of hotel it is*/
	//private final Map<Integer, AuctionType> actualHotelRoomsTypes = new HashMap<Integer, AuctionType>();

	/** first value represents day, second if a corresponding event has been allocated or not (either EVENT_ALLIGATOR_WRESTLING, EVENT_AMUSEMENT, EVENT_MUSEUM or None */
	//private final Map<Integer, AuctionType> actualEvents = new HashMap<Integer, AuctionType>();

	private int actualInFlight;
	private int actualOutFlight;

	private ItemStockAuction itemStockAuction;


	public ClientPackage(int client){
		this.client = client;
		this.itemStockAuction = new ItemStockAuction();
	}

	/**
	 * calculates on which days overnight stays and events has to be placed in order to make a feasible package
	 */
	/*
	private void ensurePresenceDays(){
		for(int i=preferredInFlight; i<preferredOutFlight; i++){
			actualHotelRoomsTypes.put(i, AuctionType.None);
			actualEvents.put(i, AuctionType.None);
		}
	}
	 */

	public void addItem(Auction auction, int quantity){
		itemStockAuction.setQuantity(auction, quantity);
	}

	/**
	 * returns true if there is already a hotel room for the given day
	 * @param day
	 * @return
	 */
	public boolean hasHotelAt(int day){
		return  itemStockAuction.getQuantityByCategory(AuctionCategory.HOTEL, day) > 0;	
	}

	/**
	 * returns true if an event is already allocated on the given day
	 * @param day
	 * @return
	 */
	public boolean hasEventAt(int day){
		return itemStockAuction.getQuantityByCategory(AuctionCategory.ENTERTAINMENT, day) > 0;	
	}

	/**
	 * returns true if the given type already exists in the package
	 * @param type
	 * @return
	 */
	public boolean hasSameEvent(Auction auction){
		return (itemStockAuction.getQuantity(auction) > 0);
	}

	/**
	 * Does this package has an in flight?
	 * @return
	 */
	public boolean hasInFlight(){
		return (actualInFlight > 0);
	}

	/**
	 * Does this package has an out flight?
	 * @return
	 */
	public boolean hasOutFlight(){
		return (actualOutFlight > 0);
	}

	/**
	 * Set the actual (bought) in flight day
	 * @param day
	 */
	public void setInFlight(int day){
		actualInFlight = day;
		System.out.println("clientpackage: inflight set on day"+day);
	}

	/**
	 * Get the actual (bought) in flight day if any
	 * @return
	 */
	public int getInFlight(){
		return actualInFlight;
	}

	/**
	 * Gets the client id to which this package is assigned
	 * @return
	 */
	public int getClient() {
		return client;
	}

	/**
	 * Get the actual (bought) out flight day if any
	 * @return
	 */
	public int getOutFlight() {
		return actualOutFlight;
	}

	/**
	 * Set the actual (bought) out flight day
	 * @param outFlight
	 */
	public void setOutFlight(int outFlight) {
		this.actualOutFlight = outFlight;
		System.out.println("clientpackage: outflight set on day"+outFlight);
	}


/*
	public void setActualHotelRooms(int day, AuctionType type){
		actualHotelRoomsTypes.put(day, type);
		System.out.println("clientpackage: hotel room set on day"+day);
	}
	*/

	/**
	 * return which type of hotel is currently in the itemstock stored
	 * @return
	 */
	public AuctionType getCurrenHotelType(){
		if(itemStockAuction.containsItemsOfGivenAuctionType(AuctionType.GOOD_HOTEL)){
			return AuctionType.GOOD_HOTEL;
		}else if(itemStockAuction.containsItemsOfGivenAuctionType(AuctionType.CHEAP_HOTEL)){
			return AuctionType.CHEAP_HOTEL;
		} else {
			return AuctionType.None;
		}
	}

	/**
	 * returns on which days hotel rooms are still needed
	 * @return
	 */
	public List<Integer> getNeedForHotelDays(ClientPreferences clientPreferences){

		List<Integer> missingDays = new ArrayList<Integer>();

		for(Integer day : clientPreferences.getPresenceDays()){
			
			if(itemStockAuction.getQuantityByCategory(AuctionCategory.HOTEL, day) <= 0){
				missingDays.add(day);
			}
		}

		return missingDays;
	}

	/**
	 * return on which days entertainment events are still needed
	 * @return
	 */
	public List<Integer> getNeedForEvents(ClientPreferences clientPreferences){

		List<Integer> missingDays = new ArrayList<Integer>();

		for(Integer day : clientPreferences.getPresenceDays()){

			if(itemStockAuction.getQuantityByCategory(AuctionCategory.ENTERTAINMENT, day) <= 0){
				missingDays.add(day);
			}
		}

		return missingDays;
	}

	public boolean hasAtLeastOneItemOf(AuctionType type){
		return (itemStockAuction.containsItemsOfGivenAuctionType(type));
	}

	/**
	 * returns true if the package has an in- and outflight as well as rooms for every night between this dates
	 * @return
	 */
	public boolean isTravelFeasible(ClientPreferences clientPreferences){
		
		boolean feasible = false;
		
		//check if package contains in and outflight
		if(this.hasInFlight() && this.hasOutFlight()){
			
			//check if there is no need for hotel rooms anymore
			feasible = this.getNeedForHotelDays(clientPreferences).size() == 0;
			
		}
		
		return feasible;
	}



}
