package org.tokenring.analysis.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class AnalzeByFile {
	Logger log = Logger.getLogger(AnalzeByFile.class);
	List<Stock> objs;

	public AnalzeByFile() {
		objs = new ArrayList<Stock>();
	}

	public List<Stock> loadFromFile() throws IOException {
		String s = "d:\\2\\log.txt.";
		for(int i = 7;i > -1 ;i --){
			String s1 = s + i;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(s1)));
			String data = null;
			while ((data = br.readLine()) != null) {
				parse(data);

			}
		}
		/*
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("d:\\2\\log.txt.1")));
		String data = null;
		while ((data = br.readLine()) != null) {
			parse(data);

		}
		br = new BufferedReader(new InputStreamReader(new FileInputStream("log.txt")));
		data = null;
		while ((data = br.readLine()) != null) {
			parse(data);

		}
		*/
		return objs;
	}

	private void parse(String data) {
		if (data.contains("StockID = ")) {
			// ÐÂµÄStock
			int iBegin = data.indexOf("= ") + ("= ").length();
			int iEnd = data.indexOf("]");
			String id = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			iBegin = data.indexOf("= ") + ("= ").length();
			iEnd = data.indexOf("]");
			String name = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			Stock stock = new Stock(id, name);
			objs.add(stock);
		}

		if (data.contains("exDates.size()= ")) {
			// ÐÂevent
			int iBegin = data.indexOf("[") + ("[").length();
			int iEnd = data.indexOf("]");
			String eventName = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			iBegin = data.indexOf("exDates.size()= ") + ("exDates.size()= ").length();
			iEnd = data.indexOf("]");
			String size = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			iBegin = data.indexOf("wins = ") + ("wins = ").length();
			iEnd = data.indexOf("]");
			String win = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			iBegin = data.indexOf("losts = ") + ("losts = ").length();
			iEnd = data.indexOf("]");
			String lost = data.substring(iBegin, iEnd);
			data = data.substring(iEnd + "]".length());

			Stock stock = objs.get(objs.size() - 1);
			stock.setSize(Integer.parseInt(size));
			stock.addEvent(eventName, Integer.parseInt(win), Integer.parseInt(lost));

		}
	}

	public void assertAll() {
		String assertID = "";
		String[] assertEvents = { "", "" };
		Stock s = null;
		Iterator<Stock> itr = objs.iterator();
		while (itr.hasNext()) {
			Stock temp = (Stock) itr.next();
			if (temp.id.equals("")) {
				s = temp;
				break;
			}
		}

		Iterator itrEvent = s.events.iterator();
		while (itrEvent.hasNext()) {
			Event e = (Event) itrEvent.next();
			String eventName = e.eventName;
			for (String s1 : assertEvents) {
				if (s1.equals(eventName)) {
					StringBuffer sb = new StringBuffer();
					sb.append(e.eventName);
					sb.append(",");
					sb.append(e.lost);
					sb.append(",");
					sb.append(e.win);
					sb.append(",");
					sb.append(e.stock.size);
					log.info(sb.toString());
				}

			}
		}

	}
	public void printAll(){
		Map map = new HashMap();
		Iterator itr = objs.iterator();
		while (itr.hasNext()){
			Stock s = (Stock)itr.next();
			if (s.id.equals("xxxx")){
				continue;
			}
			Iterator itrEvent = s.events.iterator();
			while (itrEvent.hasNext()){
				Event e = (Event)itrEvent.next();
				Counter c = (Counter)map.get(e.eventName);
				if (c == null){
					c = new Counter();
					
				}
				c.happened += e.lost + e.win;
				c.lost += e.lost;
				c.win += e.win;
				c.total += e.stock.size;
				map.put(e.eventName, c);
			}
		}
		
		Iterator <Map.Entry<String,Counter>> itrMap = map.entrySet().iterator();
		while (itrMap.hasNext()){
			Map.Entry<String,Counter> entry = itrMap.next();
			StringBuffer sb = new StringBuffer();
			sb.append(entry.getKey());
			sb.append(",");
			Counter c = entry.getValue();
			sb.append(c.total);
			sb.append(",");
			sb.append(c.happened);
			sb.append(",");
			sb.append(c.win);
			sb.append(",");
			sb.append(c.lost);
			
			log.info(sb.toString());
					
					
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnalzeByFile abf = new AnalzeByFile();
		try {
			abf.loadFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		abf.printAll();
		
	}

}
