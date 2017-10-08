package org.tokenring.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExDate {
	String stockID;
	String stockBelong;
	String stockName;
	String exDate;
	int idx;
	boolean isWin;
	List<Event> events;
	List<AssertEvent> assertEvents;
	
	public boolean hasEvent(String eventName){
		Iterator<Event> itr_event = events.iterator();
		Event e;
		while (itr_event.hasNext()){
			e = (Event)itr_event.next();
			if (e.getEventMsg().equals(eventName)){
				return true;
			}
		}
		return false;
	}
	public ExDate(String stockID,String stockBelong,String stockName,String exDate,int idx){
		this.stockID = stockID;
		this.stockBelong = stockBelong;
		this.stockName = stockName;
		this.exDate = exDate;
		this.idx = idx;
		
		events = new ArrayList<Event> ();
		assertEvents = new ArrayList<AssertEvent>();
		
		isWin=false;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

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

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getExDate() {
		return exDate;
	}

	public void setExDate(String exDate) {
		this.exDate = exDate;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public List<AssertEvent> getAssertEvents() {
		return assertEvents;
	}

	public void setAssertEvents(List<AssertEvent> assertEvents) {
		this.assertEvents = assertEvents;
	}
	
	
}
