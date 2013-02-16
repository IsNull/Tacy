package ch.mas.tacy.model.agentware;

public enum RejectReason {


	NOT_REJECTED(0, "not rejected"),
	SELF_TRANSACTION(5,"self transaction"),
	BUY_NOT_ALLOWED(7,"buy not allowed"),
	SELL_NOT_ALLOWED(8,"sell not allowed"),
	PRICE_NOT_BEAT(15,"price not beat"),
	ACTIVE_BID_CHANGED(20,"active bid changed"),
	BID_NOT_IMPROVED(21,"bid not improved"),
	BID_NOT_ACTIVE(22,"bid not active");


	private final String name;

	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private RejectReason(int value, String name){
		Value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static RejectReason byValue(int value){
		for (RejectReason status : RejectReason.values()) {
			if(status.Value == value)
				return status;
		}

		return RejectReason.NOT_REJECTED;
	}
}
