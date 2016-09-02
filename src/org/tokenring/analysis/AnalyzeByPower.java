package org.tokenring.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.tokenring.db.MySqlTrail;

public class AnalyzeByPower {
	Logger log = Logger.getLogger(AnalyzeByPower.class);
	List<TempObj> objs;
	int icalcWinDay = 1;
	int icalcNday = 1;

	public void init() throws SQLException {
		objs = new ArrayList();

		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		String strSQL = "select StockID,ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity,AdjRate,ExAmount,WinPower from temp where ExDate>'2015-06-01' order by ExDate desc";
		ResultSet rs = mySQL.QueryBySQL(strSQL);

		while (rs.next()) {
			TempObj to = new TempObj(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4),
					rs.getDouble(5), rs.getDouble(6), rs.getDouble(7), rs.getDouble(8), rs.getDouble(9),
					rs.getDouble(10));
			objs.add(to);
		}
		rs.close();
	}

	public void calc() {
		int isize = objs.size();

		for (int i = 0; i < isize - icalcNday; i++) {
			TempObj to = objs.get(i);
			double tempwinpower = 0;
			double tempamount = 0;
			for (int j = 0; j < icalcNday; j++) {
				tempwinpower += objs.get(i + j).getWinPower();
				tempamount += objs.get(i + j).getExAmount();
			}
			double tempWinPowerScore = tempwinpower / tempamount * 100;
			log.debug("tempwinpower = " + tempwinpower);
			log.debug("tempamount = " + tempamount);

			to.setWinPowerScore(tempwinpower);
			log.debug("tempWinPowerScore = " + tempWinPowerScore);

			if ((i - this.icalcWinDay) >= 0) {
				boolean b = to.getEndPrice() < objs.get(i - this.icalcWinDay).getEndPrice();

				if (b) {
					to.setIsWin("Y");
				} else {
					to.setIsWin("N");
				}
			}
		}

	}

	public void update2db() {

		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();

		StringBuffer sb;
		// strSQL = "update temp set WinPowerScore = ";
		// ResultSet rs = mySQL.QueryBySQL(strSQL);
		int isize = objs.size();

		for (int i = 0; i < isize - icalcNday ; i++) {

			TempObj to = objs.get(i);

			sb = new StringBuffer();
			sb.append("update temp set WinPowerScore = ");
			sb.append(to.getWinPowerScore());
			sb.append(" ,isWin='");
			sb.append(to.getIsWin());
			sb.append("' where ExDate = '");
			sb.append(to.getExDate());
			sb.append("'");

			mySQL.executeSQL(sb.toString());
		}

		mySQL.destroy();
	}

	public void print() {
		int iYwin = 0;
		int iYlost=0;
		int iNwin=0;
		int iNlost=0;

		for (TempObj to : objs) {
			if ("Y".equals(to.getIsWin())) {
				if (to.getWinPowerScore() > 0) {
					iYwin++; // 248
				} else {
					iYlost++;
				}
			} else if ("N".equals(to.getIsWin())) {
				if (to.getWinPowerScore() > 0) {
					iNwin++;
				} else {
					iNlost++;
				}
			}
		}

		System.out.println("iYwin = " + iYwin);
		System.out.println("iYlost = " + iYlost);
		System.out.println("iNwin = " + iNwin);
		System.out.println("iNlost = " + iNlost);
		System.out.println("Rate = " + ((double) (iYlost+iNwin) * 100 / (iYwin + iYlost + iNwin + iNlost) + "%"));

	}

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		AnalyzeByPower abp = new AnalyzeByPower();
		abp.init();
		abp.calc();
		abp.update2db();
		abp.print();
	}

}
