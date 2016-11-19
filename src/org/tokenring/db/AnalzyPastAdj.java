package org.tokenring.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.tokenring.analysis.AnalysisParser;
import org.tokenring.analysis.AnalyzeAverageAmount;
import org.tokenring.analysis.AnalyzeAveragePrice;
import org.tokenring.analysis.AnalyzeHistory;
import org.tokenring.analysis.AnalyzeMACD;
import org.tokenring.analysis.AnalyzeMyMACD;
import org.tokenring.analysis.Event;
import org.tokenring.analysis.StockHistory;

public class AnalzyPastAdj {
	public void execute() throws SQLException {
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		String sql = "truncate table t_stock_past_adj";
		mySQL.executeSQL(sql);

		String strDay;

		MySqlTrail mySQL2 = new MySqlTrail();
		b = mySQL2.init();
		//sql = "select concat(substring(ExDate,1,4),'-',substring(ExDate,5,2),'-',substring(ExDate,7,2)) newExDate, ExDate from t_stockhis_sina where StockID = '000001' and stockbelong = 'SH' order by exdate desc limit 6";
		sql = "SELECT exdate newExDate FROM t_stockadjhis_sina t WHERE t.StockID = '000001'  ORDER BY exdate DESC  LIMIT 6";
		ResultSet rs2 = mySQL2.QueryBySQL(sql);
		if (rs2.next()) {
			// 处理当天数据
			strDay = rs2.getString("newExDate");
			sql = "insert into t_stock_past_adj(StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount) select StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount from t_stockadjhis_sina where ExDate ='"
					+ strDay + "'";
			mySQL.executeSQL(sql);

			sql = "update t_stock_past_adj t1,T_StockBaseInfo t2 set t1.StockName = t2.StockName where t1.StockID=t2.StockID and t1.StockBelong = t2.StockBelong";
			mySQL.executeSQL(sql);
		}

		int i = 1;
		while (rs2.next()) {
			// 处理后续5天数据
			strDay = rs2.getString("newExDate");
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

			mySQL.executeSQL(sb.toString());
		}

		mySQL.destroy();
		mySQL2.destroy();
	}

	public void analzy() throws SQLException {
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		String strSQL = "select StockID,StockBelong,ExDate,EndPrice,ExAmount,EndPrice1,ExAmount1,EndPrice2,ExAmount2,EndPrice3,ExAmount3,EndPrice4,ExAmount4,EndPrice5,ExAmount5 from t_stock_past_adj";
		ResultSet rs = mySQL.QueryBySQL(strSQL);

		String strStockID, strStockBelong, strExDate;
		Double dEndPrice, dExAmount, dEndPrice1, dExAmount1, dEndPrice2, dExAmount2, dEndPrice3, dExAmount3, dEndPrice4,
				dExAmount4, dEndPrice5, dExAmount5;
		StringBuffer sb;

		while (rs.next()) {
			strStockID = rs.getString("StockID");
			strStockBelong = rs.getString("StockBelong");
			if (("000001".equals(strStockID) && "SH".equals("strStockBelong"))
					|| ("399001".equals(strStockID) && "SZ".equals("strStockBelong"))) {
				// 上证指数，深圳成指 不分析
				continue;
			}
			sb = new StringBuffer();
			dEndPrice = rs.getDouble("EndPrice");
			dExAmount = rs.getDouble("ExAmount");
			dEndPrice1 = rs.getDouble("EndPrice1");
			dExAmount1 = rs.getDouble("ExAmount1");
			dEndPrice2 = rs.getDouble("EndPrice2");
			dExAmount2 = rs.getDouble("ExAmount2");
			dEndPrice3 = rs.getDouble("EndPrice3");
			dExAmount3 = rs.getDouble("ExAmount3");
			dEndPrice4 = rs.getDouble("EndPrice4");
			dExAmount4 = rs.getDouble("ExAmount4");
			dEndPrice5 = rs.getDouble("EndPrice5");
			dExAmount5 = rs.getDouble("ExAmount5");
			
			StockHistory sh = new StockHistory(strStockID,strStockBelong);
			AnalyzeHistory ah;
			ah = new AnalyzeMACD(sh);
			//Event e = ah.doAnalzy(sh.getHisData().size() - 1);
			Event e = ah.doAnalzy(0);
			
			if ((e != null) && (e.getExDate().equals(rs.getString("ExDate")))){
				sb.append(e.getEventMsg());
				sb.append(",");
			}
			
			ah = new AnalyzeMyMACD(sh);
			e = ah.doAnalzy(0);
			if ((e != null) && (e.getExDate().equals(rs.getString("ExDate")))){
				sb.append(e.getEventMsg());
				sb.append(",");
			}
			/*
			AnalysisParser ap = new AnalysisParser(rs.getString("StockID"), rs.getString("StockBelong"));
			
			AnalyzeHistory ah;
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
			ap.prepareParser();
			ap.prepareAssertForcast();
			ap.doAnalyze();
			
			Iterator<Event>  iter = ap.getExDates().get(0).getEvents().iterator();
			Event e;
			while(iter.hasNext()){
				e = iter.next();
				sb.append(e.getEventMsg());
				sb.append(",");
			}
			*/
			double dTemp;
			// Rule 1: 5天涨幅20%
			if (dEndPrice > dEndPrice5 * 1.2) {
				sb.append("5天涨幅20%[");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '5天涨幅20%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
				
				
				
			}
			// Rule 2: 5天跌幅20%
			if (dEndPrice < dEndPrice5 * 0.8) {
				sb.append("5天跌幅20%[");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '5天跌幅20%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
			}
			// Rule 3： 连续3天放量10%
			if ((dExAmount > dExAmount1 * 1.1) && (dExAmount1 > dExAmount2 * 1.1) && (dExAmount2 > dExAmount3 * 1.1)) {
				sb.append("连续3天放量10%[");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '3天放量10%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
			}
			// Rule 4： 连续3天缩量10%
			if ((dExAmount < dExAmount1 * 0.9) && (dExAmount1 < dExAmount2 * 0.9) && (dExAmount2 < dExAmount3 * 0.9)) {
				sb.append("连续3天缩量10%[");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '3天缩量10%' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
			}
			// Rule 5： 连续5天量价齐涨
			if (((dEndPrice > dEndPrice1) && (dExAmount > dExAmount1)) 
					&& ((dEndPrice1 > dEndPrice2) && (dExAmount1 > dExAmount2))
					&& ((dEndPrice2 > dEndPrice3) && (dExAmount2 > dExAmount3))
					&& ((dEndPrice3 > dEndPrice4) && (dExAmount3 > dExAmount4))
					&& ((dEndPrice4 > dEndPrice5) && (dExAmount4 > dExAmount5))) {
				sb.append("连续5天量价齐涨,");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '连续  5天量价齐涨' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
			}
			// Rule 6： 连续5天量价齐跌
			if (((dEndPrice < dEndPrice1) && (dExAmount < dExAmount1)) 
					&& ((dEndPrice1 < dEndPrice2) && (dExAmount1 < dExAmount2))
					&& ((dEndPrice2 < dEndPrice3) && (dExAmount2 < dExAmount3))
					&& ((dEndPrice3 < dEndPrice4) && (dExAmount3 < dExAmount4))
					&& ((dEndPrice4 < dEndPrice5) && (dExAmount4 < dExAmount5))) {
				sb.append("连续5天量价齐跌,");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = '连续 5天量价齐跌' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				ResultSet rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("/");
				
				strSQL = "select WinRate from T_HisWinRate where EventMsg = 'BTest' and StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				rsTemp = mySQL.QueryBySQL(strSQL);
				if(rsTemp.next()){
					dTemp = rsTemp.getDouble("WinRate");
				}else {
					dTemp = 0;
				}
				
				sb.append(dTemp);
				sb.append("],");
			}
			
			
			
			if (sb.length() > 0){
				strSQL = "update t_stock_past_adj set Msg = '" + sb.toString() + "' where StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				mySQL.executeSQL(strSQL);
			}
				
		}
		rs.close();
		mySQL.destroy();
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
