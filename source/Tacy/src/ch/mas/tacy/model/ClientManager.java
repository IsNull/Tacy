package ch.mas.tacy.model;

import ch.mas.tacy.model.agentware.TACAgent;
import ch.mas.tacy.util.Lists;

/**
 * Manages the client agents
 *
 * 
 * @author P.Büttiker
 *
 */
public class ClientManager {

	private ClientAgent[] clients = new ClientAgent[TACAgent.CLIENT_COUNT];

	private final TACAgent agent;

	public ClientManager(TACAgent agent){
		this.agent = agent;
		initClients();
	}

	/**
	 * 
	 * @param agent
	 */
	private void initClients(){
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

	public void clear() {
		initClients();
	}



}
