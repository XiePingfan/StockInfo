package org.tokenring.spider.sina;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;

public class UpdateExAmountFromSina {
	public void doUpdate() throws InterruptedException {
		//MySqlTrail mySQL = new MySqlTrail();
		//boolean b = mySQL.init();
		
		

		String stockID;
		String stockBelong;
		String mExDate;
		String mExDate2;
		StringBuffer sb;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
		String strToday = sdf.format(new Date());
		System.out.println(strToday);

//		String strRows = "";
//		try {
//			strRows = Integer.toString(GetNumRows(rs));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		

		int i = 1;


			String sql = "Select StockID,StockBelong,ExDate FROM t_stockhis_sina where ExAmount is null ";
			//String sql = "Select StockID,StockBelong,ExDate FROM t_stockhis_sina where StockID = '600570'";
			//ResultSet rs = mySQL.QueryBySQL(sql);
			MyBatis mb = MyBatis.getInstance();
			List<Map> lm = mb.queryBySQL(sql);
			Iterator<Map> itr = lm.iterator();
			Semaphore  semp = new Semaphore(40);
			Map m;
			while (itr.hasNext()) {
				if (i % 100 == 0) {
					strToday = sdf.format(new Date());
					System.out.println(strToday + " now:" + Integer.toString(i) + " of " );
				}
				m = (Map) itr.next();
				stockID = (String) m.get("StockID");
				stockBelong = (String) m.get("StockBelong");
				mExDate = (String) m.get("ExDate");
				mExDate2 = mExDate.substring(0, 4) + "-" + mExDate.substring(4, 6) + "-" + mExDate.substring(6, 8);

				// 申请许可  
			    semp.acquire(); 
				UpdateExAmountFromSinaThread thread = new UpdateExAmountFromSinaThread();
				thread.setStockID(stockID);
				thread.setStockBelong(stockBelong);
				thread.setSemp(semp);
				thread.setmExDate(mExDate);
				thread.setmExDate2(mExDate2);
				thread.start();
				
				i++;
				
			}

		strToday = sdf.format(new Date());
		System.out.println("End:" + strToday);
		
		//mySQL.destroy();
		
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

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		/*String mExDate = "20140205";
		String mExDate2 = mExDate.substring(0, 4) + "-" + mExDate.substring(4, 6) + "-" + mExDate.substring(6, 8);
		System.out.println(mExDate2);*/
		
		System.out.println(System.currentTimeMillis());
		UpdateExAmountFromSina uefs = new UpdateExAmountFromSina();
		uefs.doUpdate();
		/*
		Semaphore  semp = new Semaphore(1);
		UpdateExAmountFromSinaThread thread = new UpdateExAmountFromSinaThread();
		thread.setStockID("000895");
		thread.setStockBelong("SZ");
		thread.setSemp(semp);
		thread.setmExDate("20170308");
		thread.setmExDate2("2017-03-08");
		thread.start();
		*/
		System.out.println(System.currentTimeMillis());
	}

}
