package ch.mas.tacy;

import ch.mas.tacy.model.AuctionInformationManager;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.RiskManager;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.TradeMaster;
import archimedesJ.services.ILocator;
import archimedesJ.services.ServiceLocator;




/**
 * This class is singleton
 * @author n0daft
 *
 */
public class Services implements ILocator {
	
	private static final Services instance = new Services();
	private final ServiceLocator locator = new ServiceLocator();
	
	
	private Services(){
	}
	
	public static Services instance(){
		return instance;
	}

	@Override
	public <T> T resolve(Class<T> iclazz) {
		return locator.resolve(iclazz);
	}
	
	
	public void createServices(TACAgent agent){
		locator.registerInstance(ClientManager.class, new ClientManager(agent));
		locator.registerInstance(AuctionInformationManager.class, new AuctionInformationManager(agent));
		locator.registerSingleton(TradeMaster.class, TradeMaster.class);
		locator.registerSingleton(RiskManager.class, RiskManager.class);
		
	}
	
	
	

}
