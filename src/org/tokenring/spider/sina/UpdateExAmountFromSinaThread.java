package org.tokenring.spider.sina;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;

public class UpdateExAmountFromSinaThread extends Thread {
	String stockID;
	String stockBelong;
	String mExDate;
	String mExDate2;
	Semaphore semp;

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

	public String getmExDate() {
		return mExDate;
	}

	public void setmExDate(String mExDate) {
		this.mExDate = mExDate;
	}

	public String getmExDate2() {
		return mExDate2;
	}

	public void setmExDate2(String mExDate2) {
		this.mExDate2 = mExDate2;
	}

	public Semaphore getSemp() {
		return semp;
	}

	public void setSemp(Semaphore semp) {
		this.semp = semp;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private void execute() {
		// System.out.println(stockID + ":" + stockBelong + ":" + mExDate2);
		FromTradeHisByDate fthbd = new FromTradeHisByDate();
		String strHisTrade;
		strHisTrade = fthbd.queryTradeHisByDate(stockBelong.toLowerCase() + stockID, mExDate2);

		System.out.println("queryTradeHisByDate:" + strHisTrade);
		if (strHisTrade == "") {
			System.out.println("call queryTradeHisByDate error.+[" + stockID + "][" + mExDate2 + "]");

			return;
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("exAmount", strHisTrade);
		params.put("stockId", stockID);
		params.put("stockBelong", stockBelong);
		params.put("exDate", mExDate);
		MyBatis mb = MyBatis.getInstance();
		mb.updateByLabel("ud_stockhis_sina", params);
		/*
		 * StringBuffer sb = new StringBuffer(); sb.append(
		 * "Update t_stockhis_sina set ExAmount = "); sb.append(strHisTrade);
		 * sb.append(" where StockID = '"); sb.append(stockID); sb.append(
		 * "' and StockBelong = '"); sb.append(stockBelong); sb.append(
		 * "' and ExDate = '"); sb.append(mExDate); sb.append("'");
		 * //System.out.println(sb.toString()); MySqlTrail mySQL2 = new
		 * MySqlTrail(); mySQL2.init(); if (!mySQL2.executeSQL(sb.toString())){
		 * mySQL2.destroy(); mySQL2 = new MySqlTrail(); mySQL2.init();
		 * mySQL2.executeSQL(sb.toString()); } mySQL2.destroy();
		 */
	}

	public void run() {
		Logger log = Logger.getLogger(UpdateExAmountFromSinaThread.class);

		try {
			// 业务逻辑
			execute();
			System.out.println("run() end." + System.currentTimeMillis());
		} catch (Exception e) {
			log.error(e);
		} finally {
			// 释放许可
			semp.release();
		}
	}

}
