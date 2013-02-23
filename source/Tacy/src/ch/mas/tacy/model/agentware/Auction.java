package ch.mas.tacy.model.agentware;


public class Auction {

	private int auctionId;
	private AuctionState state;


	public int getId() {
		return auctionId;
	}


	public Auction(int auctionId){
		this.auctionId = auctionId;
		this.state = AuctionState.INITIALIZING;
	}
	
	public AuctionState getState() {
		return state;
	}


	public void setState(AuctionState state) {
		this.state = state;
	}


	@Override
	public int hashCode(){
		return auctionId;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Auction){
			return ((Auction)o).getId() == this.getId();
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public AuctionCategory getCategory(){
		if (auctionId < 8) {
			return AuctionCategory.FLIGHT;
		} else if (auctionId < 16) {
			return AuctionCategory.HOTEL;
		}
		return AuctionCategory.ENTERTAINMENT;
	}

	/**
	 * Returns the day of the auction in the range 1 - 5
	 * @param auction
	 * @return
	 */
	public int getAuctionDay() {
		int day = (auctionId % 4) + 1;
		if ((auctionId / 4) == 1) {
			// Outflights are specified as day 2 to day 5
			day++;
		}
		return day;
	}

	/**
	 * Returns the type of the auction.  Please note that
	 * auctions in different categories might have the
	 * same value as type.
	 * 
	 * returns the type for this auction (TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc).
	 * @param auction
	 * @return
	 */
	public AuctionType getType() {
		int type = auctionId / 4;
		switch (type) {
		case 0: return AuctionType.INFLIGHT;
		case 1: return AuctionType.OUTFLIGHT;
		case 2: return AuctionType.CHEAP_HOTEL;
		case 3: return AuctionType.GOOD_HOTEL;
		case 4: return AuctionType.EVENT_ALLIGATOR_WRESTLING;
		case 5: return AuctionType.EVENT_AMUSEMENT;
		default: return AuctionType.EVENT_MUSEUM;
		}
	}





}
