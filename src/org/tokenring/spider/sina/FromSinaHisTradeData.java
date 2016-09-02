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

import org.apache.log4j.Logger;
import org.tokenring.db.MySqlTrail;

public class FromSinaHisTradeData {
	List<String[]> rows;
	String stockID;

	public String getStockID() {
		return stockID;
	}

	public void setStockID(String stockID) {
		this.stockID = stockID;
	}

	public List<String[]> getRows() {
		return rows;
	}

	public void setRows(List<String[]> rows) {
		this.rows = rows;
	}

	public FromSinaHisTradeData() {
		rows = new ArrayList<String[]>();

	}

	public void queryFromSina(String beginDate, String endDate) throws ParseException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateBegin = sdf.parse(beginDate);
		Date dateEnd = sdf.parse(endDate);

		DateFormat df1 = DateFormat.getDateInstance();
		int yearBegin = dateBegin.getYear() + 1900;
		int yearEnd = dateEnd.getYear() + 1900;
		int quarterBegin = calcQuarter(dateBegin.getMonth() + 1);

		int quarterEnd = calcQuarter(dateEnd.getMonth() + 1);

		while (yearEnd > yearBegin) {
			while (quarterEnd >= 1) {
				queryFromSina(yearEnd, quarterEnd);
				quarterEnd--;
			}
			quarterEnd = 4;
			yearEnd--;
		}
		while (quarterEnd >= quarterBegin) {
			queryFromSina(yearBegin, quarterEnd);
			quarterEnd--;
		}

	}

	private void queryFromSina(int yearEnd, int quarterEnd) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/");
		sb.append(stockID);
		sb.append(".phtml?year=");
		sb.append(yearEnd);
		sb.append("&jidu=");
		sb.append(quarterEnd);
		System.out.println(sb.toString());
		URL ur = new URL(sb.toString());

		HttpURLConnection uc = (HttpURLConnection) ur.openConnection();
		//System.out.println("openConnection");
		// …Ë÷√≥¨ ±1∑÷÷”
		uc.setConnectTimeout(60000);

		uc.setReadTimeout(60000);

		// BufferedReader reader = new BufferedReader(new
		// InputStreamReader(ur.openStream(), "GBK"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "GBK"));
		//System.out.println("openStream");

		String line;
		String[] row;

		line = reader.readLine();
		while (line != null) {
			try {
				if (line.contains("<a target='_blank' href='")) {
					/*
					 * 
					 * <a target='_blank'
					 * href='http://vip.stock.finance.sina.com.cn/
					 * quotes_service/
					 * view/vMS_tradehistory.php?symbol=sh600570&date=2016-05-10
					 * '> 2016-05-10 </a> </div></td> <td><div
					 * align="center">741.275</div></td> <td><div
					 * align="center">741.275</div></td> <td><div
					 * align="center">728.109</div></td> <td class="tdr"><div
					 * align="center">721.042</div></td> <td class="tdr"><div
					 * align="center">17225458.000</div></td> <td
					 * class="tdr"><div align="center">908285533.000</div></td>
					 * <td class="tdr"><div align="center">13.858</div></td>
					 * </tr>
					 * 
					 */
					line = reader.readLine();
					row = new String[8];
					for (int i = 0; i < 8; i++) {
						row[i] = "";
					}
					if (line != null) {
						int iend = line.indexOf("<");
						line = line.substring(0, iend - 1);
						line = line.trim();
						row[0] = line;
					}
					line = reader.readLine();
					for (int i = 1; i < 8; i++) {
						line = reader.readLine();
						if (line != null) {
							int iend = line.indexOf("</div>");
							int ibegin = line.indexOf("\"center\">");
							row[i] = line.substring(ibegin + 9, iend).trim();
						}
					}

					rows.add(row);
				}

				line = reader.readLine();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				line = reader.readLine();
			}
		}

		// TODO Auto-generated method stub

	}

	private int calcQuarter(int month) {
		// TODO Auto-generated method stub
		if ((month >= 1) && (month <= 3)) {
			return 1;
		} else if ((month >= 4) && (month <= 6)) {
			return 2;
		} else if ((month >= 7) && (month <= 9)) {
			return 3;
		} else if ((month >= 10) && (month <= 12)) {
			return 4;
		} else {
			return -1;
		}
	}

	public void execute() throws SQLException, ParseException, IOException {
		//
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		MySqlTrail mySQL2 = new MySqlTrail();
		b = mySQL2.init();
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo where NOT (StockID = '000001' and StockBelong = 'SH')  AND NOT (StockID = '399001' and StockBelong = 'SZ')";
		ResultSet rs = mySQL.QueryBySQL(sql);
		String mExDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strToday = sdf.format(new Date());
		List<String[]> rowRet;
		while (rs.next()) {
			System.out.println(rs.getString(1));
			sql = "Select MAX(ExDate) mExDate FROM t_stockadjhis_sina where StockID = '" + rs.getString("StockID")
					+ "' AND StockBelong = '" + rs.getString("StockBelong") + "'";
			ResultSet rs2 = mySQL2.QueryBySQL(sql);
			if (rs2.next()) {
				mExDate = rs2.getString("mExDate");
				if (mExDate == null) {
					mExDate = "2000-01-01";
				}
			} else {
				mExDate = "2000-01-01";
			}

			rowRet = genHisData(rs.getString("StockID"), rs.getString("StockBelong"), mExDate, strToday);
			insertIntoDB(rowRet);
		}

		mySQL.destroy();
		mySQL2.destroy();

	}

	private List<String[]> genHisData(String stockID, String StockBelong, String fromDate, String toDate)
			throws ParseException, IOException {
		List<String[]> returnList = new ArrayList<String[]>();
		String[] row;// 8
		String[] rowReturn;// 10

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateBegin = sdf.parse(fromDate);
		Date dateEnd = sdf.parse(toDate);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(dateEnd);
		calendar.add(Calendar.DAY_OF_YEAR, +1);
		dateEnd = calendar.getTime();
		Date dateNow;

		setStockID(stockID);

		try {
			rows = new ArrayList<String[]>();
			queryFromSina(fromDate, toDate);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			rows = new ArrayList<String[]>();
			queryFromSina(fromDate, toDate);
		}

		Iterator<String[]> itr = rows.iterator();
		while (itr.hasNext()) {
			row = (String[]) itr.next();
			dateNow = sdf.parse(row[0]);
			if ((dateNow.after(dateBegin)) && (dateNow.before(dateEnd))) {
				rowReturn = new String[10];
				rowReturn[0] = stockID;
				rowReturn[1] = StockBelong;

				for (int i = 0; i < 8; i++) {
					rowReturn[i + 2] = row[i];
				}
				returnList.add(rowReturn);
			}

		}

		return returnList;
	}

	private void insertIntoDB(List<String[]> rows) {
		Logger log = Logger.getLogger(FromSinaHisTradeData.class);
		System.out.println("insertIntoDB " + rows.size());
		Iterator<String[]> itr = rows.iterator();
		StringBuffer sb;
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		while (itr.hasNext()) {
			String[] strs = (String[]) itr.next();

			sb = new StringBuffer();
			sb.append(
					"INSERT INTO t_stockadjhis_sina (StockID,StockBelong,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity,ExAmount,AdjRate) VALUES ('");
			for (int i = 0; i < 9; i++) {
				sb.append(strs[i]);
				sb.append("','");
			}
			sb.append(strs[9]);
			sb.append("');");
			mySQL.executeSQL(sb.toString());
			//log.info(sb.toString());
		}

		mySQL.destroy();
	}

	public static void main(String[] args) throws IOException, ParseException {
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
