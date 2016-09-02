package org.tokenring.analysis;

public class TempObj {
	String StockID;
	String ExDate;
	double BeginPrice;
	double HighestPrice;
	double EndPrice;
	double LowestPrice;
	double ExQuantity;
	double AdjRate;
	double ExAmount;
	double WinPower;
	double WinPowerScore;
	String isWin;
	public TempObj(String stockID, String exDate, double beginPrice, double highestPrice, double endPrice,
			double lowestPrice, double exQuantity, double adjRate, double exAmount,double winPower) {
		
		StockID = stockID;
		ExDate = exDate;
		BeginPrice = beginPrice;
		HighestPrice = highestPrice;
		EndPrice = endPrice;
		LowestPrice = lowestPrice;
		ExQuantity = exQuantity;
		AdjRate = adjRate;
		ExAmount = exAmount;
		WinPower = winPower;
		WinPowerScore = 0;
		this.isWin = "";
	}
	public String getStockID() {
		return StockID;
	}
	public void setStockID(String stockID) {
		StockID = stockID;
	}
	public String getExDate() {
		return ExDate;
	}
	public void setExDate(String exDate) {
		ExDate = exDate;
	}
	public double getBeginPrice() {
		return BeginPrice;
	}
	public void setBeginPrice(double beginPrice) {
		BeginPrice = beginPrice;
	}
	public double getHighestPrice() {
		return HighestPrice;
	}
	public void setHighestPrice(double highestPrice) {
		HighestPrice = highestPrice;
	}
	public double getEndPrice() {
		return EndPrice;
	}
	public void setEndPrice(double endPrice) {
		EndPrice = endPrice;
	}
	public double getLowestPrice() {
		return LowestPrice;
	}
	public void setLowestPrice(double lowestPrice) {
		LowestPrice = lowestPrice;
	}
	public double getExQuantity() {
		return ExQuantity;
	}
	public void setExQuantity(double exQuantity) {
		ExQuantity = exQuantity;
	}
	public double getAdjRate() {
		return AdjRate;
	}
	public void setAdjRate(double adjRate) {
		AdjRate = adjRate;
	}
	public double getExAmount() {
		return ExAmount;
	}
	public void setExAmount(double exAmount) {
		ExAmount = exAmount;
	}
	public double getWinPower() {
		return WinPower;
	}
	public void setWinPower(double winPower) {
		WinPower = winPower;
	}
	public double getWinPowerScore() {
		return WinPowerScore;
	}
	public void setWinPowerScore(double winPowerScore) {
		WinPowerScore = winPowerScore;
	}
	public String getIsWin() {
		return isWin;
	}
	public void setIsWin(String isWin) {
		this.isWin = isWin;
	}
	
}
