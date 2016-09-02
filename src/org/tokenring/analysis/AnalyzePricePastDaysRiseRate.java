package org.tokenring.analysis;

import org.apache.log4j.Logger;

public class AnalyzePricePastDaysRiseRate implements AnalyzeHistory {
	int days;
	int riseRate;
	
	StockHistory stockHistory;
	
	/**
	 * @param Days   连续多少天
	 * @param RiseRate  一共增长百分之多少，下跌填负数，
	 * @param StockHistory
	 */
	public AnalyzePricePastDaysRiseRate(int Days,int RiseRate, StockHistory StockHistory){
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
			Double baseEndPrice = sedBase.getEndPrice();
			Double pastEndPrice = sedPast.getEndPrice();
			
			boolean bMatch = false;
			
			if (riseRate > 0){
				bMatch = (baseEndPrice - pastEndPrice) >= (pastEndPrice * riseRate/100);
						
			}else{
				bMatch = (baseEndPrice - pastEndPrice) <= (pastEndPrice * riseRate/100);
			}
			if (bMatch){
				StringBuffer sb = new StringBuffer();
				sb.append(days);
				if (riseRate > 0) {
					sb.append("天涨幅");
					sb.append(riseRate);
				}
				else{
					sb.append("天跌幅");
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
			
//			if (exDate.equals("20160324")){
//				Logger log = Logger.getLogger(AnalyzePricePastDaysRiseRate.class);
//				log.error("baseEndPrice = " + baseEndPrice);
//				log.error("pastEndPrice = " + pastEndPrice);
//				log.error("riseRate = " + riseRate);
//				log.error("bMatch = " + bMatch);
//				log.error("bMatch = " + bMatch);
//				log.error("sedPast.exDate = " + sedPast.getExDate());
//			}
		}
		return result;
	}

}
