package ch.mas.tacy.model;



/**
 * 
 * @author P.Büttiker
 *
 */
public interface IClientPackageAllocationStrategy {

	/**
	 * 
	 * @param agents
	 * @param avaiableItems
	 */
	void assignItemsToClientPackages(Iterable<ClientAgent> agents, AuctionItemStock avaiableItems);

}
