package org.tokenring.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.tokenring.db.MySqlTrail;

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
		//assertChain.add(next3DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next5DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(5, 10, 1);
		//assertChain.add(next5DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next10DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(10, 10, 1);
		//assertChain.add(next10DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next20DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(20, 10, 2);
		assertChain.add(next20DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next30DaysRise20 = new AssertForcastNextNDaysMaxMRiseR(30, 15, 2);
		assertChain.add(next30DaysRise20);

		AssertForcastNextNDaysMaxMRiseR next60DaysRise20 = new AssertForcastNextNDaysMaxMRiseR(60, 20, 3);
		assertChain.add(next60DaysRise20);

		AssertForcastNextNDaysMaxMRiseR next90DaysRise30 = new AssertForcastNextNDaysMaxMRiseR(90, 30, 3);
		assertChain.add(next90DaysRise30);
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
				//log.info(sb.toString());

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
		//add
		InnerCount icMACD2 = new InnerCount();
		icMACD2.losts = 0;
		icMACD2.wins = 0;
		mapAH.put("MyMACD+MACD", icMACD2);
		//add end

		Iterator<ExDate> iterEx = exDates.iterator();
		ExDate ex;
		Event e;
		boolean bFindMACD = false;//add
		boolean bFindMyMACD = false;//add
		
		while (iterEx.hasNext()) {
			ex = iterEx.next();
			Iterator<Event> iterEvent = ex.events.iterator();
			
			bFindMACD = false;//add
			bFindMyMACD = false;//add
			
			while (iterEvent.hasNext()) {
				e = iterEvent.next();
				
				if ("MyMACD 向上突破".equals(e.eventMsg)){
					bFindMyMACD = true;
				}
				
				if ("MACD 向上突破".equals(e.eventMsg)){
					bFindMACD = true;
				}
				
				if (bFindMACD && bFindMyMACD){
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
			mapTopWinRate.put(winRate, key+"["+df.format(rate * 100)+"%]");
/*
			if ((winRate > 0.7) && (rate > 0.1)) {

				sb.append("[");
				sb.append(key);
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
*/
		}
		entries = mapTopWinRate.entrySet().iterator();
		int i = 0;
		

		double ret = 0;
		
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		//while (entries.hasNext() && i < 3) {
		while (entries.hasNext() ) {
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
			
			if (!(eventName == null || eventName.isEmpty())){
				if (value.contains(eventName)){
					ret = key;
				}
			}
			
			/*
			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append("insert into T_HisWinRate(StockID,StockName,StockBelong,EventMsg,WinRate) values ('");
			sbSQL.append(this.stockHistory.StockID);
			sbSQL.append("','");
			sbSQL.append(this.stockHistory.StockName);
			sbSQL.append("','");
			sbSQL.append(this.stockHistory.StockBelong);
			sbSQL.append("','");
			sbSQL.append(value);
			sbSQL.append("',");
			sbSQL.append(df.format(key * 100));
			sbSQL.append(")");
			mySQL.executeSQL(sbSQL.toString());
			*/
			i++;
		}
		mySQL.destroy();
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Logger log = Logger.getLogger(AnalysisParser.class);

			MySqlTrail mySQL = new MySqlTrail();
			boolean b = mySQL.init();
			String strSQL = "select StockID,StockBelong,StockName from T_StockBaseInfo where StockName != '上证指数'";
			ResultSet rs = mySQL.QueryBySQL(strSQL);
			
			StringBuffer theEventMsg = new StringBuffer();
			int totalCount = 0;
			int beyond75 = 0;
			int beyond60 = 0;
			int beyond50 =0;
			int beyond30 = 0;
			int below30 = 0;

			while (rs.next()) {
				totalCount ++;
				AnalysisParser ap = new AnalysisParser(rs.getString("StockID"), rs.getString("StockBelong"));
				System.out.println("[StockID = " + rs.getString("StockID") + "][StockName = " + rs.getString("StockName") + "]");
				log.fatal("[StockID = " + rs.getString("StockID") + "][StockName = " + rs.getString("StockName") + "]");
				// AnalysisParser ap = new AnalysisParser("600570", "SH");

				// AnalyzeHistory ah = new AnalyzeAverageAmount(60, 5, true);
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
				/*
				for (int i = 10; i <= 60; i++) {
					for (int j = 3; j < 15; j++) {
						ah = new AnalyzeAverageAmount(i, j, true, ap.getStockHistory());
						ap.addParser(ah);
						ah = new AnalyzeAverageAmount(i, j, false, ap.getStockHistory());
						ap.addParser(ah);

						ah = new AnalyzeAveragePrice(i, j, true, ap.getStockHistory());
						ap.addParser(ah);
						ah = new AnalyzeAveragePrice(i, j, false, ap.getStockHistory());
						ap.addParser(ah);

					}
				}
				*/
				ap.prepareParser();
				ap.prepareAssertForcast();
				ap.doAnalyze();
				ap.doAssert();
				double rate = ap.printAnalyze("Sigma分析 拐点向上");
				//System.out.println(rate);
				if (rate >= 0.75){
					beyond75 ++;
				}else if (rate >= 0.6){
					beyond60 ++;
				}else if(rate >= 0.5){
					beyond50 ++;
				}else if (rate >=0.3){
					beyond30 ++;
				}else{
					below30 ++;
				}
				
				//ap.printAll();
				
				//DecisionTree dt = new DecisionTree(ap.exDates);
				//dt.printTree();

			}
			rs.close();
			mySQL.destroy();
			
			System.out.println("Total count:" + totalCount);
			System.out.println("Beyond75:" + beyond75);
			System.out.println("Beyond60:" + beyond60);
			System.out.println("Beyond50:" + beyond50);
			System.out.println("Beyond30:" + beyond30);
			System.out.println("Below30:" + below30);
			//DecisionTree dt = new DecisionTree(ap.exDates);
			 //dt.printTree();

			/*
			 * List<Event> events = ap.getEvents(); List<Event> rightEvents =
			 * new ArrayList<Event> (); List<Event> leftEvents = new
			 * ArrayList<Event> (); Iterator<Event> iterEvent =
			 * events.iterator(); Event e;
			 * 
			 * while (iterEvent.hasNext()) { // 待验证的历史事件 e = iterEvent.next();
			 * 
			 * }
			 */
			/*
			 * MySqlTrail mySQL = new MySqlTrail(); boolean b = mySQL.init();
			 * String sql;
			 * 
			 * Iterator<AssertEvent> itr = ap.getAssertEvents().iterator(); sql
			 * = "truncate table t_stock_assert_event"; mySQL.executeSQL(sql);
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

}
