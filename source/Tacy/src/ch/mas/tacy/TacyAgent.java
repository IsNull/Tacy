package ch.mas.tacy;

import java.util.logging.Logger;

import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.agentware.AgentImpl;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionState;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.CommandStatus;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.agentware.Transaction;
import ch.mas.tacy.model.auctions.AuctionInformationManager;
import ch.mas.tacy.model.auctions.TradeMaster;
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

	private AuctionInformationManager auctionManager; 
	private TradeMaster tradeMaster; 
	private ClientManager clientManager;


	@Override
	protected void init(ArgEnumerator args) {

		Services.instance().createServices(agent);

		auctionManager = Services.instance().resolve(AuctionInformationManager.class);
		tradeMaster = Services.instance().resolve(TradeMaster.class);
		clientManager = Services.instance().resolve(ClientManager.class);
	}

	@Override
	public void transaction(Transaction transaction) {
		tradeMaster.onServerTransaction(transaction);
	}


	@Override
	public void quoteUpdated(Quote quote) {
		auctionManager.onQuoteUpdated(quote);
		clientManager.pulseAll();
		tradeMaster.pulse();

		tradeMaster.onQuoteUpdated(quote);
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
		log.warning("Rejected: " + bid);
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

		// clear all previous data
		/*
		auctionManager.clear();
		clientManager.clear();
		tradeMaster.clear();
		 */

		log.fine("Game " + agent.getGameID() + " started!");
		clientManager.pulseAll();
		tradeMaster.pulse();
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

	@Override
	public void preferencesUpdated() {
		for (ClientAgent ca : clientManager.getAllClientAgents()) {
			ca.updatePreferences();
		}
	}









}
