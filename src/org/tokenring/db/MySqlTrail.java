package org.tokenring.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlTrail {
	 boolean isInit = false;
	 Connection conn = null;

	public  boolean init() {
		if (isInit) {
			return isInit;
		}
		// 驱动程序名
		String driver = "com.mysql.jdbc.Driver";

		// URL指向要访问的数据库名StockInfoDB
		String url = "jdbc:mysql://tokenring.jios.org:3306/StockInfoDB?autoReconnect=true";
		//String url = "jdbc:mysql://192.168.199.90:3306/StockInfoDB?autoReconnect=true";

		// MySQL配置时的用户名
		String user = "stockinfo";

		// MySQL配置时的密码
		String password = "stock@info";

		try {
			// 加载驱动程序
			Class.forName(driver);

			// 连续数据库
			conn = DriverManager.getConnection(url, user, password);
			isInit = !conn.isClosed();
			return isInit;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public  boolean destroy() {
		if (conn != null) {
			try {
				conn.close();
				isInit = false;
				conn = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public  boolean executeSQL(String sql) {
		try {
			if (!conn.isClosed()) {
				// statement用来执行SQL语句
				Statement statement = conn.createStatement();

				// 执行业务语句
				return statement.execute(sql);
			} else {
				System.out.println("conn is closed.");
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public  ResultSet QueryBySQL(String sql) {
		try {
			if (!conn.isClosed()) {
				// statement用来执行SQL语句
				Statement statement = conn.createStatement();
				// 执行业务语句
				return statement.executeQuery(sql);
			} else {
				System.out.println("Conn is closed.");
				return null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	public static void main(String[] args) throws SQLException {
		MySqlTrail mst = new MySqlTrail();
		boolean b = mst.init();
		
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo where NOT (StockID = '000001' and StockBelong = 'SH')  AND NOT (StockID = '399001' and StockBelong = 'SZ')";
		ResultSet rs = mst.QueryBySQL(sql);
		
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
		mst.destroy();
	}
}
