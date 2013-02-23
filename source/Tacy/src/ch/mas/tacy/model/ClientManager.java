package ch.mas.tacy.model;

import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.util.Lists;

/**
 * Manages the client agents
 * 
 * This class is a singleton
 * 
 * @author P.Büttiker
 *
 */
public class ClientManager {

	private ClientAgent[] clients = new ClientAgent[TACAgent.CLIENT_COUNT];

	public ClientManager(TACAgent agent){
		initClients(agent);
	}

	/**
	 * 
	 * @param agent
	 */
	private void initClients(TACAgent agent){
		for (int i = 0; i < clients.length; i++) {
			clients[i] = new ClientAgent(i, agent);
		}
	}

	/**
	 * Gets the client of this id
	 * @param clientId
	 * @return
	 */
	public ClientAgent getClient(int clientId){
		assert clientId >= 0 && clientId < clients.length: "invalid client id"; 
		return clients[clientId];
	}

	/**
	 * Inform all client agents to consider taking action
	 */
	public void pulseAll(){
		for (ClientAgent client : clients) {
			client.pulse();
		}
	}

	public Iterable<ClientAgent> getAllClientAgents(){
		return Lists.asNoNullList(clients);
	}



}
