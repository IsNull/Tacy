package ch.mas.tacy.model.agentware;

public enum ProcessingState {

	/**
	 * If nothing has happend with the bid yet
	 */
	UNPROCESSED(0,"unprocessed"),
	/**
	 * If bid was rejected because of one out of 8 reasons. Specified in RejectReason.java
	 */
	REJECTED(1,"rejected"),
	/**
	 * If a bid fulfills the requirements
	 */
	VALID(2,"valid"),
	/**
	 * If the bit was withdrawn
	 */
	WITHDRAWN(3,"withdrawn"),
	/**
	 * If the bid was matched
	 */
	TRANSACTED(4,"transacted"),
	/**
	 * If the bid was replaced by another bid
	 */
	REPLACED(5,"replaced"),
	/**
	 * If the bid hasn't been matched until the auction was closed
	 */
	EXPIRED(6,"expired");


	private final String name;

	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private ProcessingState(int value, String name){
		Value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static ProcessingState byValue(int value){
		for (ProcessingState status : ProcessingState.values()) {
			if(status.Value == value)
				return status;
		}

		return ProcessingState.UNPROCESSED;
	}
}
