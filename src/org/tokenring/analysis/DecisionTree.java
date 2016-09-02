package org.tokenring.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DecisionTree {
	DecisionTreeNode root;
	List<ExDate> exDates;
	public DecisionTree(List<ExDate> ExDates){
		exDates = ExDates;
		
		Set<String> hs = new HashSet<String>();
		Iterator<ExDate> iterExDate = exDates.iterator();
		ExDate e;
		Event ev;
		while (iterExDate.hasNext()) {
			e = iterExDate.next();
			if (e.events != null){
				Iterator<Event> iterEvent = e.events.iterator();
				while (iterEvent.hasNext()) {
					ev = iterEvent.next();
					hs.add(ev.eventMsg);
					
				}
				
			}
		}
		System.out.println("hs.size = " + hs.size());
		root = new DecisionTreeNode(this,null,exDates,hs);
		root.calcTreeBalance();
	}
	
	public void printTree(){
		root.printNode();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
