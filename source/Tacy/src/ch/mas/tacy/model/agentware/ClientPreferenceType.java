package ch.mas.tacy.model.agentware;


public enum ClientPreferenceType {

	None(-1),

	ARRIVAL(0),
	DEPARTURE(1),
	HOTEL_VALUE(2),
	E1(3),
	E2(4),
	E3(5);


	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private ClientPreferenceType(int value){
		Value = value;
	}



	public static ClientPreferenceType byValue(int value){
		for (ClientPreferenceType status : ClientPreferenceType.values()) {
			if(status.Value == value)
				return status;
		}

		return ClientPreferenceType.None;
	}

}
