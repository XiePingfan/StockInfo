package org.tokenring.analysis;

public class AnalyzeAmountPastDaysContinueRiseRate implements AnalyzeHistory {
	int days;
	int riseRate;

	StockHistory stockHistory;
	public AnalyzeAmountPastDaysContinueRiseRate(int Days,int RiseRate,StockHistory StockHistory){
		days = Days;
		riseRate = RiseRate;
		
		stockHistory = StockHistory;
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		//检查是否连续x天放量y%
		
		StockExchangeData sedBase = stockHistory.getHisDataByExDate(idx);
		StockExchangeData sedPast = stockHistory.getHisDataByExDate(idx + days);
		
		String exDate = sedBase.getExDate();
		Event result = null;
		if ( (sedBase != null) && (sedPast != null)){
			int baseExAmount;
			int pastExAmount;
			boolean isRise = true;
			for (int i = 1;isRise && (i <= days);i++){
				sedBase = stockHistory.getHisDataByExDate(idx + i - 1);
				sedPast = stockHistory.getHisDataByExDate(idx + i);
				
				baseExAmount = sedBase.getExAmount();
				pastExAmount = sedPast.getExAmount();
				
				if (riseRate > 0){
					isRise = (baseExAmount - pastExAmount) >= (pastExAmount * riseRate/100);
							
				}else{
					isRise = (baseExAmount - pastExAmount) <= (pastExAmount * riseRate/100);
				}
				
			}
			
			
			if (isRise){
				StringBuffer sb = new StringBuffer();
				sb.append("连续 ");
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
