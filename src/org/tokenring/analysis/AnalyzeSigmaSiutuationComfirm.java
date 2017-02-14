package org.tokenring.analysis;

public class AnalyzeSigmaSiutuationComfirm implements AnalyzeHistory {
	StockHistory stockHistory;

	public AnalyzeSigmaSiutuationComfirm(StockHistory StockHistory){
		stockHistory = StockHistory;
		stockHistory.calcAllSigma();
	}

	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		
		return null;
	}

}
