package org.tokenring.spider.sina;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.tokenring.db.MySqlTrail;

public class UpdateExAmountFromSina {
	public void doUpdate() {
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		MySqlTrail mySQL2 = new MySqlTrail();
		mySQL2.init();
		

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

		FromTradeHisByDate fthbd = new FromTradeHisByDate();
		String strHisTrade;

		int i = 1;
		try {
			String sql = "Select StockID,StockBelong,ExDate FROM t_stockhis_sina where ExAmount is null ";
			ResultSet rs = mySQL.QueryBySQL(sql);
			
			while (rs.next()) {
				if (i % 100 == 0) {
					strToday = sdf.format(new Date());
					System.out.println(strToday + " now:" + Integer.toString(i) + " of " );
				}
				stockID = rs.getString("StockID");
				stockBelong = rs.getString("StockBelong");
				mExDate = rs.getString("ExDate");
				mExDate2 = mExDate.substring(0, 4) + "-" + mExDate.substring(4, 6) + "-" + mExDate.substring(6, 8);

				//System.out.println(stockID + ":" + stockBelong + ":" + mExDate2);
				strHisTrade = fthbd.queryTradeHisByDate(stockBelong.toLowerCase() + stockID, mExDate2);
				//System.out.println(strHisTrade);
				if (strHisTrade == ""){
					System.out.println("call queryTradeHisByDate error.+[" + stockID + "][" + mExDate2 + "]" );
					
					continue;
				}

				sb = new StringBuffer();
				sb.append("Update t_stockhis_sina set ExAmount = ");
				sb.append(strHisTrade);
				sb.append(" where StockID = '");
				sb.append(stockID);
				sb.append("' and StockBelong = '");
				sb.append(stockBelong);
				sb.append("' and ExDate = '");
				sb.append(mExDate);
				sb.append("'");
				//System.out.println(sb.toString());
				if (!mySQL2.executeSQL(sb.toString())){
					mySQL2.destroy();
					mySQL2 = new MySqlTrail();
					mySQL2.init();
					mySQL2.executeSQL(sb.toString());
				}
				i++;
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		strToday = sdf.format(new Date());
		System.out.println("End:" + strToday);
		
		mySQL.destroy();
		mySQL2.destroy();
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String mExDate = "20140205";
		String mExDate2 = mExDate.substring(0, 4) + "-" + mExDate.substring(4, 6) + "-" + mExDate.substring(6, 8);
		System.out.println(mExDate2);*/
		
		UpdateExAmountFromSina uefs = new UpdateExAmountFromSina();
		uefs.doUpdate();
	}

}
