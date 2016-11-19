package org.tokenring.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.tokenring.db.MySqlTrail;

public class StockHistory {
	String StockID;
	String StockBelong;
	String StockName;
	List HisData;
	boolean isCaclMACD = false;
	boolean isCaclMyMACD = false;

	public void calcMyMACD() {
		// todo
		if (isCaclMyMACD) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			StockExchangeData sedToday = getHisDataByExDate(idx);
			StockExchangeData sedYesterday = getHisDataByExDate(idx + 1);

			if (sedYesterday != null) {
				calcAverageQuantity(idx,11);
				Double newMyEMA12 = sedYesterday.getMyEMA12() * 11 / 13 + (double)sedToday.getExAmount() / sedToday.getExQuantity() * 2 / 13 * sedToday.getExQuantity() / sedToday.getAverageQuantity();
				calcAverageQuantity(idx,25);
				Double newMyEMA26 = sedYesterday.getMyEMA26() * 25 / 27 + (double)sedToday.getExAmount() / sedToday.getExQuantity() * 2 / 27 * sedToday.getExQuantity() / sedToday.getAverageQuantity();
				Double newMyDIF = newMyEMA12 - newMyEMA26;
				Double newMyDEA = sedYesterday.getMyDEA() * 8 / 10 + newMyDIF * 2 / 10;
				Double newMyMACD = (newMyDIF - newMyDEA) * 2;

				sedToday.setMyEMA12(newMyEMA12);
				sedToday.setMyEMA26(newMyEMA26);
				sedToday.setMyDIF(newMyDIF);
				sedToday.setMyDEA(newMyDEA);
				sedToday.setMyMACD(newMyMACD);
			} else {
				// sedYesterday == null 表示是第一天
				// DIFF = 0,DEA = 0,MACD = 0,EMA12 = EMA26 = 收盘价
				sedToday.setMyDIF(0.0);
				sedToday.setMyDEA(0.0);
				sedToday.setMyMACD(0.0);
				sedToday.setMyEMA12((double)sedToday.getExAmount() / sedToday.getExQuantity());
				sedToday.setMyEMA26((double)sedToday.getExAmount() / sedToday.getExQuantity());
			}

		}
		isCaclMyMACD = true;
	}
	
	public void calcMACD() {
		// todo
		if (isCaclMACD) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			StockExchangeData sedToday = getHisDataByExDate(idx);
			StockExchangeData sedYesterday = getHisDataByExDate(idx + 1);

			if (sedYesterday != null) {
				Double newEMA12 = sedYesterday.getEMA12() * 11 / 13 + sedToday.getEndPrice() * 2 / 13;
				Double newEMA26 = sedYesterday.getEMA26() * 25 / 27 + sedToday.getEndPrice() * 2 / 27;
				Double newDIF = newEMA12 - newEMA26;
				Double newDEA = sedYesterday.getDEA() * 8 / 10 + newDIF * 2 / 10;
				Double newMACD = (newDIF - newDEA) * 2;

				sedToday.setEMA12(newEMA12);
				sedToday.setEMA26(newEMA26);
				sedToday.setDIF(newDIF);
				sedToday.setDEA(newDEA);
				sedToday.setMACD(newMACD);
			} else {
				// sedYesterday == null 表示是第一天
				// DIFF = 0,DEA = 0,MACD = 0,EMA12 = EMA26 = 收盘价
				sedToday.setDIF(0.0);
				sedToday.setDEA(0.0);
				sedToday.setMACD(0.0);
				sedToday.setEMA12(sedToday.getEndPrice());
				sedToday.setEMA26(sedToday.getEndPrice());
			}

		}
		isCaclMACD = true;
	}

	public void calcAveragePrice(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumPrice = (double) 0;
			for (int i = 0; i < days; i++) {
				sumPrice += getHisDataByExDate(idx + i).getEndPrice();
			}
			getHisDataByExDate(idx).setAveragePrice(sumPrice / days);
		} else {
			getHisDataByExDate(idx).setAveragePrice((double) -1);
		}
	}

	public void calcAverageQuantity(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumQuantity = (double) 0;
			for (int i = 0; i < days; i++) {
				sumQuantity += getHisDataByExDate(idx + i).getExQuantity();
			}
			getHisDataByExDate(idx).setAverageQuantity(sumQuantity / days);
		} else {
			getHisDataByExDate(idx).setAverageQuantity((double) -1);
		}
	}

	public void calcAverageAmount(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumAmount = (double) 0;
			for (int i = 0; i < days; i++) {
				sumAmount += getHisDataByExDate(idx + i).getExAmount();
			}
			getHisDataByExDate(idx).setAverageAmount(sumAmount / days);
		} else {
			getHisDataByExDate(idx).setAverageAmount((double) -1);
		}
	}

	public String getStockID() {
		return StockID;
	}

	public void setStockID(String stockID) {
		StockID = stockID;
	}

	public String getStockBelong() {
		return StockBelong;
	}

	public void setStockBelong(String stockBelong) {
		StockBelong = stockBelong;
	}

	public String getStockName() {
		return StockName;
	}

	public void setStockName(String stockName) {
		StockName = stockName;
	}

	public List getHisData() {
		return HisData;
	}

	public void setHisData(List hisData) {
		HisData = hisData;
	}

	public StockExchangeData getHisDataByExDate(String ExDate) {
		int isize = HisData.size();
		int i = 0;
		boolean bMatch = false;
		StockExchangeData sed = null;
		while ((i < isize) && !bMatch) {
			sed = (StockExchangeData) (HisData.get(i));
			bMatch = sed.getExDate().equals(ExDate);
			i++;
		}

		if (bMatch) {
			return sed;
		} else {
			return null;
		}
	}

	public int getIdxByExDate(String ExDate) {
		int isize = HisData.size();
		int i = 0;
		boolean bMatch = false;
		StockExchangeData sed = null;
		while ((i < isize) && !bMatch) {
			sed = (StockExchangeData) (HisData.get(i));
			bMatch = sed.getExDate().equals(ExDate);
			i++;
		}

		if (bMatch) {
			return i - 1;
		} else {
			return -1;
		}
	}

	public StockExchangeData getHisDataByExDate(int idx) {
		if ((idx >= 0) && (idx < HisData.size())) {
			return (StockExchangeData) HisData.get(idx);
		} else {
			return null;
		}

	}

	public StockHistory(String StockID, String StockBelong) throws SQLException {
		this.StockID = StockID;
		this.StockBelong = StockBelong;
		this.HisData = new ArrayList();
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		String strSQL = "select StockName from T_StockBaseInfo where StockID = '" + StockID + "' and StockBelong = '"
				+ StockBelong + "' limit 1";
		ResultSet rs = mySQL.QueryBySQL(strSQL);

		if (rs.next()) {
			this.StockName = rs.getString(1);
		}
		rs.close();
		// initHisData
		// strSQL = "select
		// ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity,ExAmount
		// from t_stockhis_sina where StockID = '" + StockID + "' and
		// StockBelong = '" + StockBelong + "' order by ExDate desc";
		strSQL = "select ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity/10000,ExAmount/10000 from t_stockadjhis_sina where StockID = '"
				+ StockID + "' and StockBelong = '" + StockBelong + "' order by ExDate desc";
		rs = mySQL.QueryBySQL(strSQL);

		while (rs.next()) {
			StockExchangeData sed = new StockExchangeData(rs.getString(1), rs.getDouble(2), rs.getDouble(3),
					rs.getDouble(4), rs.getDouble(5), rs.getInt(6), rs.getInt(7));

			HisData.add(sed);
		}
		rs.close();
		mySQL.destroy();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			StockHistory st = new StockHistory("600570", "SH");

			System.out.println("StockID:" + st.getStockID());
			System.out.println("StockName:" + st.getStockName());
			System.out.println("StockBelong:" + st.getStockBelong());
			System.out.println("20150407 EndPrice :" + st.getHisDataByExDate("2015-04-07").getEndPrice());
			System.out.println("20160405 EndPrice :" + st.getHisDataByExDate(1).getEndPrice());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
