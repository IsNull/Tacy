package ch.mas.tacy.model.auctions;

import ch.mas.tacy.model.agentware.Quote;

/**
 * An event listener for Quote changes
 * @author P.Büttiker
 *
 */
public interface IQuoteChangeListener {
	void onQuoteUpdated(Quote quote);
}
