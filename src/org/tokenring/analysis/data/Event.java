package org.tokenring.analysis.data;

public class Event {
	Stock stock;
	String eventName;
	int win;
	int lost;
	public Event(Stock parent,String eventName,int win,int lost){
		stock = parent;
		this.eventName = eventName;
		this.win = win;
		this.lost = lost;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
