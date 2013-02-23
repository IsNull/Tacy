package ch.mas.tacy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.istack.internal.FragmentContentHandler;

import sun.management.resources.agent;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;


/**
 * AuctionInformationManager 
 *	keeps track of the quote development 
 *
 * This class is a singleton
 *
 * @author P. Büttiker
 *
 */
public class AuctionInformationManager {

	/** Maps an auctionId to the history of quotes */
	Map<Auction, List<Quote>> quoteHistory = new HashMap<Auction, List<Quote>>();

	private static AuctionInformationManager instance = new AuctionInformationManager();
	public static AuctionInformationManager instance(){
		return instance;
	}

	private AuctionInformationManager(){

		// init empty history
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			quoteHistory.put(TACAgent.getAuction(i), new ArrayList<Quote>());
		}
	}

	private List<Quote> getHistoryOf(Auction auction){
		return quoteHistory.get(auction);
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


}
