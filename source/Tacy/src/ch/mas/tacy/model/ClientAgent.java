package ch.mas.tacy.model;

import ch.mas.tacy.Services;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionType;
import ch.mas.tacy.model.agentware.ClientPreferenceType;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.TradeMaster;

/**
 * Sub agent for a single client
 * 
 * @author P. Büttiker
 *
 */
public class ClientAgent {

	private boolean virgin = true;
	private final TACAgent agent;
	private final ClientPackage clientPackage;
	private final int client;


	private final Services services = Services.instance();

	private final AuctionInformationManager auctionManager = services.resolve(AuctionInformationManager.class);
	private final TradeMaster tradeMaster = services.resolve(TradeMaster.class);

	public ClientAgent(int clientID, TACAgent agent){
		this.clientPackage = new ClientPackage(clientID);
		client = clientID;
		this.agent = agent;
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
	}

	/**
	 * Occurs when there is a change in a item
	 * @param item specifies the item type
	 * @param quantity a positive quantity means increment, negative is decrement
	 */
	public void onTransaction(Auction item, int quantity){
		//TODO probably adjust client request right now?!z
	}

	/**
	 * Checks if this client wants the given item(Auction) type
	 * and returns the amount of items. 
	 * 
	 * @param item returns the quantity of items this client wants
	 * @return
	 */
	public int want(Auction item){
		//TODO 
		return 0;
	}


	/**
	 * Handle the flights
	 */
	private void handleFlights(){
		if(!clientPackage.hasInFlight()){
			int flightDay = agent.getClientPreference(client, ClientPreferenceType.ARRIVAL);
			allocFlight(flightDay, AuctionType.INFLIGHT);
		}

		if(!clientPackage.hasOutFlight()){
			int flightDay = agent.getClientPreference(client, ClientPreferenceType.DEPARTURE);
			allocFlight(flightDay, AuctionType.OUTFLIGHT);
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


}
