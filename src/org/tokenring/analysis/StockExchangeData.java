package org.tokenring.analysis;

public class StockExchangeData {
	String ExDate;
	Double BeginPrice;
	Double HighestPrice;
	Double EndPrice;
	Double LowestPrice;
	int ExQuantity;
	int ExAmount;
	Double averagePrice;
	Double averageQuantity;
	Double averageAmount;
	
	//EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
	Double EMA12;
	//EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
	Double EMA26;
	//DIF = EMA（12） - EMA（26）
	Double DIF;
	//DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
	Double DEA;
	//（DIF-DEA）*2即为MACD柱状图
	Double MACD;
	/*
	 * 投资者参考：
1.当DIF和MACD均大于0(即在图形上表示为它们处于零线以上)并向上移动时，一般表示为行情处于多头行情中，可以买入开仓或多头持仓；
2.当DIF和MACD均小于0(即在图形上表示为它们处于零线以下)并向下移动时，一般表示为行情处于空头行情中，可以卖出开仓或观望。
3.当DIF和MACD均大于0(即在图形上表示为它们处于零线以上)但都向下移动时，一般表示为行情处于下跌阶段，可以卖出开仓和观望；
4.当DIF和MACD均小于0时(即在图形上表示为它们处于零线以下)但向上移动时，一般表示为行情即将上涨，股票将上涨，可以买入开仓或多头持仓。

其买卖原则为：
1.DIF、DEA均为正，DIF向上突破DEA，买入信号参考。
2.DIF、DEA均为负，DIF向下跌破DEA，卖出信号参考。
3.DIF线与K线发生背离，行情可能出现反转信号。
4.DIF、DEA的值从正数变成负数，或者从负数变成正数并不是交易信号，因为它们落后于市场。
	*/
	 
	Double myEMA12;
	Double myEMA26;
	Double myDIF;
	Double myDEA;
	Double myMACD;
	
	public Double getAveragePrice() {
		return averagePrice;
	}
	public void setAveragePrice(Double averagePrice) {
		this.averagePrice = averagePrice;
	}
	public Double getAverageQuantity() {
		return averageQuantity;
	}
	public void setAverageQuantity(Double averageQuantity) {
		this.averageQuantity = averageQuantity;
	}
	public Double getAverageAmount() {
		return averageAmount;
	}
	public void setAverageAmount(Double averageAmount) {
		this.averageAmount = averageAmount;
	}
	public String getExDate() {
		return ExDate;
	}
	public void setExDate(String exDate) {
		ExDate = exDate;
	}
	public Double getBeginPrice() {
		return BeginPrice;
	}
	public void setBeginPrice(Double beginPrice) {
		BeginPrice = beginPrice;
	}
	public Double getHighestPrice() {
		return HighestPrice;
	}
	public void setHighestPrice(Double highestPrice) {
		HighestPrice = highestPrice;
	}
	public Double getEndPrice() {
		return EndPrice;
	}
	public void setEndPrice(Double endPrice) {
		EndPrice = endPrice;
	}
	public Double getLowestPrice() {
		return LowestPrice;
	}
	public void setLowestPrice(Double lowestPrice) {
		LowestPrice = lowestPrice;
	}
	public int getExQuantity() {
		return ExQuantity;
	}
	public void setExQuantity(int exQuantity) {
		ExQuantity = exQuantity;
	}
	public int getExAmount() {
		return ExAmount;
	}
	public void setExAmount(int exAmount) {
		ExAmount = exAmount;
	}
	public Double getEMA12() {
		return EMA12;
	}
	public void setEMA12(Double eMA12) {
		EMA12 = eMA12;
	}
	public Double getEMA26() {
		return EMA26;
	}
	public void setEMA26(Double eMA26) {
		EMA26 = eMA26;
	}
	public Double getDIF() {
		return DIF;
	}
	public void setDIF(Double dIF) {
		DIF = dIF;
	}
	public Double getDEA() {
		return DEA;
	}
	public void setDEA(Double dEA) {
		DEA = dEA;
	}
	public Double getMACD() {
		return MACD;
	}
	public void setMACD(Double mACD) {
		MACD = mACD;
	}

	public StockExchangeData(String ExDate,Double BeginPrice,Double HighestPrice,Double EndPrice,Double LowestPrice,int ExQuantity,int ExAmount){
		this.ExDate = ExDate;
		this.BeginPrice = BeginPrice;
		this.HighestPrice = HighestPrice;
		this.EndPrice = EndPrice;
		this.LowestPrice = LowestPrice;
		this.ExQuantity = ExQuantity;
		this.ExAmount = ExAmount;
	}
	public Double getMyEMA12() {
		return myEMA12;
	}
	public void setMyEMA12(Double myEMA12) {
		this.myEMA12 = myEMA12;
	}
	public Double getMyEMA26() {
		return myEMA26;
	}
	public void setMyEMA26(Double myEMA26) {
		this.myEMA26 = myEMA26;
	}
	public Double getMyDIF() {
		return myDIF;
	}
	public void setMyDIF(Double myDIF) {
		this.myDIF = myDIF;
	}
	public Double getMyDEA() {
		return myDEA;
	}
	public void setMyDEA(Double myDEA) {
		this.myDEA = myDEA;
	}
	public Double getMyMACD() {
		return myMACD;
	}
	public void setMyMACD(Double myMACD) {
		this.myMACD = myMACD;
	}
}
