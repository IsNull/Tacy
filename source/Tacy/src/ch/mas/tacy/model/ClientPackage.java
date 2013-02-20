package ch.mas.tacy.model;

/**
 * A ClientPackage represents a full package for a single Client
 * 
 * Primary purpose of this class is to keep track of the current status of a package
 * 
 */
public class ClientPackage {

	private final int client;

	private int outFlight;
	private int inFlight;


	public ClientPackage(int client){
		this.client = client;
	}

	/**
	 * Does this package has an in flight?
	 * @return
	 */
	public boolean hasInFlight(){
		return (inFlight > 0);
	}

	/**
	 * Does this package has an out flight?
	 * @return
	 */
	public boolean hasOutFlight(){
		return (outFlight > 0);
	}

	/**
	 * Set the actual (bought) in flight day
	 * @param day
	 */
	public void setInFlight(int day){
		inFlight = day;
	}

	/**
	 * 
	 * @return
	 */
	public int getInFlight(){
		return inFlight;
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
		return outFlight;
	}

	/**
	 * Set the actual (bought) out flight day
	 * @param outFlight
	 */
	public void setOutFlight(int outFlight) {
		this.outFlight = outFlight;
	}



}
