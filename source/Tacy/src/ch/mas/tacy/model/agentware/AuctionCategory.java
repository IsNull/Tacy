package ch.mas.tacy.model.agentware;

/**
 * Represents an Auction Category
 * @author P. Büttiker
 *
 */
public enum AuctionCategory {

	/**
	 * No Cat or an invalid one
	 */
	NONE(-1,"none"), 

	/**
	 * Flight cat (0)
	 */
	FLIGHT(0,"flight"),

	/**
	 * Hotel cat (1)
	 */
	HOTEL(1,"hotel"),


	/**
	 * Entertainment cat (2)
	 */
	ENTERTAINMENT(2,"entertainment");


	private final String name;

	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private AuctionCategory(int value,String name){
		Value = value;
		this.name = name;
	}



	public String getName() {
		return name;
	}


	public static AuctionCategory byName(String name){

		for (AuctionCategory cat : AuctionCategory.values()) {
			if(cat.getName().equals(name))
				return cat;
		}

		return AuctionCategory.NONE;
	}

}
