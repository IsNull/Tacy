package ch.mas.tacy;

import archimedesJ.services.ILocator;
import archimedesJ.services.ServiceLocator;
import ch.mas.tacy.model.ClientManager;
import ch.mas.tacy.model.AuctionRiskManager;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.AuctionInformationManager;
import ch.mas.tacy.model.auctions.TradeMaster;




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
		locator.registerSingleton(AuctionRiskManager.class, AuctionRiskManager.class);
		locator.registerInstance(AuctionInformationManager.class, new AuctionInformationManager(agent));

		locator.registerInstance(TradeMaster.class, new TradeMaster(agent));
		locator.registerInstance(ClientManager.class, new ClientManager(agent));


	}




}
