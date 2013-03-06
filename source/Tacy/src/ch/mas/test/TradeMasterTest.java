package ch.mas.test;

import ch.mas.tacy.Services;
import ch.mas.tacy.model.ClientAgent;
import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.model.auctions.TradeMaster;

public class TradeMasterTest {



	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Services.instance().createServices(null);

		ClientAgent client = new ClientAgent(0, null);
		TradeMaster tradeMaster = Services.instance().resolve(TradeMaster.class);


		tradeMaster.updateRequestedItem(client, TACAgent.getAuction(0), 2, 12);
		tradeMaster.pulse();


		tradeMaster.updateRequestedItem(client, TACAgent.getAuction(4), 1, 34);
		tradeMaster.updateRequestedItem(client, TACAgent.getAuction(0), 1, 12);
		tradeMaster.pulse();

		tradeMaster.updateRequestedItem(client, TACAgent.getAuction(20), -2, -1);
		tradeMaster.updateRequestedItem(client, TACAgent.getAuction(21), 23, -1);
		tradeMaster.pulse();



	}

}
