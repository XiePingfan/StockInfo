package org.tokenring.db;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.milkyway.vo.StockPastAdj;
import org.tokenring.analysis.AnalysisParser;
import org.tokenring.analysis.AnalyzeAverageAmount;
import org.tokenring.analysis.AnalyzeAveragePrice;
import org.tokenring.analysis.AnalyzeHistory;
import org.tokenring.analysis.AnalyzeKDJ;
import org.tokenring.analysis.AnalyzeMACD;
import org.tokenring.analysis.AnalyzeMyMACD;
import org.tokenring.analysis.AnalyzeSigmaPrice;
import org.tokenring.analysis.AnalyzeSigmaQuantity;
import org.tokenring.analysis.AnalyzeSigmaSituation;

import org.tokenring.analysis.Event;
import org.tokenring.analysis.StockHistory;

public class AnalzyPastAdj {
	public void execute() throws SQLException {
		MyBatis mb = MyBatis.getInstance();
		String sql = "truncate table t_stock_past_adj";
		mb.executeSQL(sql);

		String strDay;

		sql = "SELECT exdate newExDate FROM t_stockadjhis_sina t WHERE t.StockID = '000001'  ORDER BY exdate DESC  LIMIT 6";
		List<Map> lm = mb.queryBySQL(sql);
		Iterator<Map> itr = lm.iterator();
		Map m ;
		
		
		if (itr.hasNext()) {
			m = (Map)itr.next();

			// 处理当天数据
			strDay = (String) m.get("newExDate");
			sql = "insert into t_stock_past_adj(StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount) select StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount from t_stockadjhis_sina where ExDate ='"
					+ strDay + "'";
			mb.executeSQL(sql);

			sql = "update t_stock_past_adj t1,T_StockBaseInfo t2 set t1.StockName = t2.StockName where t1.StockID=t2.StockID and t1.StockBelong = t2.StockBelong";
			mb.executeSQL(sql);
		}

		int i = 1;
		while (itr.hasNext()) {
			m = (Map) itr.next();
			// 处理后续5天数据
			strDay = (String) m.get("newExDate");
			StringBuffer sb = new StringBuffer();
			sb.append("update t_stock_past_adj t1, t_stockadjhis_sina t2 ");
			sb.append("set t1.EndPrice");
			sb.append(i);
			sb.append(" = t2.EndPrice, ");
			sb.append("t1.ExAmount");
			sb.append(i);
			sb.append(" = t2.ExAmount ");
			sb.append("where t1.StockID = t2.StockID ");
			sb.append("and t1.StockBelong = t2.StockBelong ");
			sb.append("and t2.exdate = '");
			sb.append(strDay);
			sb.append("'");
			i++;

			mb.executeSQL(sb.toString());
		}

	}

	public void analzy() throws SQLException {
		MyBatis mb = MyBatis.getInstance();

		//String strSQL = "select StockID,StockBelong,ExDate,EndPrice,ExAmount,EndPrice1,ExAmount1,EndPrice2,ExAmount2,EndPrice3,ExAmount3,EndPrice4,ExAmount4,EndPrice5,ExAmount5 from t_stock_past_adj";
		Map params = new HashMap();
		List<Map> lm = mb.queryByLabel("getStockPastAdj",params);
		Iterator itr = lm.iterator();
		//Map m;
		StockPastAdj spa;

		String strStockID, strStockBelong, strExDate;
		Double dEndPrice, dExAmount, dEndPrice1, dExAmount1, dEndPrice2, dExAmount2, dEndPrice3, dExAmount3, dEndPrice4,
				dExAmount4, dEndPrice5, dExAmount5;
		StringBuffer sb;
		DecimalFormat df = new DecimalFormat("######0.00");
		double dTemp;
		while (itr.hasNext()) {
			spa = (StockPastAdj) itr.next();
			
			strStockID = spa.getStockID();
			strStockBelong = spa.getStockBelong();
			if (("000001".equals(strStockID) && "SH".equals("strStockBelong"))
					|| ("399001".equals(strStockID) && "SZ".equals("strStockBelong"))) {
				// 上证指数，深圳成指 不分析
				continue;
			}
			sb = new StringBuffer();
							
			dEndPrice = spa.getEndPrice();
			dExAmount = spa.getExAmount();
			dEndPrice1 = spa.getEndPrice1();
			dExAmount1 = spa.getExAmount();
			dEndPrice2 = spa.getEndPrice2();
			dExAmount2 = spa.getExAmount2();
			dEndPrice3 = spa.getEndPrice3();
			dExAmount3 = spa.getExAmount3();
			dEndPrice4 = spa.getEndPrice4();
			dExAmount4 = spa.getExAmount4();
			dEndPrice5 = spa.getEndPrice5();
			dExAmount5 = spa.getExAmount5();
			
			StockHistory sh = new StockHistory(strStockID,strStockBelong);
			AnalyzeHistory ah;
			ah = new AnalyzeMACD(sh);
			//Event e = ah.doAnalzy(sh.getHisData().size() - 1);
			Event e = ah.doAnalzy(0);
			
			if ((e != null) && (e.getExDate().equals(spa.getExDate()))){
				sb.append(e.getEventMsg());
				sb.append("[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '" + e.getEventMsg() + "' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else{
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				
				sb.append("],");
			}
			
			ah = new AnalyzeMyMACD(sh);
			e = ah.doAnalzy(0);
			if ((e != null) && (e.getExDate().equals(spa.getExDate()))){
				sb.append(e.getEventMsg());
				sb.append("[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '" + e.getEventMsg() + "' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
							
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				
				sb.append("],");
				
			}
			
			
			
			
			AnalysisParser ap = new AnalysisParser(spa.getStockID(),spa.getStockBelong());
			
			
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
			ah = new AnalyzeSigmaPrice(sh);
			ap.addParser(ah);
			
			ah = new AnalyzeSigmaQuantity(sh);
			ap.addParser(ah);
			
			ah = new AnalyzeSigmaSituation(sh);
			ap.addParser(ah);
			
			ah = new AnalyzeKDJ(sh);
			ap.addParser(ah);
			//ap.prepareParser();
			//ap.prepareAssertForcast();
			ap.doAnalyze();
			
			Iterator<Event>  iter = ap.getExDates().get(0).getEvents().iterator();
			Event event;
			
			while(iter.hasNext()){
				event = iter.next();
				sb.append(event.getEventMsg());
				sb.append("[");
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '" + event.getEventMsg() + "' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
								
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				
				sb.append("],");
			}
			
			
			// Rule 1: 5天涨幅20%
			if (dEndPrice > dEndPrice5 * 1.2) {
				sb.append("5天涨幅20%[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '5天涨幅20%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
								
				sb.append(df.format(dTemp));
				sb.append("],");
				
				
				
			}
			// Rule 2: 5天跌幅20%
			if (dEndPrice < dEndPrice5 * 0.8) {
				sb.append("5天跌幅20%[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '5天跌幅20%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("],");
			}
			// Rule 3： 连续3天放量10%
			if ((dExAmount > dExAmount1 * 1.1) && (dExAmount1 > dExAmount2 * 1.1) && (dExAmount2 > dExAmount3 * 1.1)) {
				sb.append("连续3天放量10%[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '3天放量10%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("],");
			}
			// Rule 4： 连续3天缩量10%
			if ((dExAmount < dExAmount1 * 0.9) && (dExAmount1 < dExAmount2 * 0.9) && (dExAmount2 < dExAmount3 * 0.9)) {
				sb.append("连续3天缩量10%[");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '3天缩量10%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("],");
			}
			// Rule 5： 连续5天量价齐涨
			if (((dEndPrice > dEndPrice1) && (dExAmount > dExAmount1)) 
					&& ((dEndPrice1 > dEndPrice2) && (dExAmount1 > dExAmount2))
					&& ((dEndPrice2 > dEndPrice3) && (dExAmount2 > dExAmount3))
					&& ((dEndPrice3 > dEndPrice4) && (dExAmount3 > dExAmount4))
					&& ((dEndPrice4 > dEndPrice5) && (dExAmount4 > dExAmount5))) {
				sb.append("连续5天量价齐涨,");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '连续  5天量价齐涨' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("],");
			}
			// Rule 6： 连续5天量价齐跌
			if (((dEndPrice < dEndPrice1) && (dExAmount < dExAmount1)) 
					&& ((dEndPrice1 < dEndPrice2) && (dExAmount1 < dExAmount2))
					&& ((dEndPrice2 < dEndPrice3) && (dExAmount2 < dExAmount3))
					&& ((dEndPrice3 < dEndPrice4) && (dExAmount3 < dExAmount4))
					&& ((dEndPrice4 < dEndPrice5) && (dExAmount4 < dExAmount5))) {
				sb.append("连续5天量价齐跌,");
				
				String strSQL = "select WinRate from T_HisWinRate where EventMsg = '连续 5天量价齐跌' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				List<Map> lm2 = mb.queryBySQL(strSQL);
				Iterator itr2 = lm2.iterator();
				Map m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				lm2 = mb.queryBySQL(strSQL);
				itr2 = lm2.iterator();
				m2 = null;
				if (itr2.hasNext()){
					m2 = (Map) itr2.next();
				}
				
				if((m2 != null) && (m2.get("WinRate") != null)){
					dTemp = ((Double) m2.get("WinRate"));
				}else {
					dTemp = 0;
				}
				
				sb.append(df.format(dTemp));
				sb.append("],");
			}
			
			
			
			if (sb.length() > 0){
				String strSQL = "update t_stock_past_adj set Msg = '" + sb.toString() + "' where StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				
				mb.executeSQL(strSQL);
			}
				
		}

	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnalzyPastAdj ap = new AnalzyPastAdj();
		try {
			ap.execute();
			ap.analzy();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
