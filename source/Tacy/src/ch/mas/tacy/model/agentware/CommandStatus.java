package ch.mas.tacy.model.agentware;

/**
 * Command status codes
 * @author P.Büttiker
 *
 */
public enum CommandStatus {

	NO_ERROR(0,"no error"),
	INTERNAL_ERROR(1,"internal error"),
	AGENT_NOT_AUTH(2,"agent not auth"),
	GAME_NOT_FOUND(4,"game not found"),
	NOT_MEMBER_OF_GAME(5,"not member of game"),
	GAME_FUTURE(7,"game future"),
	GAME_COMPLETE(10,"game complete"),
	AUCTION_NOT_FOUND(11,"auction not found"),
	AUCTION_CLOSED(12,"auction closed"),
	BID_NOT_FOUND(13,"bid not found"),
	TRANS_NOT_FOUND(14,"trans not found"),
	CANNOT_WITHDRAW_BID(15,"cannot withdraw bid"),
	BAD_BIDSTRING_FORMAT(16,"bad bidstring format"),
	NOT_SUPPORTED(17,"not supported"),
	GAME_TYPE_NOT_SUPPORTED(18, "game type not supported");





	private final String name;

	/**
	 * Gets the assigned value of this category
	 */
	public final int Value;

	private CommandStatus(int value, String name){
		Value = value;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static CommandStatus byValue(int value){
		for (CommandStatus status : CommandStatus.values()) {
			if(status.Value == value)
				return status;
		}

		return CommandStatus.NO_ERROR;
	}


}
