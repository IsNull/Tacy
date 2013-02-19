package ch.mas.tacy.model.agentware;

public enum AuctionState {

	INITIALIZING(0,"Initializing"),
	INTERMEDIATE_CLEAR( 1,"Intermediate Clear"),
	FINAL_CLEAR(2,"Final Clear"),
	CLOSED(3,"Closed");

	private final String name;

	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private AuctionState(int value, String name){
		Value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static AuctionState byValue(int value){
		for (AuctionState status : AuctionState.values()) {
			if(status.Value == value)
				return status;
		}

		return AuctionState.INITIALIZING;
	}

}
