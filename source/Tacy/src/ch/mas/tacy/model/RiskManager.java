package ch.mas.tacy.model;

import java.util.List;

import ch.mas.tacy.model.agentware.Auction;
import ch.mas.tacy.model.agentware.AuctionCategory;
import ch.mas.tacy.model.agentware.AuctionState;



public class RiskManager {
	
	private static RiskManager instance = new RiskManager();
	
	public static RiskManager instance(){
		return instance;
	}

	
	public void getRiskGrading(AuctionInformationManager auctionManager, List<Auction> auctions){
		
		for(Auction auc: auctions){
			if(auc.getCategory().equals(AuctionCategory.HOTEL) && auc.getState() != AuctionState.CLOSED){
			
			}
		}
		
	}
}
