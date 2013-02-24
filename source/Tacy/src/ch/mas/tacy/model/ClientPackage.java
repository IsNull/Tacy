package ch.mas.tacy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	
	private HashMap<Integer, Integer> actualOvernightStays;	//first value represents day, second if a hotel stay has been allocated (either 1 or 0)
	private int actualInFlight;
	private int actualOutFlight;
	
	


	public ClientPackage(int client){
		this.client = client;
	}

	/**
	 * calculates on which days overnight stays has to be placed in order to make a feasible package
	 */
	public void calculateOvernightStays(){
		for(int i=preferredInFlight; i<preferredOutFlight; i++){
			actualOvernightStays.put(i, 0);
		}
	}
	
	/**
	 * returns true if there is already a hotel room for the given day
	 * @param day
	 * @return
	 */
	public boolean hasHotel(int day){
		return (actualOvernightStays.get(day) == 1);		
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
	 * Returns a list containing the required days for spending nights in order to make package feasible
	 * @return
	 */
	public List<Integer> getOvernightDays(){
		List<Integer> overnightdays = new ArrayList<>();
		
		for(int i=preferredInFlight; i<preferredOutFlight; i++){
			overnightdays.add(i);
		}
		
		return overnightdays;
	}
	

	public int getPreferredOutFlight() {
		return preferredOutFlight;
	}

	public void setPreferredOutFlight(int preferrefOutFlight) {
		this.preferredOutFlight = preferrefOutFlight;
	}

	public int getPreferredInFlight() {
		return preferredInFlight;
	}

	public void setPreferredInFlight(int preferredInFlight) {
		this.preferredInFlight = preferredInFlight;
	}

}
