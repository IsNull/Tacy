package ch.mas.tacy.model;

import java.util.List;
import java.util.ArrayList;


/**
 * This class represents a clients preferences.
 * @author n0daft
 *
 */
public class ClientPreferences {

	//represents the client to whom this ClientPreferences are assigned to
	private final int client;
	
	//the clients preferences
	private int preferredInFlight;
	private int preferredOutFlight;
	private int premiumValueHotel;
	private int premiumValueAlligatorWrestling;
	private int premiumValueAmusementPark;
	private int premiumValuevMuseum;
	
	//further calculated information
	private List<Integer> presenceDays = new ArrayList<Integer>();
	
	
	public ClientPreferences(int client){
		this.client = client;
	}
	
	public int getPreferredInFlight() {
		return preferredInFlight;
	}
	
	public void setPreferredInFlight(int preferredInFlight) {
		this.preferredInFlight = preferredInFlight;
		calculatePresenceDays();
	}
	
	public int getPreferredOutFlight() {
		return preferredOutFlight;
	}
	
	public void setPreferredOutFlight(int preferrefOutFlight) {
		this.preferredOutFlight = preferrefOutFlight;
		calculatePresenceDays();
	}

	public int getPremiumValueHotel() {
		return premiumValueHotel;
	}

	public void setPremiumValueHotel(int premiumValueHotel) {
		this.premiumValueHotel = premiumValueHotel;
	}

	public int getPremiumValueAlligatorWrestling() {
		return premiumValueAlligatorWrestling;
	}

	public void setPremiumValueAlligatorWrestling(
			int premiumValueAlligatorWrestling) {
		this.premiumValueAlligatorWrestling = premiumValueAlligatorWrestling;
	}

	public int getPremiumValueAmusementPark() {
		return premiumValueAmusementPark;
	}

	public void setPremiumValueAmusementPark(int premiumValueAmusementPark) {
		this.premiumValueAmusementPark = premiumValueAmusementPark;
	}

	public int getPremiumValuevMuseum() {
		return premiumValuevMuseum;
	}

	public void setPremiumValuevMuseum(int premiumValuevMuseum) {
		this.premiumValuevMuseum = premiumValuevMuseum;
	}
	
	
	
	
	/**
	 * calculates on which days the client will be present (based on preferred in and outflight)
	 */
	private void calculatePresenceDays(){
		presenceDays.clear();
		
		for(int i=preferredInFlight; i<preferredOutFlight; i++){
			presenceDays.add(i);
		}
	}
	
	/**
	 * return the the clients presence days
	 * @return
	 */
	public List<Integer> getPresenceDays() {
		return presenceDays;
	}
	
	/**
	 * return the number of the clients presence days
	 * @return
	 */
	public int getPresenceDuration(){
		return presenceDays.size();
	}
	
	/**
	 * returns the number of days of the whole trip
	 * @return
	 */
	public int getTripDuration() {
		return presenceDays.size() + 1;
	}
	
	
	/**
	 * returns true if the client is present on the given day
	 * @param day
	 * @return
	 */
	public boolean isPresenceDay(int day){
		return presenceDays.contains(day);
	}
	
	public String toString(){
		return "prefInFlight: "+preferredInFlight+" prefOutFlight: "+preferredOutFlight+
				" pvHotel: "+premiumValueHotel+" pvAW: "+premiumValueAlligatorWrestling+
				" pvAP: "+premiumValueAmusementPark+" pvMU: "+premiumValuevMuseum;
		
	}
	
	
	
	

	
	
}
