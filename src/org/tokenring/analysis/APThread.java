package org.tokenring.analysis;

import java.util.concurrent.Semaphore;

public class APThread extends Thread {
	AnalysisParser ap;
	Semaphore  semp;
	
	public AnalysisParser getAp() {
		return ap;
	}
	public void setAp(AnalysisParser ap) {
		this.ap = ap;
	}
	public Semaphore getSemp() {
		return semp;
	}
	public void setSemp(Semaphore semp) {
		this.semp = semp;
	}
	public void run(){
	
		    try {  
		        // 业务逻辑  
		    	execute();
		    } 
		    finally {  
		        // 释放许可  
		        semp.release();  
		    }  
	
	}
	private void execute(){
		// AnalyzeHistory ah = new AnalyzeAverageAmount(60, 5, true);
		AnalyzeHistory ah;
		ah = new AnalyzeMACD(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeBTest(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeMyMACD(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeSigmaPrice(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeSigmaQuantity(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeSigmaSituation(ap.getStockHistory());
		ap.addParser(ah);
		
		ah = new AnalyzeKDJ(ap.getStockHistory());
		ap.addParser(ah);
		
		/*
		for (int i = 10; i <= 60; i++) {
			for (int j = 3; j < 15; j++) {
				ah = new AnalyzeAverageAmount(i, j, true, ap.getStockHistory());
				ap.addParser(ah);
				ah = new AnalyzeAverageAmount(i, j, false, ap.getStockHistory());
				ap.addParser(ah);

				ah = new AnalyzeAveragePrice(i, j, true, ap.getStockHistory());
				ap.addParser(ah);
				ah = new AnalyzeAveragePrice(i, j, false, ap.getStockHistory());
				ap.addParser(ah);

			}
		}
		*/

		ap.prepareParser();
		ap.prepareAssertForcast();
		ap.doAnalyze();
		ap.doAssert();
		double rate = ap.printAnalyze("KDJ金叉");
		ap.setRate(rate);
		
		ap.saveEventToDB();
	}

}
