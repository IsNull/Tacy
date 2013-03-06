package ch.mas.tacy.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.util.Lists;


public class ClientPackageAllocationStrategy implements IClientPackageAllocationStrategy {



	/**
	 * Assigns the avaiable items to ClientAgents
	 * 
	 */
	@Override
	public void assignItemsToClientPackages(Iterable<ClientAgent> agents, AuctionItemStock avaiableItems) {


		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			final Auction auction = TACAgent.getAuction(i);

			List<ClientAgent> prioritizedClients = Lists.newList(agents);

			if(auction.getCategory() == AuctionCategory.ENTERTAINMENT)
			{
				Collections.sort(prioritizedClients, new Comparator<ClientAgent>(){
					@Override
					public int compare(ClientAgent left, ClientAgent right) {

						return Double.compare(
								left.getEntertainmentValue(auction),
								right.getEntertainmentValue(auction)
								);
					}
				});
			}else{
				Collections.sort(prioritizedClients, importanceComparer);
			}


			//
			// depending on the prioritized agents list, assign the items to the ClientAgents
			//
			for (ClientAgent clientAgent : prioritizedClients) {
				int wantedQuantity = clientAgent.want(auction);
				if(wantedQuantity > 0 && avaiableItems.getQuantity(auction) > 0){

					int grantedQuantity = Math.min(wantedQuantity, avaiableItems.getQuantity(auction));

					avaiableItems.incrementQuantity(auction, -grantedQuantity); // a negative increment results in a decrement ;)
					clientAgent.onTransaction(auction, grantedQuantity);
				}
			}
		}
	}

	private Comparator<? super ClientAgent> importanceComparer = new Comparator<ClientAgent>(){
		@Override
		public int compare(ClientAgent left, ClientAgent right) {

			int leftValue = left.getClientPreferences() != null ? left.getClientPreferences().getTripDuration() : 0;
			int rightValue = right.getClientPreferences() != null ? right.getClientPreferences().getTripDuration() : 0;

			return Double.compare(leftValue,rightValue);
		}
	};
}
