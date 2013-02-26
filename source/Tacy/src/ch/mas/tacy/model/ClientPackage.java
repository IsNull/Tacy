package ch.mas.tacy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.model.agentware.AuctionType;

/**
 * A ClientPackage represents a full package for a single Client
 * 
 * Primary purpose of this class is to keep track of the current status of a package
 * (what does the client want and what does the client have?)
 * 
 */
public class ClientPackage {

	private final int client;

	private int preferredInFlight;
	private int preferredOutFlight;
	private int pvHotel;
	private int premiumValueAlligatorWrestling;
	private int premiumValueAmusementPark;
	private int premiumValuevMuseum;


	/** first vaule represents day, second determines which type of hotel it is*/
	private final Map<Integer, AuctionType> actualHotelRoomsTypes = new HashMap<Integer, AuctionType>();

	/** first value represents day, second if a corresponding event has been allocated or not (either EVENT_ALLIGATOR_WRESTLING, EVENT_AMUSEMENT, EVENT_MUSEUM or None */
	private final Map<Integer, AuctionType> actualEvents = new HashMap<Integer, AuctionType>();
	private int actualInFlight;
	private int actualOutFlight;





	public ClientPackage(int client){
		this.client = client;
	}

	/**
	 * calculates on which days overnight stays and events has to be placed in order to make a feasible package
	 */
	private void ensurePresenceDays(){
		for(int i=preferredInFlight; i<preferredOutFlight; i++){
			actualHotelRoomsTypes.put(i, AuctionType.None);
			actualEvents.put(i, AuctionType.None);
		}
	}

	/**
	 * returns true if there is already a hotel room for the given day
	 * @param day
	 * @return
	 */
	public boolean hasHotel(int day){
		return  actualHotelRoomsTypes.containsKey(day) ? (actualHotelRoomsTypes.get(day) != AuctionType.None) : false;	
	}

	/**
	 * returns true if an event is already allocated on the given day
	 * @param day
	 * @return
	 */
	public boolean hasEventAt(int day){
		return actualEvents.containsKey(day) ? (actualEvents.get(day) != AuctionType.None) : false;
	}

	/**
	 * returns true if the given type already exists in the package
	 * @param type
	 * @return
	 */
	public boolean hasSameEvent(AuctionType type){
		return (actualEvents.containsValue(type));
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
	}

	/**
	 * Is the given day one of the trip
	 * @param day
	 * @return
	 */
	public boolean isPresenceDay(int day){
		return actualHotelRoomsTypes.containsKey(day);
	}

	public void setActualHotelRooms(int day, AuctionType type){
		actualHotelRoomsTypes.put(day, type);
	}

	public AuctionType getCurrenHotelType(){
		if(actualHotelRoomsTypes.containsValue(AuctionType.GOOD_HOTEL)){
			return AuctionType.GOOD_HOTEL;
		}else if(actualHotelRoomsTypes.containsValue(AuctionType.CHEAP_HOTEL)){
			return AuctionType.CHEAP_HOTEL;
		} else {
			return AuctionType.None;
		}
	}

	/**
	 * returns on which days hotel rooms are still needed
	 * @return
	 */
	public List<Integer> getNeedForHotelDays(){

		List<Integer> missingDays = new ArrayList<Integer>();

		for(Integer day : actualHotelRoomsTypes.keySet()){
			if(actualHotelRoomsTypes.get(day) == AuctionType.None){
				missingDays.add(day);
			}
		}

		return missingDays;
	}
	
	/**
	 * return on which days entertainment events are still needed
	 * @return
	 */
	public List<Integer> getNeedForEvents(){

		List<Integer> missingDays = new ArrayList<Integer>();

		for(Integer day : actualEvents.keySet()){
			if(actualHotelRoomsTypes.get(day) == AuctionType.None){
				missingDays.add(day);
			}
		}

		return missingDays;
	}

	public boolean hasAtLeastOneHotelRoom(){
		return (actualHotelRoomsTypes.size() > 0);
	}


	public void setEvent(int day, AuctionType type){
		actualEvents.put(day, type);
	}


	public int getPreferredOutFlight() {
		return preferredOutFlight;
	}

	public void setPreferredOutFlight(int preferrefOutFlight) {
		this.preferredOutFlight = preferrefOutFlight;
		ensurePresenceDays();
	}

	public int getPreferredInFlight() {
		return preferredInFlight;
	}

	public void setPreferredInFlight(int preferredInFlight) {
		this.preferredInFlight = preferredInFlight;
		ensurePresenceDays();
	}

	public int getPvHotel() {
		return pvHotel;
	}

	public void setPvHotel(int pvHotel) {
		this.pvHotel = pvHotel;
	}

	public int getPremiumValueAlligatorWrestling() {
		return premiumValueAlligatorWrestling;
	}

	public void setPremiumValueAlligatorWrestling(int pvAV) {
		this.premiumValueAlligatorWrestling = pvAV;
	}

	public int getPremiumValueAmusementPark() {
		return premiumValueAmusementPark;
	}

	public void setPremiumValueAmusementPark(int pvAP) {
		this.premiumValueAmusementPark = pvAP;
	}

	public int getPremiumValuevMuseum() {
		return premiumValuevMuseum;
	}

	public void setPremiumValuevMuseum(int pvMU) {
		this.premiumValuevMuseum = pvMU;
	}

	/**
	 * returns the number of days of the whole trip
	 * @return
	 */
	public int getTripDuration() {
		return getPresenceDuration() + 1;
	}

	/**
	 * return the number of days on which the client needs hotels and can join events
	 * @return
	 */
	public int getPresenceDuration(){
		return (actualHotelRoomsTypes != null) ? actualHotelRoomsTypes.size() : 0;
	}
	
	/**
	 * returns true if the package has an in- and outflight as well as rooms for every night between this dates
	 * @return
	 */
	public boolean isTravelFeasible(){
		
		boolean feasible = false;
		
		//check if package contains in and outflight
		if(this.hasInFlight() && this.hasOutFlight()){
			
			//check if there is no need for hotel rooms anymore
			feasible = this.getNeedForHotelDays().size() == 0;
			
		}
		
		return feasible;
	}

}
