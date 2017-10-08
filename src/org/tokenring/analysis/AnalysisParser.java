package org.tokenring.analysis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;
import org.tokenring.util.DataFile;

class InnerCount {
	public int wins;
	public int losts;

	public InnerCount() {
		wins = 0;
		losts = 0;
	}
}

public class AnalysisParser {
	Logger log = Logger.getLogger(AnalysisParser.class);
	StockHistory stockHistory;

	public StockHistory getStockHistory() {
		return stockHistory;
	}

	List<AnalyzeHistory> analysisChain;
	List<AssertForcast> assertChain;
	List<Event> events;
	List<AssertEvent> assertEvents;
	List<ExDate> exDates;

	double rate = 0.0;

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public AnalysisParser(String StockID, String StockBelong) throws SQLException {
		stockHistory = new StockHistory(StockID, StockBelong);

		// 构建分析器
		analysisChain = new ArrayList<AnalyzeHistory>();

		// 构建验证器
		assertChain = new ArrayList<AssertForcast>();

		// 初始化事件接受器
		events = new ArrayList<Event>();

		// 初始化验证事件接收器
		assertEvents = new ArrayList<AssertEvent>();

		// 初始化交易数据列表
		exDates = new ArrayList<ExDate>();
		int hisSize = stockHistory.getHisData().size();

		List<StockExchangeData> sedList = stockHistory.getHisData();
		ExDate ed;
		for (int idx = 0; idx < hisSize; idx++) {
			// String stockID,String stockBelong,String stockName,String
			// exDate,int idx
			StockExchangeData sed = sedList.get(idx);
			ed = new ExDate(stockHistory.getStockID(), stockHistory.getStockBelong(), stockHistory.getStockName(),
					sed.getExDate(), idx);
			exDates.add(ed);
		}
	}

	public void addParser(AnalyzeHistory ah) {
		if (!analysisChain.contains(ah)) {
			analysisChain.add(ah);
		}
	}

	public void prepareParser() {
		AnalyzeHistory fiveDaysPriceRiseTwentyPcts = new AnalyzePricePastDaysRiseRate(5, 20, stockHistory);
		analysisChain.add(fiveDaysPriceRiseTwentyPcts);

		AnalyzeHistory fiveDaysPriceFallTwentyPcts = new AnalyzePricePastDaysRiseRate(5, -20, stockHistory);
		analysisChain.add(fiveDaysPriceFallTwentyPcts);

		AnalyzeHistory threeDaysAmountRiseTenPcts = new AnalyzeAmountPastDaysRiseRate(3, 10, stockHistory);
		analysisChain.add(threeDaysAmountRiseTenPcts);

		AnalyzeHistory threeDaysAmountFallTenPcts = new AnalyzeAmountPastDaysRiseRate(3, -10, stockHistory);
		analysisChain.add(threeDaysAmountFallTenPcts);

		AnalyzeHistory fiveDaysPriceAndAmountRise = new AnalyzePriceAndAmountPastDaysContinueRiseRate(5, 1,
				stockHistory);
		analysisChain.add(fiveDaysPriceAndAmountRise);

		AnalyzeHistory fiveDaysPriceAndAmountFall = new AnalyzePriceAndAmountPastDaysContinueRiseRate(5, -1,
				stockHistory);
		analysisChain.add(fiveDaysPriceAndAmountFall);

		AnalyzeHistory fiveDaysAbove60AveragePrice = new AnalyzeAveragePrice(60, 5, false, stockHistory);
		analysisChain.add(fiveDaysAbove60AveragePrice);

		AnalyzeHistory fiveDaysBelow60AveragePrice = new AnalyzeAveragePrice(60, 5, true, stockHistory);
		analysisChain.add(fiveDaysBelow60AveragePrice);

		AnalyzeHistory fiveDaysAbove60AverageAmount = new AnalyzeAverageAmount(60, 5, false, stockHistory);
		analysisChain.add(fiveDaysAbove60AverageAmount);

		AnalyzeHistory fiveDaysBelow60AverageAmount = new AnalyzeAverageAmount(60, 5, true, stockHistory);
		analysisChain.add(fiveDaysBelow60AverageAmount);
	}

	public void addAssert(AssertForcast af) {
		if (!this.assertChain.contains(af)) {
			assertChain.add(af);
		}
	}

	public void prepareAssertForcast() {
		// int days, int rate, int max
		// AssertForcastNextNDaysMaxMRiseR nextDaysRise = new
		// AssertForcastNextNDaysMaxMRiseR(1, 1, 1);
		// assertChain.add(nextDaysRise);

		AssertForcastNextNDaysMaxMRiseR next3DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(3, 10, 1);
		// assertChain.add(next3DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next5DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(5, 10, 1);
		// assertChain.add(next5DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next10DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(10, 10, 1);
		// assertChain.add(next10DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next20DaysRise15 = new AssertForcastNextNDaysMaxMRiseR(30, 10, 3);
		assertChain.add(next20DaysRise15);
		/*
		 * AssertForcastNextNDaysMaxMRiseR next30DaysRise20 = new
		 * AssertForcastNextNDaysMaxMRiseR(30, 15, 2);
		 * assertChain.add(next30DaysRise20);
		 * 
		 * AssertForcastNextNDaysMaxMRiseR next60DaysRise20 = new
		 * AssertForcastNextNDaysMaxMRiseR(60, 20, 3);
		 * assertChain.add(next60DaysRise20);
		 * 
		 * AssertForcastNextNDaysMaxMRiseR next90DaysRise30 = new
		 * AssertForcastNextNDaysMaxMRiseR(90, 30, 3);
		 * assertChain.add(next90DaysRise30);
		 */
	}

	public void doAnalyze() {
		int hisSize = stockHistory.getHisData().size();
		// List<StockExchangeData> sedList = stockHistory.getHisData();
		Event e;
		for (int idx = 0; idx < hisSize; idx++) {

			Iterator<AnalyzeHistory> iter = analysisChain.iterator();
			while (iter.hasNext()) {
				AnalyzeHistory ah = iter.next();
				e = ah.doAnalzy(idx);
				if (e != null) {
					events.add(e);
					exDates.get(idx).getEvents().add(e);
				}
			}
		}
		// log.error("events = " + events.size());
	}

	public void doAnalyzeToday() {

		Event e;

		Iterator<AnalyzeHistory> iter = analysisChain.iterator();
		while (iter.hasNext()) {
			AnalyzeHistory ah = iter.next();
			e = ah.doAnalzy(0);
			if (e != null) {
				events.add(e);
				exDates.get(0).getEvents().add(e);
			}
		}

		// log.error("events = " + events.size());
	}

	public void doAssert() {
		ExDate e;
		AssertEvent ae;
		AssertForcast af;

		// 待验证事件迭代器
		Iterator<ExDate> iterExDate = exDates.iterator();
		while (iterExDate.hasNext()) {
			// 待验证的历史事件
			e = iterExDate.next();

			// 验证器迭代器
			Iterator<AssertForcast> iterAssert = assertChain.iterator();
			while (iterAssert.hasNext()) {
				af = iterAssert.next();
				// 验证一下历史事件
				ae = af.doAssert(e, this.stockHistory);
				if (ae != null) {
					assertEvents.add(ae);
					e.getAssertEvents().add(ae);
					e.setWin(true);
				}
			}
		}
		// log.error("assertChain = " + assertChain.size());
		// log.error("assertEvents = " + assertEvents.size());

	}

	public void printAll() {
		Map<String, InnerCount> mapAH = new HashMap<String, InnerCount>();

		Iterator<ExDate> iterEx = exDates.iterator();
		ExDate ex;
		Event e;

		while (iterEx.hasNext()) {
			ex = iterEx.next();
			Iterator<Event> iterEvent = ex.events.iterator();
			while (iterEvent.hasNext()) {
				e = iterEvent.next();

				StringBuffer sb = new StringBuffer();
				sb.append("[");
				sb.append(ex.stockID);
				sb.append("][");
				sb.append(e.eventMsg);
				sb.append("][");
				sb.append(e.exDate);
				sb.append("]");
				// log.info(sb.toString());

				if (mapAH.containsKey(e.eventMsg)) {
					InnerCount ic = (InnerCount) mapAH.get(e.eventMsg);
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				} else {
					InnerCount ic = new InnerCount();
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				}
			}
		}

		// print mapAH msg;
		Iterator entries = mapAH.entrySet().iterator();
		DecimalFormat df = new DecimalFormat("######0.00");

		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String key = (String) entry.getKey();

			InnerCount value = (InnerCount) entry.getValue();

			StringBuffer sb = new StringBuffer();
			double winRate = (double) value.wins / (value.wins + value.losts);
			double rate = (double) (value.wins + value.losts) / this.exDates.size();

			sb.append("[");
			sb.append(key);
			sb.append("]");
			sb.append("[");
			sb.append("exDates.size()= ");
			sb.append(exDates.size());
			sb.append("]");
			sb.append("[wins = ");
			sb.append(value.wins);
			sb.append("][losts = ");
			sb.append(value.losts);
			sb.append("][winrate = ");
			if (value.losts > 0) {
				sb.append(df.format(winRate * 100));
			} else {
				sb.append(100);
			}
			sb.append("%]");

			log.info(sb.toString());

		}
	}

	public double printAnalyze(String eventName) {
		Map<String, InnerCount> mapAH = new HashMap<String, InnerCount>();
		// add
		InnerCount icMACD2 = new InnerCount();
		icMACD2.losts = 0;
		icMACD2.wins = 0;
		mapAH.put("MyMACD+MACD", icMACD2);
		// add end

		Iterator<ExDate> iterEx = exDates.iterator();
		ExDate ex;
		Event e;
		boolean bFindMACD = false;// add
		boolean bFindMyMACD = false;// add

		while (iterEx.hasNext()) {
			ex = iterEx.next();
			Iterator<Event> iterEvent = ex.events.iterator();

			bFindMACD = false;// add
			bFindMyMACD = false;// add

			while (iterEvent.hasNext()) {
				e = iterEvent.next();

				if ("MyMACD 向上突破".equals(e.eventMsg)) {
					bFindMyMACD = true;
				}

				if ("MACD 向上突破".equals(e.eventMsg)) {
					bFindMACD = true;
				}

				if (bFindMACD && bFindMyMACD) {
					icMACD2 = (InnerCount) mapAH.get("MyMACD+MACD");
					if (ex.isWin) {
						icMACD2.wins++;
					} else {
						icMACD2.losts++;
					}
					mapAH.put(e.eventMsg, icMACD2);
				}

				if (mapAH.containsKey(e.eventMsg)) {
					InnerCount ic = (InnerCount) mapAH.get(e.eventMsg);
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				} else {
					InnerCount ic = new InnerCount();
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				}
			}
		}

		// print mapAH msg;
		Iterator entries = mapAH.entrySet().iterator();
		DecimalFormat df = new DecimalFormat("######0.00");
		TreeMap<Double, String> mapTopWinRate = new TreeMap<Double, String>(new Comparator<Double>() {

			/*
			 * int compare(Object o1, Object o2) 返回一个基本类型的整型， 返回负数表示：o1 小于o2， o1
			 * 排前面 返回0 表示：o1和o2相等， 返回正数表示：o1大于o2。 o2 排前面
			 */
			public int compare(Double o1, Double o2) {

				// 指定排序器按照降序排列
				int iret;
				if (o2 == o1) {
					iret = 0;
				} else {
					iret = (o2 - o1) > 0 ? 1 : -1;
				}
				return iret;
			}
		});

		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String key = (String) entry.getKey();

			InnerCount value = (InnerCount) entry.getValue();

			StringBuffer sb = new StringBuffer();
			double winRate = (double) value.wins / (value.wins + value.losts);
			double rate = (double) (value.wins + value.losts) / this.exDates.size();
			mapTopWinRate.put(winRate, key + "[" + df.format(rate * 100) + "%]");
			/*
			 * if ((winRate > 0.7) && (rate > 0.1)) {
			 * 
			 * sb.append("["); sb.append(key); sb.append("]");
			 * 
			 * sb.append("[wins = "); sb.append(value.wins); sb.append(
			 * "][losts = "); sb.append(value.losts); sb.append("][winrate = ");
			 * if (value.losts > 0) { sb.append(df.format(winRate * 100)); }
			 * else { sb.append(100); } sb.append("%]");
			 * 
			 * log.info(sb.toString()); }
			 */
		}
		entries = mapTopWinRate.entrySet().iterator();
		int i = 0;

		double ret = 0;

		// while (entries.hasNext() && i < 3) {
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			Double key = (Double) entry.getKey();
			String value = (String) entry.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(value);
			sb.append("=");
			sb.append(df.format(key * 100));
			sb.append("%]");
			log.info(sb.toString());

			if (!(eventName == null || eventName.isEmpty())) {
				if (value.contains(eventName)) {
					ret = key;
				}
			}

			/*
			 * StringBuffer sbSQL = new StringBuffer(); sbSQL.append(
			 * "insert into T_HisWinRate(StockID,StockName,StockBelong,EventMsg,WinRate) values ('"
			 * ); sbSQL.append(this.stockHistory.StockID); sbSQL.append("','");
			 * sbSQL.append(this.stockHistory.StockName); sbSQL.append("','");
			 * sbSQL.append(this.stockHistory.StockBelong); sbSQL.append("','");
			 * sbSQL.append(value); sbSQL.append("',");
			 * sbSQL.append(df.format(key * 100)); sbSQL.append(")");
			 * mySQL.executeSQL(sbSQL.toString());
			 */
			i++;
		}
		return ret;
	}

	public List<AssertEvent> getAssertEvents() {
		return assertEvents;
	}

	public void setAssertEvents(List<AssertEvent> assertEvents) {
		this.assertEvents = assertEvents;
	}

	public List<ExDate> getExDates() {
		return exDates;
	}

	public void setExDates(List<ExDate> exDates) {
		this.exDates = exDates;
	}

	/*
	 * d =
	 * AnalysisParser.pEventBTest("org.tokenring.analysis.AnalyzeSigmaQuantity",
	 * "Sigma分析 法量大","000046");
	 */
	public static double pEventBTest(String strAh, String strEvent, String stockId)
			throws SQLException, ClassNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String strBelong = stockId.startsWith("6") ? "SH" : "SZ";

		AnalysisParser ap = new AnalysisParser(stockId, strBelong);
		AnalyzeHistory ah;
		ah = new AnalyzeBTest(ap.getStockHistory());
		ap.addParser(ah);
		ah = new AnalyzeMyMACD(ap.getStockHistory());
		ap.addParser(ah);

		Class c = Class.forName(strAh);
		Class[] parameterTypes = { StockHistory.class };
		Constructor<?> constructor = c.getConstructor(parameterTypes);
		Object[] parameters = { ap.getStockHistory() };
		Object o = constructor.newInstance(parameters);
		ah = (AnalyzeHistory) o;

		ap.addParser(ah);

		ap.prepareAssertForcast();
		ap.doAnalyze();
		ap.doAssert();

		Iterator<ExDate> iterEx = ap.exDates.iterator();
		double count = 0;
		double hasEvent = 0;
		double hasMyMACDEvent = 0;
		double hasBoth = 0;
		while (iterEx.hasNext()) {
			ExDate exDate = (ExDate) iterEx.next();

			if (exDate.isWin) {
				count = count + 1;
				Iterator<Event> itrEvent = exDate.events.iterator();
				while (itrEvent.hasNext()) {
					Event e = (Event) itrEvent.next();

					if (e.eventMsg.contains(strEvent)) {
						hasEvent = hasEvent + 1;
					}

					if (e.eventMsg.contains("MyMACD 向上突破")) {
						hasMyMACDEvent = hasMyMACDEvent + 1;
					}

					if (e.eventMsg.contains(strEvent) && e.eventMsg.contains("MyMACD 向上突破")) {
						hasBoth++;
					}

				}
			}
		}
		System.out.println("exDate.size = " + ap.exDates.size());
		System.out.println("hasEvent = " + hasEvent);
		System.out.println("hasMyMACDEvent = " + hasMyMACDEvent);
		System.out.println("hasBoth = " + hasBoth);
		System.out.println("count = " + count);
		ap.printAnalyze("");
		return hasEvent / count;

	}
	public String calcType(StockExchangeData sed){
		/*
		 * Slot1 : 光头光脚（红），上下影低于柱体1/10
		 * Slot2 ：光头光脚（绿），上下影低于柱体1/10
		 * Slot3 ：上影（红），上影长于柱体100%
		 * SLot4 ：上影（绿），上影长于柱体100%
		 * SLot5 ：下影（红），下影长于柱体100%
		 * SLot6 ：下影（绿），下影长于柱体100%
		 * SLot7 ：上下影（红），上下影长于柱体100%
		 * SLot8 ：上下影（绿），上下影长于柱体100%
		 * SLot9 ：其他
		 */
		StringBuffer sb = new StringBuffer();
		int type = 9;
		double body = Math.abs((sed.getBeginPrice() - sed.getEndPrice()));
		double upper = sed.getHighestPrice() - (Math.max(sed.getBeginPrice(), sed.getEndPrice()));
		double lower = Math.min(sed.getBeginPrice(), sed.getEndPrice()) - sed.getLowestPrice();
		boolean isRed = (sed.getEndPrice() - sed.getBeginPrice()) > 0 ;
		
		if ((upper >= body) && (lower >=body)){
			if(isRed){
				type = 7;
			}
			else{
				type = 8;
			}
		}else {
			if (upper >= body){
				if(isRed){
					type = 3;
				}
				else{
					type = 4;
				}
				
			}
			if (lower >=body){
				if(isRed){
					type = 5;
				}
				else{
					type = 6;
				}
				
			}
			if ((10 * upper <= body) && (10 * lower <=body)){
				if(isRed){
					type = 1;
				}
				else{
					type = 2;
				}
				
			}
		
		}
		
		for (int i = 1;i <= 9;i++){
			if (i == type){
				sb.append("1,");
			}else{
				sb.append("0,");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
		
	}
	public String calcEvents(ExDate exDate){
		/*
		 * AnalyzeMACD  "MACD 向上突破","MACD 向下突破",
AnalyzeMyMACD  "MyMACD 向上突破 15%","MyMACD 向上突破","MyMACD 向下突破",
AnalyzeSigmaPrice  "Sigma分析 法价大涨","Sigma分析 法价大跌",
AnalyzeSigmaQuantity  "Sigma分析 法量大涨","Sigma分析 法量大跌",
AnalyzeSigmaSituation  "Sigma分析 拐点向下","Sigma分析 拐点向上",
AnalyzeKDJ  "KDJ金叉","KDJ死叉",
AnalyzePricePastDaysRiseRate(5, 20, stockHistory);    "5天涨幅20%"
AnalyzePricePastDaysRiseRate(5, -20, stockHistory);   "5天跌幅20%"
AnalyzeAmountPastDaysRiseRate(3, 10, stockHistory);   "3天放量10%"
AnalyzeAmountPastDaysRiseRate(3, -10, stockHistory);  "3天缩量10%"
AnalyzePriceAndAmountPastDaysContinueRiseRate(5, 1,   "连续 5天量价齐涨"
				stockHistory);
AnalyzePriceAndAmountPastDaysContinueRiseRate(5, -1,  "连续 5天量价齐跌"
				stockHistory);
AnalyzeAveragePrice(60, 5, false, stockHistory);     "连续5低于60日均线"
		analysisChain.add(fiveDaysAbove60AveragePrice);
AnalyzeAveragePrice(60, 5, true, stockHistory);      "连续5高于60日均线"
		analysisChain.add(fiveDaysBelow60AveragePrice);
AnalyzeAverageAmount(60, 5, false, stockHistory);
		analysisChain.add(fiveDaysAbove60AverageAmount);  "连续5低于60日均成交量"
AnalyzeAverageAmount(60, 5, true, stockHistory);      "连续5高于60日均成交量"
		analysisChain.add(fiveDaysBelow60AverageAmount);														
		 *///23
		String[] eventNames = {"MACD 向上突破",
				               "MACD 向下突破",
				               "MyMACD 向上突破 15%",
				               "MyMACD 向上突破",
				               "MyMACD 向下突破",
				               "Sigma分析 法价大涨",
				               "Sigma分析 法价大跌",
				               "Sigma分析 法量大涨",
				               "Sigma分析 法量大跌",
				               "Sigma分析 拐点向下",
				               "Sigma分析 拐点向上",
				               "KDJ金叉",
				               "KDJ死叉",
				               "5天涨幅20%",
				               "5天跌幅20%",
				               "3天放量10%",
				               "3天缩量10%",
				               "连续 5天量价齐涨",
				               "连续 5天量价齐跌",
				               "连续5低于60日均线",
				               "连续5高于60日均线",
				               "连续5低于60日均成交量",
				               "连续5高于60日均成交量"
				               };
		StringBuffer sb = new StringBuffer();
		int iLen = eventNames.length;
		for(int i = 0; i < iLen;i++){
			sb.append(exDate.hasEvent(eventNames[i])?"1,":"0,");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
	/**
	 * 
	 * @param fw
	 * @param type
	 *            0-ALL 1-WIN -1-LOSS
	 */
	public void writeTF(DataFile fw, int type) {
		// write(6) High,Low,Open,End,Amount,Quantity
		// write events(18) MACD ascend,MACD decline,MyMACD ascend,MyMACD
		// decline,
		// SigmaPrice rise , SigmaPrice fall
		// SigmaQuantity rise, SigmaQuantity fall
		// KDJ ascend,KDJ decline
		// SigmaSituation rise ,SigmaSituation fall
		// continue price ascend ,continue price decline
		// continue amount ascend, continue amount decline
		// 20percents rise, 20percents fall
		// write forecast win,lost

		StringBuffer sb, sb_pre;
		StockHistory sh = this.stockHistory;
		StockExchangeData sed;

		// String logname = sh.StockID + ".data";
		// Logger log = Logger.getLogger(AnalysisParser.class);

		Iterator<ExDate> itr_e = exDates.iterator();
		ExDate e;
		Iterator<Event> itr_ev;
		Event event;

		int i = exDates.size() - 15 - 20 - 40;
		int j = 20;
		while (itr_e.hasNext() && (j-- >= 0)) {
			e = itr_e.next();
		}

		while (itr_e.hasNext() && (i-- > 0)) {
			e = itr_e.next();

			if ((type == 1) && (!e.isWin)) {
				continue;
			}

			if ((type == -1) && (e.isWin)) {
				continue;
			}
			// if (e.isWin){
			// continue;
			// }
			sed = sh.getHisDataByExDate(e.idx);
			sb = new StringBuffer();
			sb_pre = new StringBuffer();
			StockExchangeData sed2;
			double dQuantitySigmaCount;
			double dPriceSigmaCount;
			for (int idx = e.idx + 39; idx >= e.idx; idx--) {
				sed2 = sh.getHisDataByExDate(idx);
				// 成交量和均值有N个sigma偏差，正表示高于，负表示低于
				dQuantitySigmaCount = (sed2.getExQuantity() - sed.getAverage()) / sed.getSigma();
				// 收盘价和均值有N个sigma偏差，正表示高于，负表示低于
				dPriceSigmaCount = (sed2.getEndPrice() - sed.getPriceAverage()) / sed.getPriceSigma();

				sb_pre.append(dPriceSigmaCount);
				sb_pre.append(",");
				sb.append(dQuantitySigmaCount);
				sb.append(",");
			}

			sb.append(e.isWin() ? 1 : 0);
			sb.append(",");
			sb.append(e.isWin() ? 0 : 1);

			fw.writeMsg(sb_pre.toString() + sb.toString());
		}
		fw.flush();

	}

	public void writePreditive(DataFile fw) {
		StringBuffer sb, sb_pre;
		StockHistory sh = this.stockHistory;
		StockExchangeData sed;
		Iterator<ExDate> itr_e = exDates.iterator();
		ExDate e;
		Iterator<Event> itr_ev;
		Event event;

		int i = exDates.size() - 15 - 20 - 40;
		int j = 20;
		while (itr_e.hasNext() && (j-- >= 0)) {
			e = itr_e.next();
		}

		int k = 20;
		while (itr_e.hasNext() && (i-- > 0) && (k-- > 0)) {
			e = itr_e.next();
			sed = sh.getHisDataByExDate(e.idx);
			sb = new StringBuffer();
			sb_pre = new StringBuffer();
			StockExchangeData sed2;
			double dQuantitySigmaCount;
			double dPriceSigmaCount;
			for (int idx = e.idx + 39; idx >= e.idx; idx--) {
				sed2 = sh.getHisDataByExDate(idx);
				// 成交量和均值有N个sigma偏差，正表示高于，负表示低于
				dQuantitySigmaCount = (sed2.getExQuantity() - sed.getAverage()) / sed.getSigma();
				// 收盘价和均值有N个sigma偏差，正表示高于，负表示低于
				dPriceSigmaCount = (sed2.getEndPrice() - sed.getPriceAverage()) / sed.getPriceSigma();

				sb_pre.append(dPriceSigmaCount);
				sb_pre.append(",");
				sb.append(dQuantitySigmaCount);
				sb.append(",");
			}

			sb.append(e.isWin() ? 1 : 0);
			sb.append(",");
			sb.append(e.isWin() ? 0 : 1);
			fw.writeMsg(sb_pre.toString() + sb.toString());
		}
	}

	public void writeClassification(DataFile fw) {

		StringBuffer sb, sb_pre;
		StockHistory sh = this.stockHistory;
		StockExchangeData sed;

		// String logname = sh.StockID + ".data";
		// Logger log = Logger.getLogger(AnalysisParser.class);

		Iterator<ExDate> itr_e = exDates.iterator();
		ExDate e;
		Iterator<Event> itr_ev;
		Event event;

		//int i = exDates.size() - 15 - 40 - 1;
		// int j = 20;
		// while (itr_e.hasNext() && (j-- >= 0)) {
		// e = itr_e.next();
		// }
		int i = exDates.size() - 20 - 40 -1;
		if (i > 200) {
			i = 200;
		}
		
		int k = 20;
	    while (itr_e.hasNext() && (k-- >= 0)) {
		  e = itr_e.next();
		}

		//ExDate tomorrow = itr_e.next();
		ExDate today;
		while (itr_e.hasNext() && (i-- > 0)) {
			today = itr_e.next();
			e = today;

			sed = sh.getHisDataByExDate(e.idx);
			sb = new StringBuffer();
			sb_pre = new StringBuffer();
			StockExchangeData sed2;
			Iterator<ExDate> itr_exdate =  exDates.iterator();
			ExDate theday=null;
			while((itr_exdate.hasNext()) ){
				theday = itr_exdate.next();
				if (theday == today){
					break;
				}
			}
			
			double dQuantitySigmaCount;
			double dPriceSigmaCount;
			//
			
			StockExchangeData sedTemp = sh.getHisDataByExDate(theday.getIdx());
			for(int j = 0;j < 40;j++){
				
				
				sb_pre.insert(0,",");
				sb_pre.insert(0, calcEvents(theday));
				sb_pre.insert(0, ",");
				sb_pre.insert(0, calcType(sedTemp));
				
				theday = itr_exdate.next();
				sedTemp = sh.getHisDataByExDate(theday.getIdx());
			}
			//sb_pre.deleteCharAt(sb_pre.length() - 1);
				/*
			for (int idx = e.idx + 39; idx >= e.idx; idx--) {
				//sed2 = sh.getHisDataByExDate(idx);
			
				// 成交量和均值有N个sigma偏差，正表示高于，负表示低于
				//dQuantitySigmaCount = (sed2.getExQuantity() - sed.getAverage()) / sed.getSigma();
				// dQuantitySigmaCount = 1.0 * sed2.getExQuantity() /
				// sed.getExQuantity();
				// 收盘价和均值有N个sigma偏差，正表示高于，负表示低于
				dPriceSigmaCount = (sed2.getEndPrice() - sed.getPriceAverage()) / sed.getPriceSigma();
				// dPriceSigmaCount = 1.0 * sed2.getEndPrice() /
				// sed.getEndPrice();

				//sb_pre.append(intToOneSlot((int) Math.round(dPriceSigmaCount * 7), 41));
				sb_pre.append(Math.round(dPriceSigmaCount * 7));
				sb_pre.append(",");
				//sb.append(intToOneSlot((int) Math.round(dQuantitySigmaCount * 7), 41));
				sb.append(Math.round(dQuantitySigmaCount * 7));
				sb.append(",");
			}
				*/
			/*
			
			int flag = (int) Math.round(100.0 * (sh.getHisDataByExDate(tomorrow.getIdx()).getEndPrice()
					/ sh.getHisDataByExDate(today.getIdx()).getEndPrice()) - 100);
			if (flag > 10)
				flag = 10;
			if (flag < -10)
				flag = -10;
			for (int k = -10; k <= 10; k++) {
				sb.append((k == flag) ? 1 : 0);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			*/
			//结果按照20日15%预测
			
			if (today.isWin()){
				sb.append("1,0");
			}else{
				sb.append("0,1");
			}
			/*
			sedTemp = sh.getHisDataByExDate(today.getIdx());
			
			sb.append(calcType(sedTemp));
			*/
			
			//dfs[flag + 10].writeMsg(sb_pre.toString() + sb.toString());
			//dfs[flag + 10].flush();
			
			fw.writeMsg(sb_pre.toString() + "9876," +sb.toString());
			fw.flush();
			
			//tomorrow = today;

		}

		// merge(fw);
	}

	public String intToOneSlot(int l, int slots) {
		if (slots % 2 == 0) {
			slots += 1;
		}
		int zero = (slots + 1) / 2;
		int max = (slots - 1) / 2;
		int min = -max;
		StringBuffer sb = new StringBuffer();

		if (l >= 0) {
			if (l > max)
				l = max;
			for (int i = min; i < 0; i++) {
				sb.append("0,");
			}

			for (int i = 0; (i <= l); i++) {
				sb.append("1,");
			}

			for (int i = (l + 1); (i <= max); i++) {
				sb.append("0,");
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			if (l < min)
				l = min;
			for (int i = min; i < l; i++) {
				sb.append("0,");
			}

			for (int i = l; (i <= 0); i++) {
				sb.append("1,");
			}

			for (int i = 1; (i <= max); i++) {
				sb.append("0,");
			}
			sb.deleteCharAt(sb.length() - 1);

		}

		return sb.toString();

	}

	public void writeClassificationPredictive(DataFile fw) {
		StringBuffer sb, sb_pre;
		StockHistory sh = this.stockHistory;
		StockExchangeData sed;

		// String logname = sh.StockID + ".data";
		// Logger log = Logger.getLogger(AnalysisParser.class);

		Iterator<ExDate> itr_e = exDates.iterator();
		ExDate e;
		Iterator<Event> itr_ev;
		Event event;

		// int i = exDates.size() - 15 - 40 - 1;
		int i = 4;
		// int j = 20;
		// while (itr_e.hasNext() && (j-- >= 0)) {
		// e = itr_e.next();
		// }

		ExDate tomorrow = itr_e.next();
		ExDate today;
		while (itr_e.hasNext() && (i-- > 0)) {
			today = itr_e.next();
			e = today;

			sed = sh.getHisDataByExDate(e.idx);
			sb = new StringBuffer();
			sb_pre = new StringBuffer();
			StockExchangeData sed2;
			double dQuantitySigmaCount;
			double dPriceSigmaCount;
			for (int idx = e.idx + 39; idx >= e.idx; idx--) {
				sed2 = sh.getHisDataByExDate(idx);
				// 成交量和均值有N个sigma偏差，正表示高于，负表示低于
				dQuantitySigmaCount = (sed2.getExQuantity() - sed.getAverage()) / sed.getSigma();
				// dQuantitySigmaCount = 1.0 * sed2.getExQuantity() /
				// sed.getExQuantity();
				// 收盘价和均值有N个sigma偏差，正表示高于，负表示低于
				dPriceSigmaCount = (sed2.getEndPrice() - sed.getPriceAverage()) / sed.getPriceSigma();
				// dPriceSigmaCount = 1.0 * sed2.getEndPrice() /
				// sed.getEndPrice();

				sb_pre.append(intToOneSlot((int) Math.round(dPriceSigmaCount * 7), 41));
				sb_pre.append(",");
				sb.append(intToOneSlot((int) Math.round(dQuantitySigmaCount * 7), 41));
				sb.append(",");
			}

			int flag = (int) Math.round(100.0 * (sh.getHisDataByExDate(tomorrow.getIdx()).getEndPrice()
					/ sh.getHisDataByExDate(today.getIdx()).getEndPrice()) - 100);
			for (int k = -10; k <= 10; k++) {
				sb.append((k == flag) ? 1 : 0);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);

			fw.writeMsg(sb_pre.toString() + sb.toString());
			fw.flush();
			tomorrow = today;

		}

	}

	public static void main(String[] args) throws InterruptedException, SQLException, FileNotFoundException {

		// AnalysisParser ap = new AnalysisParser("300017","SZ");
		// System.out.println(ap.intToOneSlot(-5, 5));
		// System.out.println(ap.intToOneSlot(-2, 5));
		// System.out.println(ap.intToOneSlot(-1, 5));
		// System.out.println(ap.intToOneSlot(0, 5));
		// System.out.println(ap.intToOneSlot(1, 5));
		// System.out.println(ap.intToOneSlot(2, 5));
		// System.out.println(ap.intToOneSlot(5, 5));
		//
		// System.out.println("------------------------");
		// System.out.println(ap.intToOneSlot(-5, 4));
		// System.out.println(ap.intToOneSlot(-2, 4));
		// System.out.println(ap.intToOneSlot(-1, 4));
		// System.out.println(ap.intToOneSlot(0, 4));
		// System.out.println(ap.intToOneSlot(1, 4));
		// System.out.println(ap.intToOneSlot(2, 4));
		// System.out.println(ap.intToOneSlot(5, 4));

		String strSQL = "select StockID,StockBelong,StockName from T_StockBaseInfo where StockName != '上证指数' and StockName != '深圳成指' limit 500 ";
		//and StockID in (601288,601166,601398,601939,601818,601988,600016,600015,000001,601328,600000,600036,601998,601169,002142,601009,601229,600919,601997,600926,600908,603323,601128,002807,002839) 
		// and (StockID in (600089))
		MyBatis mb = MyBatis.getInstance();
		List<Map> lm = mb.queryBySQL(strSQL);
		Iterator itr = lm.iterator();
		Map m;

//		DataFile[] dfs = new DataFile[21];
//		for (int i = 0; i < 21; i++) {
//
//			try {
//				dfs[i] = new DataFile("First500-" + Integer.toString(i) + ".txt");
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
		DataFile df_First500 = new DataFile("First500-30-10-3.txt");
		String strLoss = "300017";
		DataFile df_SZ300017_LOSE = new DataFile("SZ300017-LOSE.txt");
		String strWin = "300017";
		DataFile df_SZ300017_WIN = new DataFile("SZ300017-WIN.txt");
		String strValidate = "600089";
		DataFile df_SZ300017 = new DataFile("SZ300017.txt");
		String strPreditive = "300017";
		DataFile df_preditive = new DataFile("predict.txt");
		while (itr.hasNext()) {
			m = (Map) itr.next();
			AnalysisParser ap = new AnalysisParser((String) m.get("StockID"), (String) m.get("StockBelong"));
			AnalyzeHistory ah;
			ah = new AnalyzeMACD(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeBTest(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeMyMACD(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeSigmaPrice(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeSigmaQuantity(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeSigmaSituation(ap.getStockHistory());
			ap.addParser(ah);

			ah = new AnalyzeKDJ(ap.getStockHistory());
			ap.addParser(ah);

			ap.prepareParser();
			ap.prepareAssertForcast();
			ap.doAnalyze();
			ap.doAssert();
			// ap.writeTF(df_First500,0);
			ap.writeClassification(df_First500);
			// ap.writeClassificationPredictive(df_preditive);

			// if (m.get("StockID").equals(strLoss)){
			// ap.writeTF(df_SZ300017_LOSE,-1);
			// }
			//
			// if (m.get("StockID").equals(strWin)){
			// ap.writeTF(df_SZ300017_WIN,1);
			// }
			//
			// if (m.get("StockID").equals(strValidate)){
			// ap.writeTF(df_SZ300017,0);
			// }
			//
			
			if (m.get("StockID").equals(strPreditive)) {
				// ap.writePreditive(df_preditive);
				ap.writeClassificationPredictive(df_preditive);
			}

		}
		df_First500.close();
		df_SZ300017_LOSE.close();
		df_SZ300017_WIN.close();
		df_preditive.close();

//		for (int k = 0; k < 21; k++) {
//
//			dfs[k].close();
//
//		}

		/*
		 * try { Logger log = Logger.getLogger(AnalysisParser.class);
		 * 
		 * 
		 * 
		 * String strSQL =
		 * "select StockID,StockBelong,StockName from T_StockBaseInfo where StockName != '上证指数' and StockName != '深圳成指' "
		 * ; //ResultSet rs = mySQL.QueryBySQL(strSQL);
		 * 
		 * MyBatis mb = MyBatis.getInstance(); List<Map> lm =
		 * mb.queryBySQL(strSQL); Iterator itr = lm.iterator(); Map m;
		 * 
		 * StringBuffer theEventMsg = new StringBuffer(); int totalCount = 0;
		 * int beyond75 = 0; int beyond60 = 0; int beyond50 =0; int beyond30 =
		 * 0; int below30 = 0; Semaphore semp = new Semaphore(60);
		 * 
		 * while (itr.hasNext()) { m = (Map) itr.next(); totalCount ++; // 申请许可
		 * semp.acquire(); AnalysisParser ap = new
		 * AnalysisParser((String)m.get("StockID"),
		 * (String)m.get("StockBelong")); System.out.println("[StockID = " +
		 * (String)m.get("StockID") + "][StockName = " +
		 * (String)m.get("StockName") + "]"); log.fatal("[StockID = " +
		 * (String)m.get("StockID") + "][StockName = " +
		 * (String)m.get("StockName") + "]"); // AnalysisParser ap = new
		 * AnalysisParser("600570", "SH");
		 * 
		 * APThread thread = new APThread(); thread.setSemp(semp);
		 * thread.setAp(ap); thread.start();
		 * 
		 * //System.out.println(rate); if (ap.getRate() >= 0.75){ beyond75 ++;
		 * }else if (ap.getRate() >= 0.6){ beyond60 ++; }else if(ap.getRate() >=
		 * 0.5){ beyond50 ++; }else if (ap.getRate() >=0.3){ beyond30 ++; }else{
		 * below30 ++; }
		 * 
		 * //ap.printAll();
		 * 
		 * //DecisionTree dt = new DecisionTree(ap.exDates); //dt.printTree();
		 * 
		 * }
		 * 
		 * 
		 * System.out.println("Total count:" + totalCount);
		 * System.out.println("Beyond75:" + beyond75);
		 * System.out.println("Beyond60:" + beyond60);
		 * System.out.println("Beyond50:" + beyond50);
		 * System.out.println("Beyond30:" + beyond30);
		 * System.out.println("Below30:" + below30); System.out.println(new
		 * Date()); //DecisionTree dt = new DecisionTree(ap.exDates);
		 * //dt.printTree();
		 */
		/*
		 * List<Event> events = ap.getEvents(); List<Event> rightEvents = new
		 * ArrayList<Event> (); List<Event> leftEvents = new ArrayList<Event>
		 * (); Iterator<Event> iterEvent = events.iterator(); Event e;
		 * 
		 * while (iterEvent.hasNext()) { // 待验证的历史事件 e = iterEvent.next();
		 * 
		 * }
		 */
		/*
		 * MySqlTrail mySQL = new MySqlTrail(); boolean b = mySQL.init(); String
		 * sql;
		 * 
		 * Iterator<AssertEvent> itr = ap.getAssertEvents().iterator(); sql =
		 * "truncate table t_stock_assert_event"; mySQL.executeSQL(sql);
		 * 
		 * while (itr.hasNext()) { AssertEvent e = itr.next();
		 * 
		 * StringBuffer sbSQL = new StringBuffer(); sbSQL.append(
		 * "insert into t_stock_assert_event(StockID,StockName,StockBelong,ExDate,EventMsg,AssertMsg) values ( '"
		 * ); sbSQL.append(e.getEvent().getStockID()); sbSQL.append("','");
		 * sbSQL.append(e.getEvent().getStockName()); sbSQL.append("','");
		 * sbSQL.append(e.getEvent().getStockBelong()); sbSQL.append("','");
		 * sbSQL.append(e.getEvent().getExDate()); sbSQL.append("','");
		 * sbSQL.append(e.getEvent().getEventMsg()); sbSQL.append("','");
		 * sbSQL.append(e.getAssertMsg()); sbSQL.append("')");
		 * 
		 * mySQL.executeSQL(sbSQL.toString()); }
		 */
		/*
		 * Iterator<Event> itrEvent = ap.getEvents().iterator(); sql =
		 * "truncate table t_stock_event"; mySQL.executeSQL(sql);
		 * 
		 * while (itrEvent.hasNext()) { Event e = itrEvent.next();
		 * 
		 * StringBuffer sbSQL = new StringBuffer(); sbSQL.append(
		 * "insert into t_stock_event(StockID,StockName,StockBelong,ExDate,EventMsg) values ( '"
		 * ); sbSQL.append(e.getStockID()); sbSQL.append("','");
		 * sbSQL.append(e.getStockName()); sbSQL.append("','");
		 * sbSQL.append(e.getStockBelong()); sbSQL.append("','");
		 * sbSQL.append(e.getExDate()); sbSQL.append("','");
		 * sbSQL.append(e.getEventMsg()); sbSQL.append("')");
		 * 
		 * mySQL.executeSQL(sbSQL.toString()); }
		 * 
		 * mySQL.destroy();
		 */

		/*
		 * } catch (SQLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

	}

	public void saveEventToDB() {
		// String strSQL = "delete from t_stock_event_sina where StockID = '" +
		// this.stockHistory.getStockID() + "'";
		MyBatis mb = MyBatis.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("stockId", this.stockHistory.getStockID());
		mb.delByLabel("del_stock_event_sina", params);

		// mb.executeSQL(strSQL);

		Iterator<ExDate> itrExDates = this.getExDates().iterator();
		ExDate exDate;
		while (itrExDates.hasNext()) {
			exDate = (ExDate) itrExDates.next();

			Iterator<Event> itrEvents = exDate.getEvents().iterator();
			Event e;
			params.clear();

			while (itrEvents.hasNext()) {
				e = (Event) itrEvents.next();

				params.put("stockId", e.stockID);
				params.put("stockBelong", e.stockBelong);
				params.put("exDate", e.exDate);
				params.put("isWin", exDate.isWin ? "Y" : "N");
				params.put("eventName", e.eventMsg);

				mb.insertByLabel("in_stock_event_sina", params);
			}

		}

		// generate data to History Win Rate
		params.clear();
		params.put("stockId", this.stockHistory.getStockID());
		params.put("stockBelong", this.stockHistory.getStockBelong());

		mb.delByLabel("del_his_win_rate", params);

		params.clear();
		params.put("stockId", this.stockHistory.getStockID());
		mb.insertByLabel("in_his_win_rate_by_stock_event_sina", params);

		params.clear();
		params.put("stockId", this.stockHistory.getStockID());
		mb.delByLabel("del_stock_event_sina", params);

	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}
}
