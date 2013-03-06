package ch.mas.tacy.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	// services
	private final Services services = Services.instance();
	private final AuctionInformationManager auctionManager = services.resolve(AuctionInformationManager.class);
	private final TradeMaster tradeMaster = services.resolve(TradeMaster.class);

	// final fields
	private boolean logRequests = false;
	private boolean logWant = false;
	private boolean flightVirgin = true;
	private final TACAgent agent;
	private final ClientPackage clientPackage;
	private final int client;

	private final QuoteChangeManager quoteChangeManager = new QuoteChangeManager();

	// fields
	private ClientPreferences clientPreferences = null;



	/**
	 * Creates a new CLientAgent
	 * @param clientID
	 * @param agent
	 */
	public ClientAgent(int clientID, TACAgent agent){
		this.clientPackage = new ClientPackage(clientID);
		this.client = clientID;
		this.agent = agent;

		auctionManager.registerQuoteChangeListener(quoteChangeManager);
	}

	public ClientPackage getClientPackage() {
		return clientPackage;
	}

	/**
	 * Returns the clients current preferences - they might be null at any time!
	 * @return
	 */
	public ClientPreferences getClientPreferences(){
		return clientPreferences;
	}

	public void setClientPreferences(ClientPreferences clientPreferences){
		this.clientPreferences = clientPreferences;
	}

	/**
	 * Raised when this agent should consider taking action.
	 * 
	 * This normally occurs when Quotes have updated or anything other important has occurred,
	 * such as the game is about to close
	 */
	public void pulse() {

		System.out.println("=========== PULSE => " + this + " =================");

		if(clientPreferences == null)
		{
			System.out.println("Missing clientPreferences!");
		}

		try{
			handleFlights();
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			handleHotels();
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			handleEntertainment();
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("=======================/=========================");

	}

	/**
	 * Handle the flights
	 */
	private void handleFlights(){
		if(clientPreferences != null){
			if(!clientPackage.hasInFlight()){
				allocFlight(clientPreferences.getPreferredInFlight(), AuctionType.INFLIGHT);
			}

			if(!clientPackage.hasOutFlight()){
				allocFlight(clientPreferences.getPreferredOutFlight(), AuctionType.OUTFLIGHT);
			}
		}
	}


	/**
	 * Occurs when there is a change in a item to this clients package
	 * @param item specifies the item type
	 * @param quantity a positive quantity means increment, negative is decrement
	 */
	public void onTransaction(Auction item, int quantity){

		// withdraw the corresponding request
		int currentQuantity = tradeMaster.getRequestedQuantity(this, item);
		int remainingQuantity = currentQuantity - quantity;
		tradeMaster.updateRequestedItem(this, item, remainingQuantity);


		// keep track in the clients package
		AuctionType type = item.getType();
		int auctionday = item.getAuctionDay();

		switch(type){

		case INFLIGHT:
			clientPackage.setInFlight(auctionday);
			break;
		case OUTFLIGHT:
			clientPackage.setOutFlight(auctionday);
			break;
		case CHEAP_HOTEL:
		case GOOD_HOTEL:
			clientPackage.addItem(item, quantity);
			break;
		case EVENT_ALLIGATOR_WRESTLING:
		case EVENT_AMUSEMENT:
		case EVENT_MUSEUM:
			clientPackage.addItem(item, quantity);
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


		if(clientPreferences == null) return 0; // as long as we have no preferences, we are not interested in anything


		switch(category){

		case FLIGHT:
			if(type.equals(AuctionType.INFLIGHT) &&
					!clientPackage.hasInFlight() && clientPreferences.getPreferredInFlight() == auctionday){
				quantity = 1;
				if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" inflight for day "+auctionday);}
			} else if(type.equals(AuctionType.OUTFLIGHT) && 
					!clientPackage.hasOutFlight() && clientPreferences.getPreferredOutFlight() == auctionday){
				quantity = 1;
				if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" outflights for day "+auctionday);}
			}
			break;


		case HOTEL:

			if(clientPreferences.isPresenceDay(auctionday) && !clientPackage.hasHotelAt(auctionday))
			{
				// we need a hotel at this day

				// lets see if the hotel type matches

				// overnight stay of hotel has to be within the trip,
				// must be of the same hotel type which already exists
				// in the package and not already existing in package
				if(clientPackage.getCurrenHotelType().equals(type) || clientPackage.getCurrenHotelType().equals(AuctionType.None)){
					quantity = 1;
					if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" hotel rooms for day "+auctionday);}
				}
			}
			break;

		case ENTERTAINMENT:

			//client wants item if: event type does not already exist, there is no event on given day, premium value for given type is not 0

			if(!clientPackage.hasEventAt(auctionday) && !clientPackage.hasSameEvent(item)){
				if(type.equals(AuctionType.EVENT_ALLIGATOR_WRESTLING) && clientPreferences.getPremiumValueAlligatorWrestling() != 0){
					quantity = 1;
					if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" event type alligator wrestling for day "+auctionday);}
				}else if(type.equals(AuctionType.EVENT_AMUSEMENT) && clientPreferences.getPremiumValueAmusementPark() != 0){
					quantity = 1;
					if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" event type amusement park for day "+auctionday);}
				}else if(type.equals(AuctionType.EVENT_MUSEUM) && clientPreferences.getPremiumValuevMuseum() != 0){
					quantity = 1;
					if(logWant){System.out.println("clientagent: client "+client+" wants " +quantity+" event type museum for day "+auctionday);}
				}
			}


			break;

		default:
			break;

		}


		return quantity;
	}




	/**
	 * Try to allocate a flight
	 * @param day
	 * @param flightType is it a in or out flight
	 */
	private void allocFlight(int day, AuctionType flightType){
		if(!(flightType == AuctionType.INFLIGHT || flightType == AuctionType.OUTFLIGHT)){
			System.err.println("allocFlight: invlaid flight type");
			return;
		}


		Auction auction = TACAgent.getAuctionFor(AuctionCategory.FLIGHT, flightType, day);
		Quote quote = auctionManager.getCurrentQuote(auction);
		long gameduration = agent.getGameTime();
		long pointOfReturn = 3 * 60 * 1000;


		if(quote != null){
			//System.out.println("quote not null");
			float currentAskPrice = quote.getAskPrice();

			if(flightVirgin){
				System.out.println("client "+client+" is flight-virgin, placing hopeful bid");
				//set an offset of 15 to the initial ask price
				float suggestedPrice = currentAskPrice - 15;

				//request this flight to the suggest price
				tradeMaster.updateRequestedItem(this, auction, 1, suggestedPrice);
				if(logRequests){System.out.println("client with ID "+client+" requested 1 item of "+auction.getType().toString()+" for $"+suggestedPrice);}

				flightVirgin = false;
			} else if (gameduration > pointOfReturn || auctionManager.getPriceGrowByValue(auction, 15)){
				//replace pending bid with new one which will match the ask price immediately
				//System.out.println("not virgin");
				tradeMaster.updateRequestedItem(this, auction, 1, currentAskPrice+1);
				if(logRequests){System.out.println("client with ID "+client+" requested 1 item of "+auction.getType().toString()+" for $"+currentAskPrice+1);}
			}
		}
	}


	/**
	 * handle the Hotels
	 */
	private void handleHotels(){

		/*
		for (ItemRequest r : tradeMaster.findAllRequests(this, AuctionCategory.HOTEL)) {
			r.setAmount(0);
			r.setPrice(0);
		}
		 */


		if(clientPreferences != null){
			List<Integer> missingHoteDays = clientPackage.getNeedForHotelDays(clientPreferences);

			AuctionType hotelType =  isTTProfitable();

			for(Integer day : missingHoteDays){
				Auction auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, hotelType, day);
				tradeMaster.updateManagedRequestedItem(this, auction, 1);
			}
		}

	}


	/** how many diffrent event types do exist*/
	private final static int DiffrentEventTypeCount = 3;



	private List<Auction> getAuctionsOfCategory(AuctionCategory cat){
		List<Auction> aucts = new ArrayList<Auction>();

		for(int i=0; i< TACAgent.getAuctionNo(); i++){	
			Auction auction = TACAgent.getAuction(i);

			if(auction.getCategory() == cat){
				aucts.add(auction);
			}
		}

		return aucts;
	}

	/**
	 * Returns all possible entertainment auctions, sorted by their current value
	 * @return
	 */
	private List<ValuedAuction> calculateEntertainmentValues(){
		List<ValuedAuction> valuedAuctions = new ArrayList<ValuedAuction>();

		if(clientPreferences != null)
		{
			List<Auction> entertainmentAuctions = getAuctionsOfCategory(AuctionCategory.ENTERTAINMENT);

			//for each entertainment auction calculate the current profit based on its premium value and what the current price for the ticket is
			for (Auction auction : entertainmentAuctions) {

				Quote currentQuote = auctionManager.getCurrentQuote(auction);
				if(currentQuote != null){


					float currentPrice = currentQuote.getAskPrice();
					float value = 0;

					if(auction.getType() == AuctionType.EVENT_ALLIGATOR_WRESTLING){
						value = clientPreferences.getPremiumValueAlligatorWrestling() - currentPrice;
					} else if(auction.getType() == AuctionType.EVENT_AMUSEMENT){
						value = clientPreferences.getPremiumValueAmusementPark() - currentPrice;
					} else if(auction.getType() == AuctionType.EVENT_MUSEUM){
						value = clientPreferences.getPremiumValuevMuseum() - currentPrice;
					}

					if(value >= 0)
					{
						ValuedAuction va = new ValuedAuction(auction, value);
						valuedAuctions.add(va);
					}
				}
			}

			Collections.sort(valuedAuctions);
		}


		return valuedAuctions;
	}


	/**
	 * handle the Entertainment
	 */
	public void handleEntertainment(){

		if(clientPreferences != null){

			List<Integer> missingEventDays = clientPackage.getNeedForEvents(clientPreferences);

			// clear all pending requests
			List<ItemRequest> allEntertainmentRequests = tradeMaster.findAllRequests(this, AuctionCategory.ENTERTAINMENT);
			for (ItemRequest itemRequest : allEntertainmentRequests) {
				tradeMaster.updateRequestedItem(this, itemRequest.getAuction(), 0, 0);
			}

			// for each free day, ensure that we have an item-event request with a given price

			// check if we have any free entertainment day, if not -> abort

			List<ValuedAuction> sortedValues = calculateEntertainmentValues();

			for (ValuedAuction valuedAuction : sortedValues) {
				Integer day = valuedAuction.getAuction().getAuctionDay();
				if(missingEventDays.contains(day))
				{
					float suggestedPrice = auctionManager.getCurrentQuote(valuedAuction.getAuction()).getAskPrice()+1;

					tradeMaster.updateRequestedItem(
							this,
							valuedAuction.getAuction(),
							1, // 1 piece
							suggestedPrice);

					missingEventDays.remove((Object)day);

					if(logRequests){
						System.out.println("client with ID "+client+" requested 1 item of "+
								valuedAuction.getAuction().getType().toString()+" for $"+
								suggestedPrice);}

				}
			}

		}

	}


	/**
	 * returns for which hotel type we have to buy rooms (based on clients premium value for hotels)
	 * @return
	 */
	private AuctionType isTTProfitable(){

		//check if the client does not have an assigned hotel room type yet.
		//If so just return the current hotel type
		if(clientPackage.getCurrenHotelType() == AuctionType.None){

			//if the client needs more than 2 hotel rooms he is not allowed to request good hotel rooms
			if(clientPreferences != null && clientPreferences.getPresenceDuration() <= 2){

				//if the difference between the total cost for staying in SS and the total cost for staying in TT 
				//is smaller than the clients premium value there is no point in buying TT rooms
				int hypotheticalCostSS = 0;
				int hypotheticalCostTT = 0;

				List<Integer> missingDays = clientPackage.getNeedForHotelDays(clientPreferences);

				for(Integer day : missingDays){
					Auction auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, AuctionType.CHEAP_HOTEL, day);
					Quote currentQuote = auctionManager.getCurrentQuote(auction);
					if(currentQuote != null){
						hypotheticalCostSS += currentQuote.getAskPrice();
					}

					Auction auction2 = TACAgent.getAuctionFor(AuctionCategory.HOTEL, AuctionType.GOOD_HOTEL, day);
					Quote currentQuote2 = auctionManager.getCurrentQuote(auction2);
					if(currentQuote2 != null){
						hypotheticalCostTT += currentQuote2.getAskPrice();			
					}
				}

				int difference = hypotheticalCostTT-hypotheticalCostSS; //could also be negative which would mean that the current price for staying in TT is cheaper than for staying in ss.
				System.out.println("client "+client+" hotel price difference: "+ difference);
				//profit of buying TT rooms has to be at least 50
				difference += 50;
				return (clientPreferences.getPremiumValueHotel() <= difference) ? AuctionType.CHEAP_HOTEL : AuctionType.GOOD_HOTEL;
			}else {
				return AuctionType.CHEAP_HOTEL;
			}
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

		double value = 0;

		if(clientPreferences != null)
		{
			int day = auction.getAuctionDay();
			AuctionType type = auction.getType();

			switch(type){

			case EVENT_ALLIGATOR_WRESTLING:
				value = clientPreferences.getPremiumValueAlligatorWrestling();

			case EVENT_AMUSEMENT:
				value = clientPreferences.getPremiumValueAmusementPark();

			case EVENT_MUSEUM:
				value = clientPreferences.getPremiumValuevMuseum();

			default:
				value = 0;

			}

			//if package is not travel feasible yet we divide by 100 to reduce the package entertainment value
			if(!clientPackage.isTravelFeasible(clientPreferences)){
				value = value/100;
			}
		}

		return value;
	}

	//gets called when client preferences are available (at game start)
	public void updatePreferences() {

		clientPreferences =  new ClientPreferences(client);

		clientPreferences.setPreferredInFlight(agent.getClientPreference(client, ClientPreferenceType.ARRIVAL));
		clientPreferences.setPreferredOutFlight(agent.getClientPreference(client, ClientPreferenceType.DEPARTURE));
		clientPreferences.setPremiumValueHotel(agent.getClientPreference(client, ClientPreferenceType.HOTEL_VALUE));
		clientPreferences.setPremiumValueAlligatorWrestling(agent.getClientPreference(client, ClientPreferenceType.E1));
		clientPreferences.setPremiumValueAmusementPark(agent.getClientPreference(client, ClientPreferenceType.E2));
		clientPreferences.setPremiumValuevMuseum(agent.getClientPreference(client, ClientPreferenceType.E3));

		System.out.println("-------------- updated client ("+ client +") preferences ------------------");
		System.out.println(clientPreferences.toString());
		System.out.println("-----------------------------/-----------------------------");
	}


}




