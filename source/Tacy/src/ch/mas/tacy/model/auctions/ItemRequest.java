package ch.mas.tacy.model.auctions;

import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.agentware.Auction;

/**
 * represents an item request from a client
 * @author IsNull
 *
 */
public class ItemRequest {

	private ClientAgent originator;
	private Auction auction;
	private int amount; 
	private float price;


	public ItemRequest(ClientAgent originator, Auction auction, int amount, float price) {
		super();
		this.originator = originator;
		this.auction = auction;
		this.amount = amount;
		this.price = price;
	}


	public ClientAgent getOriginator() {
		return originator;
	}

	public Auction getAuction() {
		return auction;
	}


	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}


}
