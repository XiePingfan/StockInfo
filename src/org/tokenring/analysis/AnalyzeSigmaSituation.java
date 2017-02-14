package org.tokenring.analysis;

public class AnalyzeSigmaSituation implements AnalyzeHistory {
	StockHistory stockHistory;
	
	public AnalyzeSigmaSituation(StockHistory StockHistory){
		stockHistory = StockHistory;
		stockHistory.calcAllSigma();
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		StockExchangeData sedToday = stockHistory.getHisDataByExDate(idx);
		StockExchangeData sedYesterday = stockHistory.getHisDataByExDate(idx+1);
		if (sedToday.getSituation() == -2){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"Sigma分析 拐点向下",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}else if (sedToday.getSituation() == 2){
			//if (sedToday.getEndPrice() < sedYesterday.getEndPrice()){
				return new Event(stockHistory.getStockID(),
						stockHistory.getStockBelong(),
						stockHistory.getStockName(),
						"Sigma分析 拐点向上",
						idx,
						stockHistory.getHisDataByExDate(idx).getExDate());
			//}
			
		}
		return null;
	}

}
