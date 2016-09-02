package org.tokenring.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class AssertForcastNextNDaysMaxMRiseR implements AssertForcast {
	Logger log = Logger.getLogger(AssertForcastNextNDaysMaxMRiseR.class);
	int days;
	int rate;
	int max;
	StringBuffer sb;

	public AssertForcastNextNDaysMaxMRiseR(int days, int rate, int max) {
		this.days = days;
		this.rate = rate;
		this.max = max;
		
		sb = new StringBuffer();
		sb.append("未来");
		sb.append(days);
		sb.append("天");
		sb.append("可增长");
		sb.append(rate);
		sb.append("%");
		sb.append("[");
		sb.append(max);
		sb.append("]");
		
	}



	public static void main(String[] args) {
		List<Double> stockHisEndPrices = new ArrayList<Double>();
		Double d = 0.01;
		stockHisEndPrices.add(d);
		d = 0.02;
		stockHisEndPrices.add(d);
		d = 0.003;
		stockHisEndPrices.add(d);
		d = 0.03;
		stockHisEndPrices.add(d);
		// 降序排序
		Collections.sort(stockHisEndPrices);

		System.out.println(stockHisEndPrices.get(stockHisEndPrices.size() - 1));
	}

	@Override
	public AssertEvent doAssert(ExDate exDate, StockHistory stockHistory) {
		// TODO Auto-generated method stub
		List<Double> stockHisEndPrices = new ArrayList<Double>();

		StockExchangeData sedBase = stockHistory.getHisDataByExDate(exDate.getIdx());
		Double baseEndPrice = sedBase.getEndPrice();
		StockExchangeData sedNext;

		int baseIdx = exDate.getIdx();
		for (int i = 1; i <= days; i++) {
			// 未来可能还没到，有空值
			sedNext = stockHistory.getHisDataByExDate(baseIdx - i);
			if (sedNext != null) {
				stockHisEndPrices.add(sedNext.getEndPrice());
			} else {
				// 一旦有空值，没必要继续循环下去
				stockHisEndPrices.add(new Double(0));
			}
		}

		// 降序排序
		Collections.sort(stockHisEndPrices);

		//List 从 0 开始
		Double maxOfEndPrice = stockHisEndPrices.get(stockHisEndPrices.size() - max);
		
//		log.error("maxOfEndPrice = " + maxOfEndPrice);
//		log.error("sedBase.getEndPrice() = " + sedBase.getEndPrice());
//		log.error("baseEndPrice + baseEndPrice * rate / 100 = " + baseEndPrice + baseEndPrice * rate / 100);

		if(maxOfEndPrice > (baseEndPrice + baseEndPrice * rate / 100) ){
			
			AssertEvent ae = new AssertEvent(exDate, sb.toString());
			return ae;
		}else{
			return null;
		}
	
	}
}
