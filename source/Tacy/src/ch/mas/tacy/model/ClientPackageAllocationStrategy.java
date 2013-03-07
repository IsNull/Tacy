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
	 * Assigns the available items to ClientAgents
	 * 
	 */
	@Override
	public void assignItemsToClientPackages(Iterable<ClientAgent> agents, AuctionItemStock avaiableItems) {


		for (int i = 0; i < TACAgent.getAuctionNo(); i++) {
			final Auction auction = TACAgent.getAuction(i);

			//
			// Prioritize the ClientAgents depending on the item/auction
			//
			List<ClientAgent> prioritizedClients = Lists.newList(agents);
			Comparator<ClientAgent> comparator = findComparer(auction);
			if(comparator != null)
				Collections.sort(prioritizedClients, comparator);


			//
			// depending on the prioritized agents list, assign the items to the ClientAgents
			//
			for (ClientAgent clientAgent : prioritizedClients) {
				int wantedQuantity = clientAgent.want(auction);

				if(avaiableItems.getQuantity(auction) <= 0) 
					break;

				if(wantedQuantity > 0){
					int grantedQuantity = Math.min(wantedQuantity, avaiableItems.getQuantity(auction));

					avaiableItems.incrementQuantity(auction, -grantedQuantity); // a negative increment results in a decrement ;)
					clientAgent.onTransaction(auction, grantedQuantity);
				}
			}
		}
	}



	private Comparator<ClientAgent> findComparer(final Auction auction){
		Comparator<ClientAgent> comparator = null;
		if(auction.getCategory() == AuctionCategory.ENTERTAINMENT)
		{
			comparator = new Comparator<ClientAgent>(){
				@Override
				public int compare(ClientAgent left, ClientAgent right) {

					return Double.compare(
							left.getEntertainmentValue(auction),
							right.getEntertainmentValue(auction)
							);
				}
			};
		}else{
			comparator = importanceComparer;
		}

		return comparator;
	}

	private Comparator<ClientAgent> importanceComparer = new Comparator<ClientAgent>(){
		@Override
		public int compare(ClientAgent left, ClientAgent right) {

			int leftValue = left.getClientPreferences() != null ? left.getClientPreferences().getTripDuration() : 0;
			int rightValue = right.getClientPreferences() != null ? right.getClientPreferences().getTripDuration() : 0;

			return Double.compare(leftValue,rightValue);
		}
	};
}
