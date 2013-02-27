package ch.mas.tacy.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sun.awt.SunToolkit.InfiniteLoop;

import ch.mas.tacy.Services;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionType;
import ch.mas.tacy.model.agentware.ClientPreferenceType;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.AuctionInformationManager;
import ch.mas.tacy.model.auctions.ItemRequest;
import ch.mas.tacy.model.auctions.QuoteChangeManager;
import ch.mas.tacy.model.auctions.TradeMaster;
import ch.mas.tacy.model.auctions.ValuedAuction;

/**
 * Sub agent for a single client
 * 
 * @author P. Büttiker & E. Neher
 *
 */
public class ClientAgent {

	private boolean virgin = true;
	private final TACAgent agent;
	private final ClientPackage clientPackage;
	private final int client;
	private Quote lastHotelQuote = null;


	private final Services services = Services.instance();

	private final AuctionInformationManager auctionManager = services.resolve(AuctionInformationManager.class);
	private final TradeMaster tradeMaster = services.resolve(TradeMaster.class);

	private final QuoteChangeManager quoteChangeManager = new QuoteChangeManager();


	public ClientAgent(int clientID, TACAgent agent){
		this.clientPackage = new ClientPackage(clientID);
		client = clientID;
		this.agent = agent;

		auctionManager.registerQuoteChangeListener(quoteChangeManager);

		setPreferences();
	}

	public ClientPackage getClientPackage() {
		return clientPackage;
	}

	/**
	 * Raised when this agent should consider taking action.
	 * 
	 * This normally occurs when Quotes have updated or anything other important has occurred,
	 * such as the game is about to close
	 */
	public void pulse() {
		handleFlights();
		handleHotels();
		handleEntertainment();
	}

	/**
	 * sets all the clients preferences into the clients package
	 */
	public void setPreferences(){
		clientPackage.setPreferredInFlight(agent.getClientPreference(client, ClientPreferenceType.ARRIVAL));
		clientPackage.setPreferredOutFlight(agent.getClientPreference(client, ClientPreferenceType.DEPARTURE));
		clientPackage.setPvHotel(agent.getClientPreference(client, ClientPreferenceType.HOTEL_VALUE));
		clientPackage.setPremiumValueAlligatorWrestling(agent.getClientPreference(client, ClientPreferenceType.E1));
		clientPackage.setPremiumValueAmusementPark(agent.getClientPreference(client, ClientPreferenceType.E2));
		clientPackage.setPremiumValuevMuseum(agent.getClientPreference(client, ClientPreferenceType.E3));


	}

	/**
	 * Occurs when there is a change in a item to this clients package
	 * @param item specifies the item type
	 * @param quantity a positive quantity means increment, negative is decrement
	 */
	public void onTransaction(Auction item, int quantity){

		AuctionCategory category = item.getCategory();
		AuctionType type = item.getType();
		int auctionday = item.getAuctionDay();

		//withdraw the corresponding request
		//TODO the quantity need to be the old ordered quantity
		tradeMaster.updateRequestedItem(this, item, quantity, 0);


		//keep track in the clients package
		switch(type){

		case INFLIGHT:
			clientPackage.setInFlight(auctionday);
			break;
		case OUTFLIGHT:
			clientPackage.setOutFlight(auctionday);
			break;
		case CHEAP_HOTEL:
		case GOOD_HOTEL:
			clientPackage.setActualHotelRooms(auctionday, type);
			break;
		case EVENT_ALLIGATOR_WRESTLING:
		case EVENT_AMUSEMENT:
		case EVENT_MUSEUM:
			clientPackage.setEvent(auctionday, type);
			break;
		default:
			break;
		}
	}

	/**
	 * Checks if this client wants the given item(Auction) type
	 * and returns the amount of items. 
	 * 
	 * @param item returns the quantity of items this client wants
	 * @return
	 */
	public int want(Auction item){
		int quantity = 0;

		AuctionCategory category = item.getCategory();
		AuctionType type = item.getType();
		int auctionday = item.getAuctionDay();


		switch(category){

		case FLIGHT:
			if(type.equals(AuctionType.INFLIGHT) &&
					!clientPackage.hasInFlight() && clientPackage.getPreferredInFlight() == auctionday){
				quantity = 1;
				System.out.println("clientagent: want" +quantity+" inflight for "+auctionday);
			} else if(type.equals(AuctionType.OUTFLIGHT) && 
					!clientPackage.hasOutFlight() && clientPackage.getPreferredOutFlight() == auctionday){
				quantity = 1;
				System.out.println("clientagent: want" +quantity+" outflights for "+auctionday);
			}
			break;


		case HOTEL:

			if(clientPackage.isPresenceDay(auctionday) && !clientPackage.hasHotel(auctionday))
			{
				// we need a hotel at this day

				// lets see if the hotel type matches

				// overnight stay of hotel has to be within the trip,
				// must be of the same hotel type which already exists
				// in the package and not already existing in package
				if(clientPackage.getCurrenHotelType().equals(type) || clientPackage.getCurrenHotelType().equals(AuctionType.None)){
					quantity = 1;
					System.out.println("clientagent: want" +quantity+" hotel rooms for "+auctionday);
				}
			}
			break;

		case ENTERTAINMENT:

			//client wants item if: event type does not already exist, there is no event on given day, premium value for given type is not 0

			if(!clientPackage.hasEventAt(auctionday) && !clientPackage.hasSameEvent(type)){
				if(type.equals(AuctionType.EVENT_ALLIGATOR_WRESTLING) && clientPackage.getPremiumValueAlligatorWrestling() != 0){
					quantity = 1;
					System.out.println("clientagent: want" +quantity+" event type alligator wrestling for "+auctionday);
				}else if(type.equals(AuctionType.EVENT_AMUSEMENT) && clientPackage.getPremiumValueAmusementPark() != 0){
					quantity = 1;
					System.out.println("clientagent: want" +quantity+" event type amusement park for "+auctionday);
				}else if(type.equals(AuctionType.EVENT_MUSEUM) && clientPackage.getPremiumValuevMuseum() != 0){
					quantity = 1;
					System.out.println("clientagent: want" +quantity+" event type museum for "+auctionday);
				}
			}


			break;

		default:
			break;

		}


		return quantity;
	}


	/**
	 * Handle the flights
	 */
	private void handleFlights(){
		System.out.println("handleflights");
		if(!clientPackage.hasInFlight()){
			allocFlight(clientPackage.getPreferredInFlight(), AuctionType.INFLIGHT);
		}

		if(!clientPackage.hasOutFlight()){
			allocFlight(clientPackage.getPreferredOutFlight(), AuctionType.OUTFLIGHT);
		}
	}

	/**
	 * Try to allocate a flight
	 * @param day
	 * @param flightType is it a in or out flight
	 */
	private void allocFlight(int day, AuctionType flightType){
		if(!(flightType == AuctionType.INFLIGHT || flightType == AuctionType.OUTFLIGHT)){
			System.err.println("invlaid flight type");
			return;
		}


		Auction auction = TACAgent.getAuctionFor(AuctionCategory.FLIGHT, flightType, day);
		Quote quote = auctionManager.getCurrentQuote(auction);
		long gameduration = agent.getGameTime();
		long pointOfReturn = 3 * 60 * 1000;
		
		System.out.println(gameduration);
		System.out.println(pointOfReturn);
		
		if(quote != null){
			System.out.println("quote not null");
			float currentAskPrice = quote.getAskPrice();
			
			if(virgin){
				System.out.println("virgin");
				//set an offset of 50 to the initial ask price
				float suggestedPrice = currentAskPrice - 50;

				//request this flight to the suggest price
				tradeMaster.updateRequestedItem(this, auction, 1, suggestedPrice);
				virgin = false; // bad bad :)
			} else if (gameduration > pointOfReturn || auctionManager.getPriceGrowByValue(auction, 100)){
				//replace pending bid with new one which will match the ask price immediately
				System.out.println("not virgin");
				tradeMaster.updateRequestedItem(this, auction, 1, currentAskPrice+1);
			}
		}
	}


	/**
	 * handle the Hotels
	 */
	private void handleHotels(){


		List<Integer> missingHoteDays = clientPackage.getNeedForHotelDays();

		for(Integer day : missingHoteDays){
			Auction auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, isTTProfitable(), day);
			Quote quote = auctionManager.getCurrentQuote(auction);
			int alloc = agent.getAllocation(auction);
			if(quoteChangeManager.tryVisit(auction) && quote.hasHQW(agent.getBid(auction)) && quote.getHQW() < alloc){

				tradeMaster.updateRequestedItem(this, auction, 1, quote.getAskPrice()+50);
			}

		}


		/*
		Auction auction = quote.getAuction();
		AuctionCategory auctionCategory = auction.getCategory();
		if (auctionCategory == AuctionCategory.HOTEL) {
			int alloc = agent.getAllocation(auction);
			if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
					quote.getHQW() < alloc) {
				Bid bid = new Bid(auction);
				// Can not own anything in hotel auctions...
				prices[auction.getId()] = quote.getAskPrice() + 50;
				bid.addBidPoint(alloc, prices[auction.getId()]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction)
							+ " own=" + agent.getOwn(auction));
				}
				agent.submitBid(bid);
			}
		 */

	}


	public void allocHotels(int day, AuctionType hotelType){

	}

	/** how many diffrent event types do exist*/
	private final static int DiffrentEventTypeCount = 3;


	/**
	 * Returns all possible entertainment auctions, sorted by their current value
	 * @return
	 */
	private List<ValuedAuction> calculateEntertainmentValues(){
		List<ValuedAuction> valuedAuctions = new ArrayList<ValuedAuction>();
		List<Auction> entertainmentAuctions = new ArrayList<Auction>();

		//get all entertainment auctions
		for(int i=0; i< TACAgent.getAuctionNo(); i++){	
			if(TACAgent.getAuction(i).getCategory() == AuctionCategory.ENTERTAINMENT){
				entertainmentAuctions.add(TACAgent.getAuction(i));
			}
		}

		//for each entertainment auction calculate the current profit based on what we would have to pay and what the current price for the ticket is
		for (Auction auction : entertainmentAuctions) {

			Quote currentQuote = auctionManager.getCurrentQuote(auction);
			if(currentQuote != null){


				float currentPrice = currentQuote.getAskPrice();
				float value = 0;

				if(auction.getType() == AuctionType.EVENT_ALLIGATOR_WRESTLING){
					value = clientPackage.getPremiumValueAlligatorWrestling() - currentPrice;
				} else if(auction.getType() == AuctionType.EVENT_AMUSEMENT){
					value = clientPackage.getPremiumValueAmusementPark() - currentPrice;
				} else if(auction.getType() == AuctionType.EVENT_MUSEUM){
					value = clientPackage.getPremiumValuevMuseum() - currentPrice;
				}

				if(value >= 0)
				{
					ValuedAuction va = new ValuedAuction(auction, value);
					valuedAuctions.add(va);
				}
			}
		}

		Collections.sort(valuedAuctions);
		return valuedAuctions;
	}


	/**
	 * handle the Entertainment
	 */
	public void handleEntertainment(){

		List<Integer> missingEventDays = clientPackage.getNeedForEvents();
		//Map<Integer, AuctionType> plannedEvents = new HashMap<Integer, AuctionType>();

		// clear all pending requests
		List<ItemRequest> allEntertainmentRequests = tradeMaster.findAllRequests(this, AuctionCategory.ENTERTAINMENT);
		for (ItemRequest itemRequest : allEntertainmentRequests) {
			tradeMaster.updateRequestedItem(this, itemRequest.getAuction(), 0, 0);
		}

		// for each free day, ensure that we have an item-event request with a given price

		// check if we have any free entertainment day, if not -> abort

		List<ValuedAuction> sortedValues = calculateEntertainmentValues();

		for (ValuedAuction valuedAuction : sortedValues) {
			int day = valuedAuction.getAuction().getAuctionDay();
			if(missingEventDays.contains(day))
			{
				// do not forget to clear all pending bids first ;)

				tradeMaster.updateRequestedItem(this, valuedAuction.getAuction(), 1, auctionManager.getCurrentQuote(valuedAuction.getAuction()).getAskPrice()+1);
			}
		}




		/*
		while(missingEventDays.size() > DiffrentEventTypeCount){
			missingEventDays.remove((int)Math.random()*DiffrentEventTypeCount);
		}


		for(Integer day : missingEventDays){
				plannedEvents.put(day, value);
		}

		for(Integer day : plannedEvents.keySet()){
			Auction auction = TACAgent.getAuctionFor(AuctionCategory.ENTERTAINMENT, isTTProfitable(), day);
			Quote quote = auctionManager.getCurrentQuote(auction);
			int alloc = agent.getAllocation(auction);
			if(quoteChangeManager.tryVisit(auction) && quote.hasHQW(agent.getBid(auction)) && quote.getHQW() < alloc){

				tradeMaster.updateRequestedItem(this, auction, 1, quote.getAskPrice()+50);
			}

		}
		 */



		/*
		 * else if (auctionCategory == AuctionCategory.ENTERTAINMENT) {
			int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
			if (alloc != 0) {
				Bid bid = new Bid(auction);
				if (alloc < 0)
					prices[auction.getId()] = 200f - (agent.getGameTime() * 120f) / 720000;
				else
					prices[auction.getId()] = 50f + (agent.getGameTime() * 100f) / 720000;
				bid.addBidPoint(alloc, prices[auction.getId()]);
				if (DEBUG) {
					log.finest("submitting bid with alloc="
							+ agent.getAllocation(auction)
							+ " own=" + agent.getOwn(auction));
				}
				agent.submitBid(bid);
			}
		}*
		 */


	}


	/**
	 * returns for which hotel type we have to buy rooms (based on clients premium value for hotels)
	 * @return
	 */
	private AuctionType isTTProfitable(){


		if(clientPackage.getCurrenHotelType() == AuctionType.None){

			//if the difference between the total cost for SS and the total cost for TT is smaller than the clients
			//premium value there is no point in buying TT rooms
			int hypotheticalCostSS = 0;
			int hypotheticalCostTT = 0;

			List<Integer> missingDays = clientPackage.getNeedForHotelDays();

			for(Integer day : missingDays){
				Auction auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, AuctionType.CHEAP_HOTEL, day);
				hypotheticalCostSS += auctionManager.getCurrentQuote(auction).getAskPrice();

				auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, AuctionType.GOOD_HOTEL, day);
				hypotheticalCostTT += auctionManager.getCurrentQuote(auction).getAskPrice();			
			}

			int difference = hypotheticalCostTT-hypotheticalCostSS; //could also be negative which would mean that the current price for staying in TT is cheaper than for staying in ss.


			return (clientPackage.getPvHotel() > difference) ? AuctionType.GOOD_HOTEL : AuctionType.CHEAP_HOTEL;

		} else {

			return clientPackage.getCurrenHotelType();

		}
	}


	@Override
	public String toString(){
		return "ClientAgent{" + client + "}";
	}

	/**
	 * Returns the subjective value for the given Entertainment ticket for this client.
	 * (Assuming, he would own this ticket)
	 * @param auction
	 * @return
	 */
	public double getEntertainmentValue(Auction auction) {

		assert auction != null : "auction cannot be null!";

		double value;

		int day = auction.getAuctionDay();
		AuctionType type = auction.getType();

		switch(type){

		case EVENT_ALLIGATOR_WRESTLING:
			value = clientPackage.getPremiumValueAlligatorWrestling();

		case EVENT_AMUSEMENT:
			value = clientPackage.getPremiumValueAmusementPark();

		case EVENT_MUSEUM:
			value = clientPackage.getPremiumValuevMuseum();

		default:
			value = 0;

		}

		//if package is not travel feasible yet we divide by 100 to reduce the package entertainment value
		if(!clientPackage.isTravelFeasible()){
			value = value/100;
		}

		return value;
	}


}




