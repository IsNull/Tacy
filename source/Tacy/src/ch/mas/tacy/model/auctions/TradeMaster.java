package ch.mas.tacy.model.auctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.agentware.Auction;


/**
 * The TradeMaster is responsible to synchronize the bids, keep track of the overall amounts
 * of items, and trigger the distribute strategy to assign the owned items to the Agents.
 * 
 * @author P. Büttiker
 *
 */
public class TradeMaster {

	private static TradeMaster instance = new TradeMaster();
	public static TradeMaster instance(){
		return instance;
	}

	/** holds all item requests */
	private final Map<Auction, List<ItemRequest>> requests = new HashMap<Auction, List<ItemRequest>>();


	/**
	 * Request an item 
	 * 
	 * @param client the client requesting the item
	 * @param auctionId the auction id, representing the item type
	 * @param amount item amount
	 * @param price the agent's suggested bit price
	 */
	public void requestItem(ClientAgent client, Auction auction, int amount, float price){

	}

	public void sendBits(){

	}

	public ItemRequest findRequest(){

	}

}
