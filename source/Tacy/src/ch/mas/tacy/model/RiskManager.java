package ch.mas.tacy.model;

import java.util.HashMap;
import java.util.List;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionState;



public class RiskManager {



	public void getRiskGrading(AuctionInformationManager auctionManager, List<Auction> auctions){
		HashMap<Auction, Float> map = new HashMap<Auction, Float>();

		for(Auction auc: auctions){
			if(auc.getCategory().equals(AuctionCategory.HOTEL) && auc.getState() != AuctionState.CLOSED){
				map.put(auc, auctionManager.getPriceGrowthByRelation(auc));

			}
		}

	}
}
