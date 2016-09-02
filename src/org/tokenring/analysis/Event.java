package org.tokenring.analysis;

public class Event {
	String stockID;
	String stockBelong;
	String stockName;
	String eventMsg;
	String exDate;
	int idx;
	
	
	public String getExDate() {
		return exDate;
	}
	public void setExDate(String exDate) {
		this.exDate = exDate;
	}
	
	
	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public String getStockID() {
		return stockID;
	}
	public void setStockID(String stockID) {
		this.stockID = stockID;
	}
	public String getStockBelong() {
		return stockBelong;
	}
	public void setStockBelong(String stockBelong) {
		this.stockBelong = stockBelong;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public String getEventMsg() {
		return eventMsg;
	}
	public void setEventMsg(String eventMsg) {
		this.eventMsg = eventMsg;
	}
	public Event(String StockID,String StockBelong,String StockName,String EventMsg ,int Idx,String ExDate){
		stockID = StockID;
		stockBelong = StockBelong;
		stockName = StockName;
		eventMsg = EventMsg;
		idx = Idx;
		exDate = ExDate;
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
