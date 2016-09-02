package org.tokenring.analysis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DecisionTreeNode {
	DecisionTreeNode root;//根节点 root 为 null
	DecisionTreeNode left;
	DecisionTreeNode right;
	DecisionTree decisionTree;
	double rate;
	Set<String> decisionRule;
	List<ExDate> exDates;
	String splitMsg = "";
	double winRate;
	int level;
	
	private boolean eventMatch(List<Event> Events,String EventMsg){
		boolean result = false;
		Iterator<Event> iterEvent = Events.iterator();
		Event e;
		while (iterEvent.hasNext() &&!result) {
			e = iterEvent.next();
			result = (e.getEventMsg().equals(EventMsg));
		}
		return result;
	}
	public DecisionTreeNode(DecisionTree dt,DecisionTreeNode Root,List<ExDate> ExDates,Set<String> DecisionRule){
		decisionTree = dt;
		root = Root;
		if (Root == null){
			level = 1;
		}else{
			level = Root.level + 1;
		}
			
		
		left = null;
		right = null;
		exDates = new ArrayList<ExDate>();

		exDates.addAll(ExDates);
		decisionRule = new HashSet<String>();
		decisionRule.addAll(DecisionRule);
		rate = -1;
	}
	
	public boolean calcTreeBalance(){
		//计算rate
		ExDate e;
		int assertNote = 0;
		Iterator<ExDate> iterExDate = exDates.iterator();
		while (iterExDate.hasNext()) {
			e = iterExDate.next();
			if ((e.assertEvents != null) && (e.assertEvents.size() > 0)){
				assertNote ++;
			}
		}
		rate = Math.pow((double)assertNote / (double)this.exDates.size(),2) + Math.pow((double)(this.exDates.size()-assertNote) / (double)this.exDates.size(),2);
		winRate = (double)assertNote / (double)this.exDates.size();
		
		double assertLeft;
		double leftCount;
		double assertRight;
		double rightCount;
		
		String splitMsg;
		
		double newRate = -1;
		String newSplitMsg = "";
		
		Iterator<String> itrSplitMsg = decisionRule.iterator();
		while (itrSplitMsg.hasNext()) {
			assertLeft = 0;
			leftCount = 0;
			assertRight = 0;
			rightCount = 0;
			
			splitMsg = itrSplitMsg.next();
			iterExDate = exDates.iterator();
			while (iterExDate.hasNext()) {
				e = iterExDate.next();
				if ((e.events != null) && (eventMatch(e.events,splitMsg))){
					rightCount ++;
					if ((e.assertEvents != null) && e.assertEvents.size() > 0){
						assertRight ++;
					}
				}else{
					leftCount ++;
					if ((e.assertEvents != null) && e.assertEvents.size() > 0){
						assertLeft ++;
					}
				}
			}
			double tempRate;
			double tempLeftRate;
			double tempRightRate;
			if (( leftCount == 0 ) || (rightCount == 0)){
				tempRate = this.rate;
			}else{
				tempLeftRate = (leftCount / this.exDates.size()) * (Math.pow(( assertLeft / leftCount), 2) + Math.pow((leftCount - assertLeft) / leftCount, 2));
				tempRightRate = (rightCount / this.exDates.size()) * (Math.pow(( assertRight / rightCount), 2) + Math.pow((rightCount - assertRight) / rightCount, 2));
				tempRate = tempLeftRate + tempRightRate ;
			}
			if ((tempRate > this.rate) && (tempRate > newRate )){
				newRate = tempRate;
				newSplitMsg = splitMsg;
			}
		}
		
		if (newRate > this.rate){
			//找到更好的分类规则
			this.splitMsg = newSplitMsg;
			List<ExDate> exLeftDates,exRightDates;
			exLeftDates = new ArrayList<ExDate>();
			exRightDates = new ArrayList<ExDate>();
			iterExDate = exDates.iterator();
			while (iterExDate.hasNext()) {
				e = iterExDate.next();
				if ((e.events != null) && (eventMatch(e.events,newSplitMsg))){
					exRightDates.add(e);
				}
				else{
					exLeftDates.add(e);
				}
			}
			Set<String> newDecisionRule = new HashSet<String>();
			newDecisionRule.addAll(this.decisionRule);
			newDecisionRule.remove(newSplitMsg);
			
			left = new DecisionTreeNode(this.decisionTree,this,exLeftDates,newDecisionRule);
			right = new DecisionTreeNode(this.decisionTree,this,exRightDates,newDecisionRule);
			left.calcTreeBalance();
			right.calcTreeBalance();
			
			return true;
		}else{
			return false;
		}
	}
	
	public void printNode(){
		DecimalFormat    df   = new DecimalFormat("######0.00");   
		int preSpace = (level - 1) > 0 ? (level - 2) * 8 + 4 : 0;
		StringBuffer sb = new StringBuffer();
		for (int i = 0;i < preSpace ; i++ ){
			sb.append(' ');
		}
		
		if (level > 1) {
			sb.append('|');
			sb.append('-');
			sb.append('L');
			sb.append(level);
			sb.append('-');
		}
		sb.append("(");
		sb.append(this.exDates.size());
		sb.append(")<");
		sb.append(splitMsg);
		sb.append(">[");
		sb.append(df.format(winRate*100));
		sb.append("%]");
		System.out.println(sb.toString());
		if (this.left != null){
			left.printNode();
		}
		if (this.right != null){
			right.printNode();
		}
	}

}
