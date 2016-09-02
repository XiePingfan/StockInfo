package org.tokenring.analysis;

public class AnalyzeAverageAmount implements AnalyzeHistory {
	int averageDays;
	int continueDays;
	boolean isBelow;
	StockHistory stockHistory;
	/**
	 * @param AverageDays   多少天均线
	 * @param ContinueDays  连续多少天
	 * @param IsBelow       True 低于，False 高于
	 */
	public AnalyzeAverageAmount(int AverageDays,int ContinueDays,boolean IsBelow,StockHistory StockHistory){
		averageDays = AverageDays;
		continueDays = ContinueDays;
		isBelow = IsBelow;
		stockHistory = StockHistory;
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		boolean result = true;
		int i = 0;
		StockExchangeData sed;
		while ((result) && i < continueDays){
			stockHistory.calcAverageAmount(idx + i, averageDays);
			sed = stockHistory.getHisDataByExDate(idx + i);
			if (sed.getAverageAmount() < 0){
				result = false;
				break;
			}
			if (isBelow){
				result = sed.getExAmount()< sed.getAverageAmount();
			}else{
				result = sed.getExAmount() > sed.getAverageAmount();
			}
			
			i ++;
		}
		
		if (result){
			//连续满足条件
			StringBuffer sb = new StringBuffer();
			sb.append("连续");
			sb.append(continueDays);
			if (isBelow){
				sb.append("低于");
			}else{
				sb.append("高于");
			}
			sb.append(averageDays);
			sb.append("日均成交量");
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					sb.toString(),
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}else{
			return null;
		}
	}

}
