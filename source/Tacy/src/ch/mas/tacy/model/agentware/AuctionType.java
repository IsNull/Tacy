package ch.mas.tacy.model.agentware;
/**
 * Represents an AuctionType
 * 
 * @author P. Büttiker
 *
 */
public enum AuctionType {

	/** None or invalid Auction type*/
	None(-1),


	/** in flight */
	INFLIGHT(1),
	/** out flight */
	OUTFLIGHT(0),

	/** TT Towers*/
	GOOD_HOTEL(1),

	/** SS Towers*/
	CHEAP_HOTEL(0),

	EVENT_ALLIGATOR_WRESTLING(1),
	EVENT_AMUSEMENT(2),
	EVENT_MUSEUM(3);




	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private AuctionType(int value){
		Value = value;
	}



	public static AuctionType byValue(int value){
		for (AuctionType status : AuctionType.values()) {
			if(status.Value == value)
				return status;
		}

		return AuctionType.None;
	}
}
