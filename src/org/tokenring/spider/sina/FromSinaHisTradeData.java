package org.tokenring.spider.sina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;

public class FromSinaHisTradeData {
	
	String stockID;

	public String getStockID() {
		return stockID;
	}

	public void setStockID(String stockID) {
		this.stockID = stockID;
	}

	
	public FromSinaHisTradeData() {
		
	}

	

	

	

	public void execute() throws SQLException, ParseException, IOException, InterruptedException {
		//
		MyBatis mb = MyBatis.getInstance();
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo where NOT (StockID = '000001' and StockBelong = 'SH')  AND NOT (StockID = '399001' and StockBelong = 'SZ')";
		List<Map> lm = mb.queryBySQL(sql);
		
		Semaphore  semp = new Semaphore(30);
		Iterator itr = lm.iterator();
		Map m;
		while(itr.hasNext()){
			m = (Map) itr.next();
			System.out.println(m.get("StockID"));
			// 申请许可  
		    semp.acquire();  
			FromSinaHisTradeDataThread thread = new FromSinaHisTradeDataThread();
			thread.setStockID((String) m.get("StockID"));
			thread.setStockBelong((String) m.get("StockBelong"));
			thread.setSemp(semp);
			thread.start();
			
		}
			
		/*
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo where NOT (StockID = '000001' and StockBelong = 'SH')  AND NOT (StockID = '399001' and StockBelong = 'SZ')";
		ResultSet rs = mySQL.QueryBySQL(sql);
		
		
		
		
		Semaphore  semp = new Semaphore(30);
		while (rs.next()) {
			System.out.println(rs.getString(1));
			// 申请许可  
		    semp.acquire();  
			FromSinaHisTradeDataThread thread = new FromSinaHisTradeDataThread();
			thread.setStockID(rs.getString("StockID"));
			thread.setStockBelong(rs.getString("StockBelong"));
			thread.setSemp(semp);
			thread.start();
			
		}

		mySQL.destroy();
		//
		*/
	}

	

	public static void main(String[] args) throws IOException, ParseException, InterruptedException {
		// TODO Auto-generated method stub
		/*
		 * String date = "2016-05-10"; SimpleDateFormat sdf = new
		 * SimpleDateFormat("yyyy-MM-dd"); Date d = sdf.parse(date);
		 * System.out.println(d);
		 * 
		 * Logger log = Logger.getLogger(FromSinaHisTradeData.class);
		 * FromSinaHisTradeData fhtd = new FromSinaHisTradeData();
		 * fhtd.setStockID("600570"); //fhtd.queryFromSina(2016, 1); List rowRet
		 * = fhtd.genHisData("600570", "SH", "2016-03-10", "2016-03-10");
		 * 
		 * Iterator itr = rowRet.iterator(); while (itr.hasNext()) { String[]
		 * strs = (String[]) itr.next(); for (int i = 0; i < 10; i++) {
		 * log.info(strs[i]); } }
		 */

		FromSinaHisTradeData fhtd = new FromSinaHisTradeData();
		try {
			fhtd.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
