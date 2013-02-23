package ch.mas.tacy.model.auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.ItemStock;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.agentware.Transaction;


/**
 * The TradeMaster is responsible to synchronize the bids, keep track of the overall amounts
 * of items, and trigger the distribute strategy to assign the owned items to the Agents.
 * 
 * A schedule looks like this:
 * 
 * 1. Let the ClientAgents request some items
 * --> expect a pulse afterwards
 * 2. Analyze the requests and compare with the available items
 * 3. Create Bids for the missing items, according to the prices of the ClientAgents
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
	private final ItemStock stock = new ItemStock();

	/** holds all item which are currently not assigned to a sub agent */
	private final ItemStock avaiableItems = new ItemStock();


	private TradeMaster(){

		// init stock and avaiable item store
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);
			stock.setQuantity(auction, 0);
			avaiableItems.setQuantity(auction, 0);
		}
	}



	/**
	 * Updates the given item(auction) requested amount and requested price
	 * 
	 * @param client the client requesting the item
	 * @param auctionId the auction id, representing the item type
	 * @param amount item amount
	 * @param price the agent's suggested bit price
	 */
	public void updateRequestedItem(ClientAgent client, Auction auction, int amount, float price){
		assert client != null : "client can not be null";
		assert auction != null : "auction can not be null";

		ItemRequest request = findRequest(auction, client);

		if(request == null)
		{
			request = new ItemRequest(client, auction, amount, price);
			placeRequest(request);
		}else{
			// update existing request
			request.setAmount(amount);
			request.setPrice(price);
			onRequestUpdated(request);
		}
	}


	/**
	 * Occurs when the TradeMaster shall consider taking action
	 */
	public void pulse(){
		reallocateItems();
		updateBids();
	}

	/**
	 * - Assigns available items to the ClientAgents
	 */
	private void reallocateItems(){

	}

	/**
	 * Send the necessary bits, withdraw no longer necessary bids etc.!
	 */
	private void updateBids(){
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


	/**
	 * Occurs when a Bid has been updated
	 * @param bid
	 */
	public void onBidUpdated(Bid bid){

		switch (bid.getProcessingState()) {
		case REJECTED:

			break;

		case REPLACED:	
		case WITHDRAWN:
		case VALID:

			break;

		case EXPIRED:
			break;

		case TRANSACTED:

			break;

		default:
			break;
		}

	}

	public void onTransaction(Transaction transaction){
		if(transaction.getQuantity() > 0){
			// we have won and buyed the auction, thus we can now update our stock details
			onNewItemArrived(transaction.getAuction(), transaction.getQuantity());
		}else{
			// 
		}
	}



	/**
	 * Occurs when we own n new items. 
	 * @param auction item type
	 * @param quantity n-new items 
	 */
	protected void onNewItemArrived(Auction auction, int quantity){
		stock.incrementQuantity(auction, quantity);
		avaiableItems.incrementQuantity(auction, quantity);
	}



	/**
	 * Occurs when a itemrequest has been updated
	 * @param request
	 */
	protected void onRequestUpdated(ItemRequest request) {

	}  

	/**
	 * Occurs when a itemrequest has been created
	 * @param request
	 */
	protected void onRequestAdded(ItemRequest request) {

	}  


}
