package ch.mas.tacy.model;


import java.util.List;

import com.sun.xml.internal.bind.v2.TODO;

import ch.mas.tacy.Services;
import ch.mas.tacy.TacyAgent;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionType;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.ClientPreferenceType;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.TradeMaster;

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

	public ClientAgent(int clientID, TACAgent agent){
		this.clientPackage = new ClientPackage(clientID);
		client = clientID;
		this.agent = agent;

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
	}

	/**
	 * sets all the clients preferences into the clients package
	 */
	public void setPreferences(){
		clientPackage.setPreferredInFlight(agent.getClientPreference(client, ClientPreferenceType.ARRIVAL));
		clientPackage.setPreferredOutFlight(agent.getClientPreference(client, ClientPreferenceType.DEPARTURE));
		clientPackage.setPvHotel(agent.getClientPreference(client, ClientPreferenceType.HOTEL_VALUE));
		clientPackage.setPvAW(agent.getClientPreference(client, ClientPreferenceType.E1));
		clientPackage.setPvAP(agent.getClientPreference(client, ClientPreferenceType.E2));
		clientPackage.setPvMU(agent.getClientPreference(client, ClientPreferenceType.E3));

		clientPackage.calculateOvernightStays();

	}

	/**
	 * Occurs when there is a change in a item
	 * @param item specifies the item type
	 * @param quantity a positive quantity means increment, negative is decrement
	 */
	public void onTransaction(Auction item, int quantity){

		AuctionCategory category = item.getCategory();
		AuctionType type = item.getType();
		int auctionday = item.getAuctionDay();

		//withdraw the corresponding request
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
			clientPackage.setOvernightStay(auctionday);
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
			if(type.equals(AuctionType.INFLIGHT) && !clientPackage.hasInFlight() && clientPackage.getPreferredInFlight() == auctionday){
				quantity = 1;
			} else if(type.equals(AuctionType.OUTFLIGHT) && !clientPackage.hasOutFlight() && clientPackage.getPreferredOutFlight() == auctionday){
				quantity = 1;
			}
			break;


		case HOTEL:
			//overnight stay of hotel has to be within the trip and not already existing in package
			if(clientPackage.isPresenceDay(auctionday) && !clientPackage.hasHotel(auctionday)){
				quantity = 1;
			}
			break;

		case ENTERTAINMENT:
			//client wants item if: event type does not already exist, there is no event on given day, premium value for given type is not 0
			if(type.equals(AuctionType.EVENT_ALLIGATOR_WRESTLING) && clientPackage.getPvAW() != 0 && !clientPackage.hasEvent(auctionday) && !clientPackage.hasSameEvent(type)){
				quantity = 1;
			}else if(type.equals(AuctionType.EVENT_AMUSEMENT) && clientPackage.getPvAP() != 0 && !clientPackage.hasEvent(auctionday) && !clientPackage.hasSameEvent(type)){
				quantity = 1;
			}else if(type.equals(AuctionType.EVENT_MUSEUM) && clientPackage.getPvMU() != 0 && !clientPackage.hasEvent(auctionday) && !clientPackage.hasSameEvent(type)){
				quantity = 1;
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
		float currentAskPrice = quote.getAskPrice();

		if(virgin){
			//set an offset of 50 to the initial ask price
			float suggestedPrice = currentAskPrice - 50;

			//request this flight to the suggest price
			tradeMaster.updateRequestedItem(this, auction, 1, suggestedPrice);
			virgin = false; // bad bad :)
		} else if (agent.getGameTime() > 3 * 60 * 1000 || auctionManager.getPriceGrowByValue(auction, 100)){
			//replace pending bid with new one which will match the ask price immediately
			tradeMaster.updateRequestedItem(this, auction, 1, currentAskPrice+1);
		}



	}



	private void handleHotels(){

		
		List<Integer> missingDays = clientPackage.getNeedForHotelDays();
		for(Integer day : missingDays){
			Auction auction = agent.getAuctionFor(AuctionCategory.HOTEL, getGoodOrBadHotel(), day);
			Quote quote = auctionManager.getCurrentQuote(auction);
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

		Auction auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, AuctionType.CHEAP_HOTEL, day);
		Quote quote = auctionManager.getCurrentQuote(auction);
		float currentAskPrice = quote.getAskPrice();
		
	
	}

	public AuctionType getGoodOrBadHotel(){


		return (clientPackage.getPvHotel() > 70) ? AuctionType.GOOD_HOTEL : AuctionType.CHEAP_HOTEL;



	}


}
