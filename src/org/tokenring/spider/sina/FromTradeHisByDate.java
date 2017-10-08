package org.tokenring.spider.sina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FromTradeHisByDate {
	
	public String queryTradeHisByDate(String strSymbol,String strDate) {
		URL ur ;
		
		String line;
		String strTradeHis="";
		
		StringBuffer sb = new StringBuffer();
		sb.append("http://vip.stock.finance.sina.com.cn/quotes_service/view/vMS_tradehistory.php?symbol=");
		sb.append(strSymbol);//sh600570
		sb.append("&date=");
		sb.append(strDate);//2014-03-14
		
		try {
			ur = new URL(sb.toString());
			HttpURLConnection uc = (HttpURLConnection) ur.openConnection();
			//设置超时1分钟
			uc.setConnectTimeout(60000);
			//设置超时1分钟
			uc.setReadTimeout(60000);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "GBK"));
			
			while ((line = reader.readLine()) != null) {
				
				//格式如下：<tr><td>成交额(千元):</td><td>273402.63</td></tr>
				if (line.contains("成交额(千元):"))
				{
					String[] strArr = line.split("</td>");
					strTradeHis = strArr[1].substring(4);
					
					//System.out.println(strTradeHis);
				}
				
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strTradeHis;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FromTradeHisByDate fthb = new FromTradeHisByDate();
		System.out.println(fthb.queryTradeHisByDate("sz000895", "2017-03-06"));
		
	}

}
