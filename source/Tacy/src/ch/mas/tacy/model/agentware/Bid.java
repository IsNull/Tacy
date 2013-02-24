/**
 * TAC AgentWare
 * http://www.sics.se/tac        tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 23 April, 2002
 * Updated : $Date: 2005/06/07 19:06:15 $
 *	     $Revision: 1.1 $
 */

package ch.mas.tacy.model.agentware;
import java.util.StringTokenizer;

/**
 * a bid could look like this: ((2 4) (-6 6)) where the first value of each pair describes weather it is buy bid (positive value) or a sell bid (negative value).
 * The second value determines in every case how many items are tried to be bought or selled
 * @author n0daft
 *
 */
public class Bid {

	public final static String EMPTY_BID_STRING = "()";
	public final static int NO_ID = -1;
	private final static int INCREMENT = 10;


	private final Auction auction;
	private int id = NO_ID;
	private RejectReason rejectReason;
	private String bidHash;
	private ProcessingState processingState = ProcessingState.UNPROCESSED;

	private String bidString;

	private long timeProcessed;
	private long timeClosed;

	private int len;
	private int[] quantity;
	private float[] price;

	private Bid replacing;
	private long timeSubmitted = 0L;

	// Transaction clearing
	private int clearID = -1;
	private String clearHash;
	private String clearString;
	private int clearQuantity;

	public Bid(Auction auction) {
		this.auction = auction;
	}

	public Bid(Bid oldBid) {
		this.auction = oldBid.auction;
	}

	Bid(Bid oldBid, String bidString, String bidHash) {
		this.id = oldBid.id;
		this.auction = oldBid.auction;
		this.bidString = bidString;
		this.bidHash = bidHash;
		this.rejectReason = oldBid.rejectReason;
		this.processingState = oldBid.processingState;
		this.timeProcessed = oldBid.timeProcessed;
		this.timeClosed = timeClosed;
		this.timeSubmitted = timeSubmitted;
		parseBidString(bidString);
	}

	// Should this be public? FIX THIS!!
	private boolean isSubmitted() {
		return timeSubmitted > 0;
	}

	void submitted() {
		if (timeSubmitted > 0) {
			throw new IllegalStateException("Bid already submitted");
		}
		timeSubmitted = System.currentTimeMillis();
	}

	void setID(int bidID) {
		if (id != NO_ID) {
			throw new IllegalStateException("Bid ID already set " + id);
		}
		id = bidID;
	}

	void setRejectReason(RejectReason reason) {
		rejectReason = reason;
	}

	// This bid is replacing the included bid
	// It is not neccesary so that this bid is replaced by replaceBid
	// - it can also be a submitBid
	void setReplacing(Bid bid) {
		replacing = bid;
	}

	public Bid getReplacing() {
		return replacing;
	}

	// Only used when recovering bids
	void setBidHash(String hash) {
		bidHash = hash;
	}

	public String getBidHash() {
		return bidHash;
	}

	void setTimeProcessed(long time) {
		timeProcessed = time * 1000;
	}

	public long getTimeProcessed() {
		return timeProcessed;
	}

	void setTimeClosed(long time) {
		timeClosed = time * 1000;
	}

	public long getTimeClosed() {
		return timeClosed;
	}

	void setProcessingState(ProcessingState state) {
		processingState = state;
	}

	public ProcessingState getProcessingState() {
		return processingState;
	}


	public boolean isPreliminary() {
		return (id == NO_ID || processingState == ProcessingState.UNPROCESSED);
	}

	public boolean isRejected() {
		return rejectReason != RejectReason.NOT_REJECTED;
	}

	public RejectReason getRejectReason() {
		return rejectReason;
	}

	public Auction getAuction() {
		return auction;
	}

	public int getID() {
		return id;
	}

	public synchronized void addBidPoint(int quantity, float unitPrice) {
		if (isSubmitted()) {
			throw new IllegalStateException("Bid already submitted");
		}
		if (unitPrice < 0) {
			throw new IllegalArgumentException("Negative price not allowed");
		}
		// This is a "trick" for checking that this auction allow "sell"
		if (auction.getId() < TACAgent.MIN_ENTERTAINMENT && quantity < 0) {
			throw new IllegalArgumentException("Not allowed to sell in auction " +
					auction);
		}

		realloc();
		this.quantity[len] = quantity;
		this.price[len++] = unitPrice;
		this.bidString = null;
	}

	public int getNoBidPoints() {
		return len;
	}

	public int getQuantity() {
		int len = this.len;
		int[] quant = quantity;
		int q = 0;
		if (quant != null) {
			for (int i = 0; i < len; i++) {
				q += quant[i];
			}
		}
		return q;
	}

	/**
	 * Returns the highest bit price
	 * @return
	 */
	public float getMaxPrice() {
		int len = this.len;
		int[] quant = quantity;
		float maxPrice = 0;
		if (price != null) {
			for (int i = 0; i < len; i++) {
				maxPrice = Math.max(maxPrice, price[i]);
			}
		}
		return maxPrice;
	}

	public int getQuantity(int index) {
		if (quantity == null) {
			throw new IndexOutOfBoundsException("Index: " + index
					+ ", Size: " + len);
		}
		return quantity[index];
	}

	public float getPrice(int index) {
		if (price == null) {
			throw new IndexOutOfBoundsException("Index: " + index
					+ ", Size: " + len);
		}
		return price[index];
	}

	public String getBidString() {
		String bidString = this.bidString;
		if (bidString == null){
			StringBuffer bid = new StringBuffer();
			bid.append('(');
			for (int i = 0; i < len; i++) {
				bid.append('(').append(quantity[i]).append(' ').
				append(price[i]).append(')');
			}
			bid.append(')');
			this.bidString = bidString = bid.toString();
		}
		return bidString;
	}

	// Only used when recovering bids
	void setBidString(String bidString) {
		this.bidString = bidString;
		parseBidString(bidString);
	}

	public boolean same(Bid bid) {
		return this == bid || ((bid != null && id == bid.id) && (id != NO_ID));
	}

	void setBidTransacted(int clearID, String bidHash, String bidString) {
		this.clearID = clearID;
		this.clearHash = bidHash;
		this.clearString = bidString;
	}

	int getClearID() {
		return clearID;
	}

	String getClearString() {
		return clearString;
	}

	String getClearHash() {
		return clearHash;
	}

	public boolean isAwaitingTransactions() {
		return clearID >= 0;
	}

	private synchronized void realloc() {
		if (quantity == null) {
			quantity = new int[INCREMENT];
			price = new float[INCREMENT];
		} else if (len == quantity.length) {
			int[] tmp = new int[len + INCREMENT];
			System.arraycopy(quantity, 0, tmp, 0, len);

			float[] tmp2 = new float[len + INCREMENT];
			System.arraycopy(price, 0, tmp2, 0, len);

			quantity = tmp;
			price = tmp2;
		}
	}

	private void parseBidString(String bidString) {
		StringTokenizer tok = new StringTokenizer(bidString, "() \t\r\n");
		while (tok.hasMoreTokens()) {
			int q = (int) Float.parseFloat(tok.nextToken());
			float p = Float.parseFloat(tok.nextToken());
			addBidPoint(q, p);
		}
	}

	static ProcessingState mapProcessingState(int state) {
		if (state == 7) {
			return ProcessingState.TRANSACTED;
		} else if (state == 9) {
			return ProcessingState.UNPROCESSED;
		}
		return ProcessingState.byValue(state);
	}

	static RejectReason mapRejectReason(int state) {
		switch (state) {
		case 1:
			return RejectReason.ACTIVE_BID_CHANGED;
		case 11:
		case 12:
		case 13:
		case 14:
			return RejectReason.BID_NOT_IMPROVED;
		case 15:
		case 16:
			return RejectReason.PRICE_NOT_BEAT;
		default:
			return RejectReason.byValue(state);
		}
	}
}
