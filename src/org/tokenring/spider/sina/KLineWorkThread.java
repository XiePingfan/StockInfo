package org.tokenring.spider.sina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;

public class KLineWorkThread extends Thread {
	String stockID;
	String stockBelong;
	Semaphore  semp;
	public String getStockID() {
		return stockID;
	}
	public void setStockID(String stockID) {
		this.stockID = stockID;
	}
	public String getStockBelong() {
		return stockBelong;
	}
	public void setStockBelong(String stockBelong) {
		this.stockBelong = stockBelong;
	}
	
	
	public Semaphore getSemp() {
		return semp;
	}
	public void setSemp(Semaphore semp) {
		this.semp = semp;
	}
	public void run(){
		    
		    try {  
		        // 业务逻辑  
		    	execute();
		    } catch (Exception e) {  
		  
		    } finally {  
		        // 释放许可  
		        semp.release();  
		    }  

		
	}
	private void execute() throws SQLException, IOException{
        //todo
		String sql;
		String mExDate;
		MyBatis mb = MyBatis.getInstance();
		sql = "Select MAX(ExDate) mExDate FROM t_stockhis_sina where StockID = '" + stockID
				+ "' AND StockBelong = '" + stockBelong + "'";
		List<Map> lm = mb.queryBySQL(sql);
		Iterator<Map> itr = lm.iterator();
		if (itr.hasNext()){
			Map m = itr.next();
			if(m == null){
				mExDate = "20000101";
			}else{
				mExDate = (String) m.get("mExDate");
				
				if(mExDate == null){
					mExDate = "20000101";
				}
			}
		}else{
			mExDate = "20000101";
		}
		/*
		MySqlTrail mySQL1 = new MySqlTrail();
		//MySqlTrail mySQL2 = new MySqlTrail();
		boolean b;
		b = mySQL1.init();
		//b = mySQL2.init();
		
		
		
		sql = "Select MAX(ExDate) mExDate FROM t_stockhis_sina where StockID = '" + stockID
				+ "' AND StockBelong = '" + stockBelong + "'";
		ResultSet rs2 = mySQL1.QueryBySQL(sql);
		rs2.next();
		//mySQL1.destroy();
		
		mExDate = rs2.getString("mExDate");
		if (mExDate == null) {
			mExDate = "20000101";
		}
		*/

		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String strToday = sdf.format(new Date());
		
		sb.append("http://biz.finance.sina.com.cn/stock/flash_hq/kline_data.php?&rand=random(10000)&symbol=");
		sb.append(stockBelong.toLowerCase());
		sb.append(stockID);
		sb.append("&end_date=");
		sb.append(strToday);
		sb.append("&begin_date=");
		sb.append(mExDate);
		sb.append("&type=plain");

		URL ur = null;
		HttpURLConnection uc;
		ur = new URL(sb.toString());
		
		uc = (HttpURLConnection) ur.openConnection();
		//设置超时1分钟
		uc.setConnectTimeout(60000);
		uc.setReadTimeout(60000);
		//BufferedReader reader = new BufferedReader(new InputStreamReader(ur.openStream(), "GBK"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(),"GBK"));


		String line;
		while ((line = reader.readLine()) != null) {

			String[] strArr = line.split(",");

			if ( mExDate.equals(strArr[0].replace("-", ""))) {
				continue;
			}
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("stockId", stockID);
			params.put("stockBelong", stockBelong);
			params.put("exDate", strArr[0].replace("-", ""));
			params.put("beginPrice", strArr[1]);
			params.put("highestPrice", strArr[2]);
			params.put("endPrice", strArr[3]);
			params.put("lowestPrice",strArr[4]);
			params.put("exQuantity",strArr[5]);
			
			mb.insertByLabel("in_stockhis_sina", params);
			
			/*
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

			mySQL1.executeSQL(sb.toString());
			*/
		}
		
		//mySQL1.destroy();
    } 

}
