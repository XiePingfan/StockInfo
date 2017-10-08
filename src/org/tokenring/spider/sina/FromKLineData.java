
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

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
	public void execute() throws Exception {
		MyBatis mb = MyBatis.getInstance();
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo";
		List<Map> lm = mb.queryBySQL(sql);
		int iRowNum = lm.size();
		String stockID;
		String stockBelong;
		KLineWorkThread thread;
		Semaphore  semp = new Semaphore(30);
		Iterator itr = lm.iterator();
		Map m;
		int i = 1;
		while (itr.hasNext()) {
			m = (Map) itr.next();
			
			stockID = (String) m.get("StockID");
			stockBelong = (String) m.get("StockBelong");
			
			System.out.println("now:[" + i++ + " of "+ iRowNum + "]" + stockID + "." + stockBelong);
			// 申请许可  
		    semp.acquire();  
			thread = new KLineWorkThread();
			thread.setSemp(semp);
			thread.setStockID(stockID);
			thread.setStockBelong(stockBelong);
			thread.start();
		}
		
		/*
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		//MySqlTrail mySQL2 = new MySqlTrail();
		//b = mySQL2.init();
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo";
		ResultSet rs = mySQL.QueryBySQL(sql);
		int iRowNum = GetNumRows(rs);

		String stockID;
		String stockBelong;
		//String mExDate;
		//StringBuffer sb;
		KLineWorkThread thread;
		Semaphore  semp = new Semaphore(30);
		
		

		while (rs.next()) {
			stockID = rs.getString("StockID");
			stockBelong = rs.getString("StockBelong");
			System.out.println("now:[" + rs.getRow() + " of "+ iRowNum + "]" + stockID + "." + stockBelong);
			// 申请许可  
		    semp.acquire();  
			thread = new KLineWorkThread();
			thread.setSemp(semp);
			thread.setStockID(stockID);
			thread.setStockBelong(stockBelong);
			thread.start();
		}
		mySQL.destroy();
		//mySQL2.destroy();
		 * 
		 */
	}

	private int GetNumRows(ResultSet rs) throws Exception {

		// 通过改方法获取结果集的行数

		int result = 0;

		if (rs.last()) {

			result = rs.getRow();

			rs.beforeFirst();// 光标回滚

		}

		return result;

	}

	public static void main(String[] args){
		FromKLineData fkld = new FromKLineData();

		try {
			System.out.print("Begin:");
			System.out.println(new Date());
			fkld.execute();
			System.out.print("End:");
			System.out.println(new Date());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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