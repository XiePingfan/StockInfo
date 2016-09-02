package org.tokenring.analysis;

public class AssertEvent {
	ExDate exDate;
	String assertMsg;
	AssertEvent(ExDate exDate,String AssertMsg){
		this.exDate = exDate;
		assertMsg = AssertMsg;
	}
	public ExDate getEvent() {
		return exDate;
	}
	public void setEvent(ExDate exDate) {
		this.exDate = exDate;
	}
	public String getAssertMsg() {
		return assertMsg;
	}
	public void setAssertMsg(String assertMsg) {
		this.assertMsg = assertMsg;
	}
	
}
