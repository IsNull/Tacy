package ch.mas.tacy;

import ch.mas.tacy.model.agentware.AgentImpl;
import ch.mas.tacy.model.agentware.Bid;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.util.ArgEnumerator;

/**
 * Tacy - a MAS Agent implementation for BTH
 * 
 * 
 * @author Pascal Büttiker & Eric Neher
 *
 */
public class TacyAgent extends AgentImpl  {

	@Override
	protected void init(ArgEnumerator args) {

	}

	/**
	 * There are TACAgent have received an answer on a bid query/submission
	 *   (new information about the bid is available)
	 */
	@Override
	public void bidUpdated(Bid bid) {

	}

	/**
	 * The bid has been rejected (reason is bid.getRejectReason())
	 */
	@Override
	public void bidRejected(Bid bid) {

	}

	/**
	 * the bid contained errors (error represent error status - commandStatus)
	 */
	@Override
	public void bidError(Bid bid, int error) {

	}

	/**
	 * The TAC game has started, and all information about the
	 * game is available (preferences etc).
	 */
	@Override
	public void gameStarted() {


	}

	@Override
	public void gameStopped() {

	}

	@Override
	public void auctionClosed(int auction) {

	}

	/**
	 * New information about the quotes on the auction (quote.getAuction())
	 *    has arrived
	 * @param quote
	 */
	@Override
	public void quoteUpdated(Quote quote) {
	}

	/**
	 * New information about the quotes on all auctions for the auction
	 * category has arrived (quotes for a specific type of auctions are
	 * often requested at once).
	 * @param auctionCategory
	 */
	@Override
	public void quoteUpdated(int auctionCategory) {
	}

}
