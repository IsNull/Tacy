package ch.mas.tacy.model;

import java.util.List;


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
	void assignItemsToClientPackages(List<ClientAgent> agents, ItemStock avaiableItems);

}
