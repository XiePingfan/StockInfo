package org.tokenring.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnalzyPast {

	public void execute() throws SQLException {
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		String sql = "truncate table t_stock_past";
		mySQL.executeSQL(sql);

		String strDay;

		MySqlTrail mySQL2 = new MySqlTrail();
		b = mySQL2.init();
		sql = "select * from t_stockhis_sina where StockID = '000001' and stockbelong = 'SH' order by exdate desc limit 6";
		ResultSet rs2 = mySQL2.QueryBySQL(sql);
		if (rs2.next()) {
			// 处理当天数据
			strDay = rs2.getString("ExDate");
			sql = "insert into t_stock_past(StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount) select StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExAmount from t_stockhis_sina where ExDate ='"
					+ strDay + "'";
			mySQL.executeSQL(sql);

			sql = "update t_stock_past t1,T_StockBaseInfo t2 set t1.StockName = t2.StockName where t1.StockID=t2.StockID and t1.StockBelong = t2.StockBelong";
			mySQL.executeSQL(sql);
		}

		int i = 1;
		while (rs2.next()) {
			// 处理后续5天数据
			strDay = rs2.getString("ExDate");
			StringBuffer sb = new StringBuffer();
			sb.append("update t_stock_past t1, t_stockhis_sina t2 ");
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

		String strSQL = "select StockID,StockBelong,ExDate,EndPrice,ExAmount,EndPrice1,ExAmount1,EndPrice2,ExAmount2,EndPrice3,ExAmount3,EndPrice4,ExAmount4,EndPrice5,ExAmount5 from t_stock_past";
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

			// Rule 1: 5天涨幅20%
			if (dEndPrice > dEndPrice5 * 1.2) {
				sb.append("5天涨幅20%,");
			}
			// Rule 2: 5天跌幅20%
			if (dEndPrice < dEndPrice5 * 0.8) {
				sb.append("5天跌幅20%,");
			}
			// Rule 3： 连续3天放量10%
			if ((dExAmount1 > dExAmount2 * 1.1) && (dExAmount2 > dExAmount3 * 1.1) && (dExAmount3 > dExAmount4 * 1.1)) {
				sb.append("连续3天放量10%,");
			}
			// Rule 4： 连续3天缩量10%
			if ((dExAmount1 < dExAmount2 * 0.9) && (dExAmount2 < dExAmount3 * 0.9) && (dExAmount3 < dExAmount4 * 0.9)) {
				sb.append("连续3天缩量10%,");
			}
			// Rule 5： 连续5天量价齐涨
			if (((dEndPrice > dEndPrice1)) && ((dEndPrice1 > dEndPrice2) && (dExAmount1 > dExAmount2))
					&& ((dEndPrice2 > dEndPrice3) && (dExAmount2 > dExAmount3))
					&& ((dEndPrice3 > dEndPrice4) && (dExAmount3 > dExAmount4))
					&& ((dEndPrice4 > dEndPrice5) && (dExAmount4 > dExAmount5))) {
				sb.append("连续5天量价齐涨,");
			}
			// Rule 6： 连续5天量价齐跌
			if (((dEndPrice < dEndPrice1)) && ((dEndPrice1 < dEndPrice2) && (dExAmount1 < dExAmount2))
					&& ((dEndPrice2 < dEndPrice3) && (dExAmount2 < dExAmount3))
					&& ((dEndPrice3 < dEndPrice4) && (dExAmount3 < dExAmount4))
					&& ((dEndPrice4 < dEndPrice5) && (dExAmount4 < dExAmount5))) {
				sb.append("连续5天量价齐跌,");
			}
			
			if (sb.length() > 0){
				strSQL = "update t_stock_past set Msg = '" + sb.toString() + "' where StockID = '" + strStockID + "' and StockBelong = '" + strStockBelong + "'";
				mySQL.executeSQL(strSQL);
			}
				
		}
		rs.close();
		mySQL.destroy();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnalzyPast ap = new AnalzyPast();
		try {
			ap.execute();
			ap.analzy();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
