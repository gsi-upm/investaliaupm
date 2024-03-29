package es.upm.gsi.marketSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IntelligentInvestor extends InvestorType {
	boolean impulsive;
	boolean perception;
	boolean anxiety;
	boolean memory;
	boolean isDiversifier;
	
	boolean isTruster;
	boolean habits;
	boolean isEspeculator;
	boolean isOptimism;
		
	//Map<String, Double> memoryPonderation;
		
	//Variables for debug
	double totalIncrementAverage = 0;
	double totalAverage = 0;
	
	public IntelligentInvestor(Investors investor) {
		iterationsToBuy = 4;
        //iteracionesVenta = 5;
        initialCapital = Properties.INITIAL_LIQUIDITY;
        liquidity = initialCapital; //setLiquidez(randomInRange(3000,10000));
        maxBuyValue = Properties.MAX_BUY_VALUE;
        buyProbability = Properties.BUY_PROBABILITY;
        sellProbability = Properties.SELL_PROBABILITY;
        //if one share along the movements decrease his value in 15% you might buy
        this.investor = investor;
        if(investor.randomInRange(0.0, 1.0) < Properties.PERCEPTION_PROBABILITY) {
        	this.perception = true;
        	debugParam = investor.getId();
        } else
        	this.perception = false;
        if(investor.randomInRange(0.0, 1.0) < Properties.IMPULSIVE_PROBABILITY)
        	this.impulsive = true;
        else
        	this.impulsive = false;
        if(investor.randomInRange(0.0, 1.0) < Properties.ANXIETY_PROBABILITY)
        	this.anxiety = true;
        else
        	this.anxiety = false;
        if(investor.randomInRange(0.0, 1.0) < Properties.MEMORY_PROBABILITY)
        	this.memory = true;
        else
        	this.memory = false;
        if(investor.randomInRange(0.0, 1.0) < Properties.DIVERSIFICATION_PROBABILITY)
        	this.isDiversifier = true;
        else
        	this.isDiversifier = false;
        configureIntelligent();
        //configureIntelligent (investor.randomIs(), investor.randomIs(), 
        //		investor.randomIs(),investor.randomIs(), investor.randomIs());
	}
	
	private void configureIntelligent () {
		if(!perception) {
			//this.actividadComprar /= Properties.PERCEPTION_DEGRADATION;
			this.sellProbability /= Properties.PERCEPTION_DEGRADATION;
		}
		int financialHistory = 0;
		for(Share share: ((SimulateSocialExchange)investor.getRoot()).getStock().getShares().values() ) {
			if(share instanceof HistoryFileShare)
				financialHistory++;
		}
		if(financialHistory > ((SimulateSocialExchange)investor.getRoot()).getStock().getShares().size()/2 ) {
			sellTable = Properties.sellHistoryFileTable;			
		} else {
			int i = 0;
			for(i = 0; i < Properties.chooseTableByStockVariation.length; i++) {
				if(Properties.STOCK_VARIATION >= Properties.chooseTableByStockVariation[i])
					break;
			}
			if(i == Properties.chooseTableByStockVariation.length)
				i--;
			sellTable = Properties.sellPrudentTable[i];
		}
		//System.out.println("Selltable:"+sellTable+" ?= "+Properties.sellHistoryFileTable);
		if(anxiety) {
			sellTable = Properties.anxietySellTable;
			sellAll = Properties.anxietySellAll;
		} else {
			//sellTable = Properties.sellTable;
			sellAll = Properties.sellAll;
		}
		if(memory) {
			rentabilityToBuy = -investor.randomInRange(Properties.BUY_PROFITABILITY[0],Properties.BUY_PROFITABILITY[1]);	        
		} else {
			rentabilityToBuy = investor.randomInRange(Properties.BUY_PROFITABILITY[0],Properties.BUY_PROFITABILITY[1]);
		}
		//	memoryPonderation = new HashMap<String, Double>();
		if(impulsive)
			maxBuyValue *= Properties.IMPULSIVE_INCREMENTATION;
	}
	
	private void configureIntelligent (boolean perception, boolean anxiety, boolean memory,
			boolean impulsive, boolean isDiversifier) {
		this.perception = perception;
		this.anxiety = anxiety;
		this.memory = memory;
		this.impulsive = impulsive;
		this.isDiversifier = isDiversifier;
		if(!perception) {
			this.buyProbability /= Properties.PERCEPTION_DEGRADATION;
			this.sellProbability /= Properties.PERCEPTION_DEGRADATION;
		}
		int financialHistory = 0;
		for(Share share: ((SimulateSocialExchange)investor.getRoot()).getStock().getShares().values() ) {
			if(share instanceof HistoryFileShare)
				financialHistory++;
		}
		if(financialHistory > ((SimulateSocialExchange)investor.getRoot()).getStock().getShares().size()/2 ) {
			sellTable = Properties.sellHistoryFileTable;
		} else {
			int i = 0;
			for(i = 0; i < Properties.chooseTableByStockVariation.length; i++) {
				if(Properties.STOCK_VARIATION >= Properties.chooseTableByStockVariation[i])
					break;
			}
			if(i == Properties.chooseTableByStockVariation.length)
				i--;
			sellTable = Properties.sellPrudentTable[i];
		}
		System.out.println("Selltable:"+sellTable);
		if(anxiety) {			
			sellTable = Properties.anxietySellTable;
			sellAll = Properties.anxietySellAll;
		} else {
			//sellTable = Properties.sellTable;
			sellAll = Properties.sellAll;
		}
		//if(memory)
		//	memoryPonderation = new HashMap<String, Double>();
		if(impulsive)
			maxBuyValue *= Properties.IMPULSIVE_INCREMENTATION;
	}
	
	public void playInStock(Ibex35 stock){
		HashMap<String, Share> shares = stock.getShares();
		if (investor.randomInRange(0.0,1.0) < sellProbability){
			//elegir empresa antes?, ver la mejor*probabilidad de vender??			
			//For each share of exchange, I have look for my shares checking up on which I have it
			//and checking up on the profitability. If it is good, sell the share.
			//TODO: conviene quitar getActualCapital()??
			Double capital = getActualCapital(stock);
			for(int id = 0; id < myPortfolio.size(); id++) {
				Investment myInversion = myPortfolio.get(id);
				Share share = shares.get(myInversion.getIdCompany());
				int inversionClusterTime = (investor.getTime() - myInversion.getDate()) / Properties.TIME_CLUSTER;							
				int sharesToSell = (int) (myInversion.getInitialQuantity() * 
					getPercentToSell(share.getValue(),myInversion.getBuyValue(),inversionClusterTime,capital)/100);
				if (sharesToSell > 0){
					sells++;
					if(sharesToSell > myInversion.getQuantity())
						sharesToSell = myInversion.getQuantity();
					myInversion.setQuantity(myInversion.getQuantity() - sharesToSell);
					double stockLiquidity = sharesToSell * share.getValue();
					liquidity +=  stockLiquidity;
					
					//double inversionReturn = (share.getValor() - myInversion.getValorCompra()) 
					//	/ myInversion.getValorCompra();
					if((share.getValue() - myInversion.getBuyValue()) < 0)
						capitalWithNegativeReturn += stockLiquidity;
					
					addOperationClosed(myInversion, sharesToSell, stockLiquidity, investor.getTime());
					
					//investor.updateFinancialReputation(ibex35);
					//System.out.println("("+ time +") id :" + getId() + ", vendo ("+  number2sell + " de " + 
					//	accionesAntesVenta + "): " + accionesBolsa.getNombre() + ", total ingreso: " + stockLiquidity);						
					if (myInversion.getQuantity() <= 0) {
						myPortfolio.remove(myInversion);
						id--;							
					}							
				}
				
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+" Perc:"+perception+" anx:"+anxiety+" invClus:"+
				//		inversionClusterTime+" imp:"+impulsive+" s2s:"+sharesToSell+" val:"+share.getValor()+
				//		" liq:"+liquidez+" num:"+investor.misAcciones.size()+" rent:"+rentabilityToBuy);				
			}			
		}
		
		//Buy
		if (liquidity > 0 && investor.randomInRange(0.0,1.0) < buyProbability){
			Map<String,Double> capitalByStockCategory = null;			
			for (Share share : shares.values()) {
				ArrayList<Double> history = share.getVariationsHistory();
				double suma = 0;					
				for ( int i = iterationsToBuy-1; i<history.size(); i++) {
					suma += history.get(i);
				}				
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+" nombre:"+accionesBolsa.getNombre()+
				//		" rentabilidad a comprar:"+suma);
				if(history.size() > 1) {
					if(memory && (history.get(history.size()-1) + history.get(history.size()-2)) < 0)
						continue;
					if(!memory && (history.get(history.size()-1) + history.get(history.size()-2)) > 0)
						continue;
				} else
					break;				
				if(isDiversifier && capitalByStockCategory == null) {
					capitalByStockCategory = getBuyInvestmentByCategory();					
				}				
				int limite1 = (int)Math.floor(maxBuyValue / share.getValue());
				int limite2 = (int)(liquidity / share.getValue());
				int number2buy = 0;
				if (limite1 > 0 && limite2 > 0){										
					number2buy =  ((int)investor.randomInRange(1, limite1));
					if(isDiversifier) {
						Double incrementByCategory = 
								getIncrementByStockCategory(number2buy,share,capitalByStockCategory);
						number2buy *= incrementByCategory;
						if(!impulsive && number2buy > limite1)
							number2buy = limite1;
						/*totalIncrementAverage += forDebug;
						totalAverage++;
						if(forDebug > 2)
							System.out.println(investor.getId()+": "+share.getName()+forDebug + 
									" > 2  ("+totalIncrementAverage/totalAverage);
						else if(forDebug < 0.5)
							System.out.println(investor.getId()+": "+share.getName()+forDebug + 
									" < 0.5  ("+totalIncrementAverage/totalAverage);*/
					}
					//TODO: sirve para algo esta impuslividad????
					if(impulsive) {
						if(suma/rentabilityToBuy > 1)
							number2buy = (int) Math.abs(number2buy * suma/rentabilityToBuy);						
					}
					if(number2buy > limite2) {
						number2buy = limite2;
						withoutLiquidity++;
					}
					buys++;
					liquidity -=  number2buy*share.getValue();
					investCapital += number2buy*share.getValue();
					myPortfolio.add(new Investment(number2buy, share.getValue(),share.getName(),
							share.getCategory(), investor.getTime()));

					//System.out.println("("+ time +") id :" + getId() + ", compro(" + number2buy + ") 
					//de " + accionesBolsa.getNombre()+ ",tot: " + number2buy*accionesBolsa.getValor());					
				} else
					withZero++;
			}
			//Estimates the capital I have.
			//investor.setCapital(miBolsa, liquidez);
			this.maxBuyValue = Math.max(Properties.MAX_BUY_VALUE, liquidity
					* Properties.MAX_BUY_VALUE_BY_LIQUIDITY);
		}		
	}
	
	public Double getIncrementByStockCategory (int number2buy, Share share, 
			Map<String,Double> capitalByStockCategory) {
		if( capitalByStockCategory.size() == 0 || 
				(capitalByStockCategory.size() == 1 && capitalByStockCategory.containsKey(share.getCategory())) )
			return 1.0;
		Double total = 0.0;
		for(Double categoryInversion : capitalByStockCategory.values())
			total += categoryInversion;
		//New Form
		//total += number2buy * share.getValue();
		Double categoryCapital = capitalByStockCategory.get(share.getCategory());
		if(categoryCapital == null) {
			total /= (capitalByStockCategory.size() * number2buy * share.getValue());
			//total /= ((capitalByStockCategory.size()+1) * (number2buy * share.getValue()));
		} else {
			total += number2buy * share.getValue();
			total /= capitalByStockCategory.size() * (number2buy * share.getValue() + categoryCapital);
		}
		total = Math.min(Properties.MAX_INCREMENT_DIVERSIFIER, total);
		return Math.max(1/Properties.MAX_INCREMENT_DIVERSIFIER, total);		
	}	
	
	public Map<String, Double> getBuyInvestmentByCategory () {
		Map<String, Double> buyInvestmentByCategory = new HashMap<String, Double> ();
		for (Investment investment : myPortfolio) {
			//Now, invesment capital is from buy and not actual value
			//if(investment.getIdCompany().equalsIgnoreCase(share.getName())) {
				Double value;
				if( (value = buyInvestmentByCategory.get(investment.getCategory())) != null) {
					buyInvestmentByCategory.put(investment.getCategory(), 
							value + investment.getBuyValue()*investment.getQuantity());
				} else {
					buyInvestmentByCategory.put(investment.getCategory(), 
							investment.getBuyValue()*investment.getQuantity());
				}
			//}
		}
		return buyInvestmentByCategory;
	}			
	
	public double actualInversionOnShare (Share share) {
		double actualInversion = 0;
		for(Investment inversion : myPortfolio) {
			if(inversion.getIdCompany().equalsIgnoreCase(share.getName()))
				actualInversion += inversion.getQuantity() * share.getValue(); //inversion.getValorCompra()	
		}
		return actualInversion;
	}
	
	private double getPercentToSell (double actualValue, double firstValue, 
			int clusterTime, double actualCapital) {
		int value = 0;
		double inversionReturn = (actualValue - firstValue) / firstValue;
		//if(if(liquidity < (Properties.MAX_BUY_VALUE*4)){
		if(liquidity < actualCapital * Properties.CAPITAL_DECREMENT_TO_SELL_ALL) {
			if(clusterTime > sellAll[1]) {
				sellsAll1++;
				return 100;
			}
			if(clusterTime > sellAll[0] && inversionReturn > 0) {			
				//System.out.println("SA0:"+sellAll[0]+", cT:"+clusterTime+" ,invR:"+inversionReturn);
				sellsAll0++;
				return 100;
			}
		}
		for(int i = 0; i < sellTable.length; i++) {
			value = i;
			if(inversionReturn <= sellTable[i][0])
				break;			
		}
		if(value == 0)
			return sellTable[0][1];
		else {
			return sellTable[value-1][1] + 
				(inversionReturn - sellTable[value-1][0])/(sellTable[value][0] - sellTable[value-1][0])
				* (sellTable[value][1] - sellTable[value-1][1]);
		}
	}
	
	public String getAgentTypeToString () {
		String agentType = "[";
		if(memory)
			agentType += "ALC,";
		else
			agentType += "BAJ,";
		if(impulsive)
			agentType += "IMP,";
		if(perception)
			agentType += "PER,";
		if(anxiety)
			agentType += "ANX,";		
		if(isDiversifier)
			agentType += "DIV,";
		return agentType + String.format("%.2f",rentabilityToBuy) + "]";
	}
	
}
