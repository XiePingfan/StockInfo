package org.milkyway.vo;


import java.util.Date;

public class StockVO {
	Date exDate;
	long exDateTs;
	int situation;
	double open;
	double high;
	double close;
	double low;
	double quantity;
	
	public int getSituation() {
		return situation;
	}
	public void setSituation(int situation) {
		this.situation = situation;
	}
	public long getExDateTs() {
		return exDateTs;
	}
	public void setExDateTs(long exDateTs) {
		this.exDateTs = exDateTs;
	}
	public Date getExDate() {
		return exDate;
	}
	public void setExDate(Date exDate) {
		this.exDate = exDate;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

}
