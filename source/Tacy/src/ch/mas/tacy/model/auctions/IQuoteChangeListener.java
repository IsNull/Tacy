package ch.mas.tacy.model.auctions;

import ch.mas.tacy.model.agentware.Quote;

public interface IQuoteChangeListener {
	void onQuoteUpdated(Quote quote);
}
