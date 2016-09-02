package org.tokenring.analysis;

public class AnalyzeAmountPastDaysRiseRate implements AnalyzeHistory {
	int days;
	int riseRate;

	StockHistory stockHistory;
	
	/**
	 * @param Days   连续多少天
	 * @param RiseRate   交易量增长百分之多少，下跌填负数
	 * @param StockHistory
	 */
	public AnalyzeAmountPastDaysRiseRate(int Days,int RiseRate, StockHistory StockHistory){
		days = Days;
		riseRate = RiseRate;

		stockHistory = StockHistory;
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		StockExchangeData sedBase = stockHistory.getHisDataByExDate(idx);
		StockExchangeData sedPast = stockHistory.getHisDataByExDate(idx + days);
		String exDate = sedBase.getExDate();
		Event result = null;
		if ( (sedBase != null) && (sedPast != null)){
			int baseExAmount = sedBase.getExAmount();
			int pastExAmount = sedPast.getExAmount();
			
			boolean bMatch = false;
			if (riseRate >0){
				bMatch = ((baseExAmount - pastExAmount) >= (pastExAmount * riseRate/100));
						
			}
			else{
				bMatch = ((baseExAmount - pastExAmount) <= (pastExAmount * riseRate/100));
			}
			
			if (bMatch){
				StringBuffer sb = new StringBuffer();
				sb.append(days);
				if (riseRate > 0) {
					sb.append("天放量");
					sb.append(riseRate);
				}
				else{
					sb.append("天缩量");
					sb.append(-riseRate);
				}
				sb.append("%");
				result = new Event(stockHistory.getStockID(),
						stockHistory.getStockBelong(),
						stockHistory.getStockName(),
						sb.toString(),
						idx,
						exDate);
						
			}
		}
		return result;
	}

}
