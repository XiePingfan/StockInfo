
package org.tokenring.spider.sina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.tokenring.db.*;

/*
 * QUERYURL?&REF1&REF2&REF3&REF4&REF5
 * QUERYURL : http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php
 * REF1 : rand=random(10000)
 * REF2 : symbol=sh600887         证券代码，前缀为归属市场
 * REF3 : end_date=20160105		      结束日期
 * REF4 : begin_date=20000101      开始日期
 * REF5 : type=plain			   送此参数返回文本格式，逗号分隔，一行一条记录。不送这个参数返回xml格式
  example : http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php?&rand=random(10000)&symbol=sh600887&end_date=20160105&begin_date=20000101&type=plain"
 */
public class FromKLineData {
	public void execute() throws MalformedURLException, UnsupportedEncodingException, SQLException, IOException {
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		MySqlTrail mySQL2 = new MySqlTrail();
		b = mySQL2.init();
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo";
		ResultSet rs = mySQL.QueryBySQL(sql);

		String stockID;
		String stockBelong;
		String mExDate;
		StringBuffer sb;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String strToday = sdf.format(new Date());
		URL ur = null;
		HttpURLConnection uc;
		String line;

		while (rs.next()) {
			stockID = rs.getString("StockID");
			stockBelong = rs.getString("StockBelong");
			System.out.println("now:[" + rs.getRow() + " of 83]" + stockID + "." + stockBelong);

			sql = "Select MAX(ExDate) mExDate FROM t_stockhis_sina where StockID = '" + stockID
					+ "' AND StockBelong = '" + stockBelong + "'";
			ResultSet rs2 = mySQL2.QueryBySQL(sql);
			rs2.next();
			mExDate = rs2.getString("mExDate");
			if (mExDate == null) {
				mExDate = "20000101";
			}

			sb = new StringBuffer();
			sb.append("http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php?&rand=random(10000)&symbol=");
			sb.append(stockBelong.toLowerCase());
			sb.append(stockID);
			sb.append("&end_date=");
			sb.append(strToday);
			sb.append("&begin_date=");
			sb.append(mExDate);
			sb.append("&type=plain");

			ur = new URL(sb.toString());
			
			uc = (HttpURLConnection) ur.openConnection();
			//设置超时1分钟
			uc.setConnectTimeout(60000);
			uc.setReadTimeout(60000);
			//BufferedReader reader = new BufferedReader(new InputStreamReader(ur.openStream(), "GBK"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(),"GBK"));

			while ((line = reader.readLine()) != null) {

				String[] strArr = line.split(",");

				if ( mExDate.equals(strArr[0].replace("-", ""))) {
					continue;
				}
				sb = new StringBuffer();
				sb.append(
						"INSERT INTO t_stockhis_sina (StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity) VALUES ('");
				sb.append(stockID); // StockID
				sb.append("','");
				sb.append(stockBelong); // StockBelong
				sb.append("','");
				sb.append(strArr[0].replace("-", "")); // ExDate
				sb.append("','");
				sb.append(strArr[1]); // BeginPrice
				sb.append("','");
				sb.append(strArr[2]); // HighestPrice
				sb.append("','");
				sb.append(strArr[3]); // EndPrice
				sb.append("','");
				sb.append(strArr[4]); // LowestPrice
				sb.append("','");
				sb.append(strArr[5]); // ExQuantity
				sb.append("')");

				mySQL2.executeSQL(sb.toString());
			}
		}
		mySQL.destroy();
		mySQL2.destroy();
	}

	public static void main(String[] args) throws MalformedURLException {
		FromKLineData fkld = new FromKLineData();

		try {
			System.out.print("Begin:");
			System.out.println(new Date());
			fkld.execute();
			System.out.print("End:");
			System.out.println(new Date());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * boolean b = MySqlTrail.init(); String sql =
		 * "Select MAX(ExDate) mExDate  FROM T_StockHis_Sina where StockID = '"
		 * + "000046" + "' AND StockBelong = '" + "SZ" + "'";
		 * System.out.println(sql); ResultSet rs2 = MySqlTrail.QueryBySQL(sql);
		 * String mExDate; if (rs2.next()){ mExDate = rs2.getString("mExDate");
		 * } else{ mExDate = "empty"; } System.out.println(mExDate);
		 * MySqlTrail.destroy();
		 */

	}

}