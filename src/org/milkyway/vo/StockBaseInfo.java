package org.milkyway.vo;

public class StockBaseInfo {
	String stockID;
	String stockBelong;
	String stockName;
	String stockCode;
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
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	
	public String toString() {
		return "StockBaseInfo [id=" + stockID + ", name=" + stockName + "]";
	
	}
}
