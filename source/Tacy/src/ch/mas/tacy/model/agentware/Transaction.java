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
 * Updated : $Date: 2005/06/07 19:06:17 $
 *	     $Revision: 1.1 $
 */

package ch.mas.tacy.model.agentware;

public class Transaction {

  private int auction;
  private int quantity;
  private float price;

  public Transaction(int auction, int quantity, float price) {
    this.auction = auction;
    this.price = price;
    this.quantity = quantity;
  }

  public int getQuantity() {
    return quantity;
  }

  public float getPrice() {
    return price;
  }

  public int getAuction() {
    return auction;
  }

} // Transaction