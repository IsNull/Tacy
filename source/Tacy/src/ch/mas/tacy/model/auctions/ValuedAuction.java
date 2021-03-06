package ch.mas.tacy.model.auctions;

import ch.mas.tacy.model.agentware.Auction;


public class ValuedAuction implements Comparable<ValuedAuction> {

	private final Auction auction;
	private final float value;

	public ValuedAuction(Auction auction, float value){
		this.auction = auction;
		this.value = value;
	}


	@Override
	public int compareTo(ValuedAuction other) {
		return Double.compare(value, other.getValue());
	}

	public float getValue() {
		return value;
	}


	public Auction getAuction() {
		return auction;
	}

}
