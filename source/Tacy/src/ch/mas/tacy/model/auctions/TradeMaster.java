package ch.mas.tacy.model.auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ch.mas.tacy.Services;
import ch.mas.tacy.TacyAgent;
import ch.mas.tacy.model.AuctionItemStock;
import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.ClientPackageAllocationStrategy;
import ch.mas.tacy.model.IClientPackageAllocationStrategy;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.Quote;
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

	private static final Logger log = Logger.getLogger(TacyAgent.class.getName());

	private final TACAgent agent;

	/** holds all item requests */
	private final Map<Auction, List<ItemRequest>> requests = new HashMap<Auction, List<ItemRequest>>();


	private final AuctionInformationManager auctionManager = Services.instance().resolve(AuctionInformationManager.class);


	/** holds all items we own */
	private final AuctionItemStock stock = new AuctionItemStock();

	/** holds all item which are currently not assigned to a sub agent */
	private final AuctionItemStock avaiableItems = new AuctionItemStock();


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
	 * Updates the given requested item(auction) amount
	 * 
	 * The request is managed by the trademaster, which means
	 * that prices and bidding are handled by the trademaster
	 * 
	 * @param clientAgent
	 * @param auction
	 * @param amount
	 */
	public void updateManagedRequestedItem(ClientAgent clientAgent, Auction auction, int amount) {
		updateRequestedItem(clientAgent, auction, amount, ItemRequest.ManagedPrice);
	}  

	/**
	 * Updates the given item(auction) requested newQuantity to the given one
	 * 
	 * @param client The client who requests the item(s)
	 * @param auction
	 * @param newQuantity The new requested quantity
	 */
	public void updateRequestedItem(ClientAgent client, Auction auction, int newQuantity)
	{
		float price = 0;

		ItemRequest request = findRequest(auction, client);
		if(request != null){
			price = request.getPrice();
		}

		updateRequestedItem(client, auction, newQuantity, price);
	}

	/**
	 * Updates the given item(auction) requested amount and requested price
	 * 
	 * @param client The client who requests the item(s)
	 * @param auctionId The auction id, representing the item type
	 * @param newQuantity The new requested quantity
	 * @param newPrice The agent's new suggested bit price
	 */
	public void updateRequestedItem(ClientAgent client, Auction auction, int newQuantity, float newPrice){
		assert client != null : "client can not be null";
		assert auction != null : "auction can not be null";

		ItemRequest request = findRequest(auction, client);

		String requestUpdate = "tradeMaster: updateRequestedItem() "
				+ client + " " + auction + " quantity="+newQuantity+" price="+newPrice;
		log.fine(requestUpdate);
		System.out.println(requestUpdate);

		if(request == null)
		{
			// this is a new request
			request = new ItemRequest(client, auction, newQuantity, newPrice);
			placeRequest(request);
		}else{
			// update existing request
			request.setAmount(newQuantity);
			request.setPrice(newPrice);
			onRequestUpdated(request);
		}
	}

	/**
	 * gets the current requested quantity of the given item type by the given client
	 * @param clientAgent
	 * @param item
	 * @return
	 */
	public int getRequestedQuantity(ClientAgent clientAgent, Auction item) {

		int quantity = 0;
		if(requests.containsKey(item))
		{
			List<ItemRequest> myRequests = requests.get(item);
			for (ItemRequest itemRequest : myRequests) {
				if(clientAgent.equals(itemRequest.getOriginator()))
					quantity += itemRequest.getAmount();
			}
		}

		return quantity;
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
	 * Each auction has exactly one or no bid.
	 * 
	 */
	private void updateBids(){

		printRequestTable();
		System.out.println("TradeMaster: updating bids...");

		// handle Bids if necessary
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);

			List<ItemRequest> pendingRequests = findAllRequests(auction);

			// calculate quantities
			// a positive quantity means we need to buy
			// a negative means we can sell

			int requested_quantity = sumQuantity(pendingRequests);
			int avaiable_quantity = avaiableItems.getQuantity(auction);
			int deltaQuantity = requested_quantity - avaiable_quantity;

			float suggestedPrice = 0;

			Bid newBid = new Bid(auction);

			if(deltaQuantity > 0){
				// we need to buy
				Bid currentBid = agent.getBid(newBid.getAuction());
				float currentPrice = 0;
				if(currentBid != null)
				{
					currentPrice = currentBid.getMaxPrice();
				}

				suggestedPrice = getBuyPrice(auction, pendingRequests, currentPrice); 
				newBid.addBidPoint(deltaQuantity, suggestedPrice);
			}else if(deltaQuantity < 0){
				// we need to sell
				if(auction.canSell()){
					suggestedPrice = getSellPrice(auction);
					newBid.addBidPoint(deltaQuantity, suggestedPrice);
				}
			}else{ // deltaQuantity = 0
				// we don't need anything - cancel existing bids
				// for now, we just set the price to 0
				// so we wont buy anything if possible
				suggestedPrice = 0;
				newBid.addBidPoint(deltaQuantity, suggestedPrice);
			}

			sendBid(newBid);
		}
	}

	/**
	 * Sends the given Bid to the server.
	 * Handling new Bids, Bid replacement
	 * @param newBid
	 */
	private void sendBid(Bid newBid){
		Bid currentBid = agent.getBid(newBid.getAuction());

		if(currentBid == null){ // no current Bid

			if(newBid.getQuantity() != 0){ // no need to create a new zero quantity Bid
				agent.submitBid(newBid);
				System.out.println("submitted new Bid: " + newBid);
			}
		}else{
			// we have a current bid
			if(currentBid.getQuantity() != newBid.getQuantity() || currentBid.getMaxPrice() != newBid.getMaxPrice()){
				// which does no longer match our preferred Bid values
				if(!currentBid.isPreliminary())
				{
					agent.replaceBid(currentBid, newBid);
					System.out.println("replaced bid:" + newBid);
				}
			}
		}
	}

	/**
	 * Returns a buy price depending on the requests, price suggestions and current quotes
	 * @param auction
	 * @param requests
	 * @return
	 */
	private float getBuyPrice(Auction auction, Iterable<ItemRequest> requests, float currentPrice){

		float price;

		/** is this a managed request? */
		boolean ismanaged = false;
		for (ItemRequest itemRequest : requests) {
			if(itemRequest.isManaged()){
				ismanaged = true;
				break;
			}
		}

		if(ismanaged)
		{
			price = getManagedBuyPrice(auction, sumQuantity(requests), currentPrice);
		}else
			price = maxPrice(requests);

		return price;
	}

	private final float IncreasingAmmount = 20;

	/**
	 * Calculates a bid price for the given item (auction)
	 * @param auction
	 * @param requiredQuantity
	 * @return
	 */
	private float getManagedBuyPrice(Auction auction, int requiredQuantity, float currentPrice)
	{
		float price = currentPrice;
		Quote quote = auctionManager.getCurrentQuote(auction);

		if(quote != null){

			if(quote.getHQW() < requiredQuantity){
				System.out.println("Increasing Bid: " + quote + "; hqw is " + quote.getHQW() + " of " + requiredQuantity );

				float cp = Math.max(quote.getAskPrice(), quote.getBidPrice());

				price = cp+IncreasingAmmount;
			}

		}else {
			System.err.println("Cant calc managed price for "+auction+": No current Quote!");
		}

		return price;
	}


	/**
	 * Get the sell price for the given auction
	 * @param auction
	 * @return
	 */
	private float getSellPrice(Auction auction) {
		float price = 0;
		AuctionInformationManager auctionInformationManager = Services.instance().resolve(AuctionInformationManager.class);
		Quote q = auctionInformationManager.getCurrentQuote(auction);

		if(q != null)
		{
			price = q.getAskPrice() + (int)((Math.random()-0.5f)*40);
		}

		return price;
	}



	private void printRequestTable(){

		boolean showZeroQuantity = false;


		System.out.println("-------------------------------------------");

		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);
			if(requests.containsKey(auction)){
				List<ItemRequest> itemRequests = requests.get(auction);

				for (ItemRequest itemRequest : itemRequests) {
					if(showZeroQuantity || itemRequest.getAmount() != 0)
						System.out.println(auction + "\t" + itemRequest);  
				}
			}
		}

		System.out.println("-------------------------------------------");

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
	public List<ItemRequest> findAllRequests(ClientAgent client, AuctionCategory category){
		List<ItemRequest> matches = new ArrayList<ItemRequest>();
		for (List<ItemRequest> rq : requests.values()) {
			for (ItemRequest itemRequest : rq) {
				if(itemRequest.getOriginator() != null && itemRequest.getOriginator().equals(client))
				{
					if(category == null || itemRequest.getAuction().getCategory() == category)
						matches.add(itemRequest);
				}
			}
		}

		return matches;
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
	 * Sum of quantity of all given requests
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

	/**
	 * Returns the highest price of the given requests
	 * @param requests
	 * @return
	 */
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

	/**
	 * Occurs when a server side transaction has been made
	 * @param transaction
	 */
	public void onServerTransaction(Transaction transaction){

		System.err.println("onServerTransaction: " + transaction);

		if(transaction.getQuantity() > 0){
			// we have won the auction and bought the item, thus we can now update our stock details
			transact(transaction.getAuction(), transaction.getQuantity());
		}else{
			// we have 
			transact(transaction.getAuction(), transaction.getQuantity());
		}
	}



	/**
	 * transact the given amount of items to this tradmaster
	 * @param auction item type
	 * @param quantity n-new items 
	 */
	protected void transact(Auction auction, int quantity){
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
