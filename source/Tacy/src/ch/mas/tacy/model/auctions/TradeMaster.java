package ch.mas.tacy.model.auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.Services;
import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.ClientPackageAllocationStrategy;
import ch.mas.tacy.model.IClientPackageAllocationStrategy;
import ch.mas.tacy.model.ItemStock;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.agentware.Transaction;
import ch.mas.tacy.util.Lists;


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

	private final TACAgent agent;

	/** holds all item requests */
	private final Map<Auction, List<ItemRequest>> requests = new HashMap<Auction, List<ItemRequest>>();


	/** holds all items we own */
	private final ItemStock stock = new ItemStock();

	/** holds all item which are currently not assigned to a sub agent */
	private final ItemStock avaiableItems = new ItemStock();


	public TradeMaster(TACAgent agent){

		this.agent = agent;

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

	private IClientPackageAllocationStrategy packageAllocator = new ClientPackageAllocationStrategy();

	/**
	 * - Assigns available items to the ClientAgents
	 * Client request will become undone when an desired item was assigned to one of the clients
	 */
	private void reallocateItems(){

		ClientManager clientManager = Services.instance().resolve(ClientManager.class);
		packageAllocator.assignItemsToClientPackages(clientManager.getAllClientAgents(), avaiableItems);

	}

	/**
	 * Send the necessary bids, withdraw no longer necessary bids etc.!
	 * 
	 * 
	 * 
	 */
	private void updateBids(){

		// handle Bids if necessary
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);

			List<ItemRequest> pendingRequests = findAllRequests(auction);

			int requested_quantity = sumQuantity(pendingRequests);
			Bid currentBid = agent.getBid(auction);

			float suggestedPrice = maxPrice(pendingRequests);

			Bid newBid = new Bid(auction);
			newBid.addBidPoint(requested_quantity, suggestedPrice);


			if(currentBid == null){ // no current Bid

				if(requested_quantity > 0){
					agent.submitBid(newBid);
				}
			}else{
				// we have a current bid
				if(currentBid.getQuantity() != requested_quantity || currentBid.getMaxPrice() != suggestedPrice){
					//which does no longer match.
					agent.replaceBid(currentBid, newBid);
				}
			}
		}


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
	 * Searches for all requests of the same item, given as auction
	 * @param auction
	 * @return
	 */
	public List<ItemRequest> findAllRequests(Auction auction){
		if(requests.containsKey(auction)){
			return Lists.newList(requests.get(auction));
		}
		return new ArrayList<ItemRequest>();
	}

	/**
	 * Sum of all given requests
	 * @param requests
	 * @return
	 */
	private int sumQuantity(Iterable<ItemRequest> requests){
		int quantity = 0;
		if(requests != null){
			for (ItemRequest itemRequest : requests) {
				quantity += itemRequest.getAmount();
			}
		}
		return quantity;
	}

	private float maxPrice(Iterable<ItemRequest> requests){
		float price = 0;
		if(requests != null){
			for (ItemRequest itemRequest : requests) {
				price = Math.max(price, itemRequest.getPrice());
			}
		}
		return price;
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
			// we have won the auction and bought the item, thus we can now update our stock details
			onNewItemArrived(transaction.getAuction(), transaction.getQuantity());
		}else{
			// we have 
			onNewItemArrived(transaction.getAuction(), transaction.getQuantity());
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
