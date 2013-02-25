package ch.mas.tacy.model.auctions;

import java.util.HashMap;
import java.util.Map;

import archimedesJ.util.Objects;
import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.Quote;
import ch.mas.tacy.model.agentware.TACAgent;

public class QuoteChangeManager  implements IQuoteChangeListener{

	private static class QuoteTuple{
		private Quote currentQuote;
		private Quote lastSeenQuote;

		public Quote getCurrentQuote() {
			return currentQuote;
		}
		public void setCurrentQuote(Quote currentQuote) {
			this.currentQuote = currentQuote;
		}
		public Quote getLastSeenQuote() {
			return lastSeenQuote;
		}
		public void setLastSeenQuote(Quote lastSeenQuote) {
			this.lastSeenQuote = lastSeenQuote;
		}
	}



	private final Map<Auction, QuoteTuple> quoteSnapshot = new HashMap<Auction, QuoteTuple>();

	public QuoteChangeManager(){
		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);
			quoteSnapshot.put(auction, new QuoteTuple());
		}
	}



	@Override
	public void onQuoteUpdated(Quote quote){
		quoteSnapshot.get(quote.getAuction()).setCurrentQuote(quote);
	}

	/**
	 * Try to visit a new Quote. If a new Quote is avaiable, the Quote gets marked as visited, and true is returned.
	 * If the current quote has been visited before, false is returned.
	 * @param auction
	 * @return
	 */
	public boolean tryVisit(Auction auction){
		if(hasQuoteChanged(auction))
		{
			visitQuote(auction);
			return true;
		}
		return false;
	}


	public boolean hasQuoteChanged(Auction auction){
		QuoteTuple quotes = quoteSnapshot.get(auction);
		return !Objects.equals(quotes.getCurrentQuote(), quotes.getLastSeenQuote());
	}

	public void visitQuote(Auction auction){
		QuoteTuple quotes = quoteSnapshot.get(auction);
		quotes.setLastSeenQuote(quotes.getCurrentQuote());
	}



}
