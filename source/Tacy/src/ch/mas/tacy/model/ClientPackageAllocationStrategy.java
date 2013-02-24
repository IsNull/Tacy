package ch.mas.tacy.model;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.TACAgent;


public class ClientPackageAllocationStrategy implements IClientPackageAllocationStrategy {



	/**
	 * Assigns the avaiable items to ClientAgents
	 * 
	 */
	@Override
	public void assignItemsToClientPackages(Iterable<ClientAgent> agents, ItemStock avaiableItems) {


		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			Auction auction = TACAgent.getAuction(i);


			for (ClientAgent clientAgent : agents) {
				int wantedQuantity = clientAgent.want(auction);
				if(wantedQuantity > 0 && avaiableItems.getQuantity(auction) > 0){

					int grantedQuantity = Math.min(wantedQuantity, avaiableItems.getQuantity(auction));

					avaiableItems.incrementQuantity(auction, -grantedQuantity); // a negative increment results in a decrement ;)
					clientAgent.onTransaction(auction, grantedQuantity);
				}
			}


		}



	}

}
