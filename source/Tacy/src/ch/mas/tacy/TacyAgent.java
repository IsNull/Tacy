package ch.mas.tacy;

import java.util.logging.Logger;

import ch.mas.tacy.model.AuctionInformationManager;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.agentware.AgentImpl;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionState;
import ch.mas.tacy.model.agentware.AuctionType;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.ClientPreferenceType;
import ch.mas.tacy.model.agentware.CommandStatus;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.util.ArgEnumerator;

/**
 * Tacy - a MAS Agent implementation for BTH
 * 
 * 
 * @author Pascal Büttiker & Eric Neher
 *
 */
public class TacyAgent extends AgentImpl  {

	private static final Logger log = Logger.getLogger(TacyAgent.class.getName());
	private static final boolean DEBUG = false;

	private final AuctionInformationManager auctionManager = AuctionInformationManager.instance();
	private ClientManager clientManager;

	private float[] prices;

	@Override
	protected void init(ArgEnumerator args) {
		prices = new float[TACAgent.getAuctionNo()];
		clientManager = new ClientManager(agent);
	}



	@Override
	public void quoteUpdated(Quote quote) {
		auctionManager.onQuoteUpdated(quote);

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
		} else if (auctionCategory == AuctionCategory.ENTERTAINMENT) {
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
		}
	}

	@Override
	public void quoteUpdated(AuctionCategory auctionCategory) {
		log.fine("All quotes for "
				+ auctionCategory
				+ " has been updated");
	}

	@Override
	public void bidUpdated(Bid bid) {
		log.fine("Bid Updated: id=" + bid.getID() + " auction="
				+ bid.getAuction() + " state="
				+ bid.getProcessingState());
		log.fine("       Hash: " + bid.getBidHash());
	}

	@Override
	public void bidRejected(Bid bid) {
		log.warning("Bid Rejected: " + bid.getID());
		log.warning("      Reason: " + bid.getRejectReason()
				+ " (" + bid.getRejectReason() + ')');
	}

	@Override
	public void bidError(Bid bid, CommandStatus status) {
		log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
				+ " (" + status + ')');
	}

	@Override
	public void gameStarted() {
		log.fine("Game " + agent.getGameID() + " started!");

		calculateAllocation();
		sendBids();
	}

	@Override
	public void gameStopped() {
		log.fine("Game Stopped!");
	}

	@Override
	public void auctionClosed(int auction) {
		Auction auc = TACAgent.getAuction(auction);
		if(auc != null)
		auc.setState(AuctionState.CLOSED);
		log.fine("*** Auction " + auction + " closed!");
	}

	private void sendBids() {
		for (int i = 0, n = TACAgent.getAuctionNo(); i < n; i++) {

			Auction auction = TACAgent.getAuction(i);

			int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
			float price = -1f;
			switch (auction.getCategory()) {
			case FLIGHT:
				if (alloc > 0) {
					price = 1000;
				}
				break;
			case HOTEL:
				if (alloc > 0) {
					price = 200;
					prices[i] = 200f;
				}
				break;
			case ENTERTAINMENT:
				if (alloc < 0) {
					price = 200;
					prices[i] = 200f;
				} else if (alloc > 0) {
					price = 50;
					prices[i] = 50f;
				}
				break;
			default:
				break;
			}
			if (price > 0) {
				Bid bid = new Bid(auction);
				bid.addBidPoint(alloc, price);
				if (DEBUG) {
					log.finest("submitting bid with alloc=" + agent.getAllocation(auction)
							+ " own=" + agent.getOwn(auction));
				}
				agent.submitBid(bid);
			}
		}
	}




	private void calculateAllocation() {
		for (int client = 0; client < 8; client++) {
			int inFlight = agent.getClientPreference(client, ClientPreferenceType.ARRIVAL);
			int outFlight = agent.getClientPreference(client, ClientPreferenceType.DEPARTURE);
			int hotel = agent.getClientPreference(client, ClientPreferenceType.HOTEL_VALUE);
			AuctionType type;



			// if the hotel value is greater than 70 we will select the expensive hotel
			if (hotel > 100) {
				type = AuctionType.GOOD_HOTEL;
			} else {
				type = AuctionType.CHEAP_HOTEL;
			}

			Auction auction;
			// allocate a hotel night for each day that the agent stays
			for (int d = inFlight; d < outFlight; d++) {
				auction = TACAgent.getAuctionFor(AuctionCategory.HOTEL, type, d);
				log.finer("Adding hotel for day: " + d + " on " + auction);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
			}

			AuctionType eType = AuctionType.None;
			while((eType = nextEntType(client, eType)) != AuctionType.None) {
				auction = bestEntDay(inFlight, outFlight, eType);
				log.finer("Adding entertainment " + eType + " on " + auction);
				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
			}
		}
	}

	private Auction bestEntDay(int inFlight, int outFlight, AuctionType type) {
		for (int i = inFlight; i < outFlight; i++) {
			Auction auction = TACAgent.getAuctionFor(AuctionCategory.ENTERTAINMENT,
					type, i);
			if (agent.getAllocation(auction) < agent.getOwn(auction)) {
				return auction;
			}
		}
		// If no left, just take the first...
		return TACAgent.getAuctionFor(AuctionCategory.ENTERTAINMENT,
				type, inFlight);
	}

	private AuctionType nextEntType(int client, AuctionType lastType) {
		int e1 = agent.getClientPreference(client, ClientPreferenceType.E1);
		int e2 = agent.getClientPreference(client, ClientPreferenceType.E2);
		int e3 = agent.getClientPreference(client, ClientPreferenceType.E3);

		// At least buy what each agent wants the most!!!
		if ((e1 > e2) && (e1 > e3) && lastType == AuctionType.None)
			return AuctionType.EVENT_ALLIGATOR_WRESTLING;
		if ((e2 > e1) && (e2 > e3) && lastType == AuctionType.None)
			return AuctionType.EVENT_AMUSEMENT;
		if ((e3 > e1) && (e3 > e2) && lastType == AuctionType.None)
			return AuctionType.EVENT_MUSEUM;
		return AuctionType.None;
	}

}
