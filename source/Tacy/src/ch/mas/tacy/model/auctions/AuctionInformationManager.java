package ch.mas.tacy.model.auctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;


/**
 * AuctionInformationManager 
 *	keeps track of the quote development 
 *
 *
 * @author P. Büttiker
 *
 */
public class AuctionInformationManager {

	private final TACAgent agent;

	private final List<IQuoteChangeListener> quoteChangeListeners = new ArrayList<IQuoteChangeListener>();

	/** Maps an auctionId to the history of quotes */
	Map<Auction, List<Quote>> quoteHistory = new HashMap<Auction, List<Quote>>();



	public AuctionInformationManager(TACAgent agent){

		this.agent = agent;

		// init empty history
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			quoteHistory.put(TACAgent.getAuction(i), new ArrayList<Quote>());
		}
	}

	private List<Quote> getHistoryOf(Auction auction){
		return quoteHistory.get(auction);
	}

	public void registerQuoteChangeListener(IQuoteChangeListener listener){
		quoteChangeListeners.add(listener);
	}

	/**
	 * Occurs when a Quote has been updated
	 * @param quote
	 */
	public void onQuoteUpdated(Quote quote){
		List<Quote> history = getHistoryOf(quote.getAuction());
		if(history != null){
			history.add(quote);
		}

		for (IQuoteChangeListener listener : quoteChangeListeners) {
			listener.onQuoteUpdated(quote);
		}
	}



	/**
	 * Returns the last quote of the given auction.
	 * If there is no quote available, null is returned.
	 * @param auctionId
	 * @return
	 */
	public Quote getCurrentQuote(Auction auction){
		List<Quote> history = getHistoryOf(auction);
		return history != null && !history.isEmpty() ? history.get(history.size()-1) : null;
	}

	/**
	 * Returns true if the ask price of the auction has increased since the initial quote by the given value
	 * @param auction
	 * @param value
	 * @return
	 */
	public boolean getPriceGrowByValue(Auction auction, int value){
		List<Quote> history = getHistoryOf(auction);

		if(history != null && !history.isEmpty()){
			Quote firstQuote = history.get(0);
			Quote currentQuote = this.getCurrentQuote(auction);
			return (currentQuote.getAskPrice() - firstQuote.getAskPrice()) >= value;
		}

		return false;
	}

	/**
	 * Returns the price growth of the given auction from the initial quote to the current quote in percent
	 * @param auction
	 * @return
	 */
	public float getPriceGrowthByRelation(Auction auction) throws NullPointerException {

		List<Quote> history = getHistoryOf(auction);


		if(history != null && !history.isEmpty()){
			Quote firstQuote = history.get(0);
			Quote currentQuote = this.getCurrentQuote(auction);

			float difference = currentQuote.getAskPrice() - firstQuote.getAskPrice();
			return 100f / firstQuote.getAskPrice() * difference;

		}

		return Float.MAX_VALUE;
	}

	/**
	 * Returns the bid rate of the given auction (bids per second)
	 * We consider auctions with a high bid rate as more risky
	 * @param auction
	 * @return
	 */
	public float getBidRate(Auction auction){
		float bidRate = 0;
		List<Quote> history = getHistoryOf(auction);

		float amountOfBids = history.size();
		float passedTime = agent.getGameTime() / 1000;


		bidRate = amountOfBids / passedTime;

		return bidRate;
	}


}
