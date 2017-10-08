package org.tokenring.analysis;

public class AnalyzeKDJ implements AnalyzeHistory {
	StockHistory stockHistory;
	
	public AnalyzeKDJ(StockHistory StockHistory){
		stockHistory = StockHistory;
		stockHistory.calcKDJ();
	}
	
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		StockExchangeData sedToday = stockHistory.getHisDataByExDate(idx);
		StockExchangeData sedYesterday = stockHistory.getHisDataByExDate(idx + 1);
		
		if((sedToday == null) || (sedYesterday == null)){
			return null;
		}
		if ((sedYesterday.getK() < sedYesterday.getD()) && (sedToday.getK() > sedToday.getD()) && (sedToday.getJ() >= 80)){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"KDJ½ð²æ",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}else if  ((sedYesterday.getK() > sedYesterday.getD()) && (sedToday.getK() < sedToday.getD()) && (sedToday.getJ() <= 20)){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"KDJËÀ²æ",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}
		return null;
	}

}
