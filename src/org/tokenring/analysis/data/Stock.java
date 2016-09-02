package org.tokenring.analysis.data;

import java.util.List;
import java.util.ArrayList;

public class Stock {
	String id;
	String name;
	List events;
	int size;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List getEvents() {
		return events;
	}

	public void setEvents(List events) {
		this.events = events;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Stock(String stockID, String stockName) {
		id = stockID;
		name = stockName;
		this.size = 0;
		events = new ArrayList();
	}

	public List addEvent(String name, int win, int lost) {
		Event e = new Event(this, name, win, lost);
		events.add(e);
		return events;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
