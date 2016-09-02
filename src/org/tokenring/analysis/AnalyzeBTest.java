package org.tokenring.analysis;

public class AnalyzeBTest implements AnalyzeHistory {
	StockHistory stockHistory;
	AnalyzeBTest (StockHistory stockHistory){
		this.stockHistory = stockHistory;
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		return new Event(stockHistory.getStockID(),
				stockHistory.getStockBelong(),
				stockHistory.getStockName(),
				"BTest",
				idx,
				stockHistory.getHisDataByExDate(idx).getExDate());
	}

}
