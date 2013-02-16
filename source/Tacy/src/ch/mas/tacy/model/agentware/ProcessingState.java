package ch.mas.tacy.model.agentware;

public enum ProcessingState {


	UNPROCESSED(0,"unprocessed"),
	REJECTED(1,"rejected"),
	VALID(2,"valid"),
	WITHDRAWN(3,"withdrawn"),
	TRANSACTED(4,"transacted"),
	REPLACED(5,"replaced"),
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
