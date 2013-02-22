package ch.mas.tacy.model.auctions;

import java.util.ArrayList;
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

	/** holds all items we own */
	private final Map<Auction, Integer> stockOverview = new HashMap<Auction, Integer>();

	/** holds all item which are currently not assigned to a sub agent */
	private final Map<Auction, Integer> avaiableItems = new HashMap<Auction, Integer>();





	/**
	 * Request an item 
	 * 
	 * @param client the client requesting the item
	 * @param auctionId the auction id, representing the item type
	 * @param amount item amount
	 * @param price the agent's suggested bit price
	 */
	public void requestItem(ClientAgent client, Auction auction, int amount, float price){
		assert client != null : "client can not be null";
		assert auction != null : "auction can not be null";

		ItemRequest request = findRequest(auction, client);

		if(request == null)
		{
			request = new ItemRequest(client, auction, amount, price);
			placeRequest(request);
		}else{
			//update existing request
			request.setAmount(amount);
			request.setPrice(price);
			onRequestUpdated(request);
		}
	}


	public void sendBits(){
		//TODO
	}

	protected void placeRequest(ItemRequest request) {
		if(!requests.containsKey(request.getAuction())){
			requests.put(request.getAuction(), new ArrayList<ItemRequest>());
		}
		requests.get(request.getAuction()).add(request);
		onRequestAdded(request);
	}

	/**
	 * Searches for the given request by the auction and client
	 * returns the found request or null, if no match could be found
	 * @param auction
	 * @param client
	 * @return
	 */
	public ItemRequest findRequest(Auction auction, ClientAgent client){
		if(requests.containsKey(auction)){
			for (ItemRequest request : requests.get(auction)) {
				if(request.getOriginator().equals(client))
					return request;
			}
		}
		return null;
	}

	protected void onRequestUpdated(ItemRequest request) {

	}  

	protected void onRequestAdded(ItemRequest request) {

	}  


}
