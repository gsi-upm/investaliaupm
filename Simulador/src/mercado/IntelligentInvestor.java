package mercado;

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
		
	Map<String, Double> memoryPonderation;
	
	double sharePercentInSell = 0;
	
	public IntelligentInvestor(Inversores investor) {
		iteraccionesCompra = 4;
        iteracionesVenta = 5;
        initialCapital = Properties.INITIAL_LIQUIDITY;
        liquidity = initialCapital; //setLiquidez(randomInRange(3000,10000));
        maxValorCompra = Properties.MAX_BUY_VALUE;
        buyProbability = Properties.BUY_PROBABILITY;
        sellProbability = Properties.SELL_PROBABILITY;
        //if one share along the movements decrease his value in 15% you might buy
        rentabilidadCompra = investor.randomInRange(Properties.BUY_PROFITABILITY[0],Properties.BUY_PROFITABILITY[1]);
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
        
		System.out.println("id:"+investor.getId()+" - Intelligent Agent configured");
		
		
			
	}
	
	private void configureIntelligent () {
		if(!perception) {
			//this.actividadComprar /= Properties.PERCEPTION_DEGRADATION;
			this.sellProbability /= Properties.PERCEPTION_DEGRADATION;
		}
		if(anxiety) {
			sellTable = Properties.anxietySellTable;
			sellAll = Properties.anxietySellAll;
		} else {
			sellTable = Properties.sellTable;
			sellAll = Properties.sellAll;
		}
		if(memory)
			memoryPonderation = new HashMap<String, Double>();
		if(impulsive)
			maxValorCompra *= Properties.IMPULSIVE_INCREMENTATION;
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
		if(anxiety) {
			sellTable = Properties.anxietySellTable;
			sellAll = Properties.anxietySellAll;
		} else {
			sellTable = Properties.sellTable;
			sellAll = Properties.sellAll;
		}
		if(memory)
			memoryPonderation = new HashMap<String, Double>();
		if(impulsive)
			maxValorCompra *= Properties.IMPULSIVE_INCREMENTATION;
	}
	
	public void jugarEnBolsa(Ibex35 stock){
		HashMap<String, Share> shares = stock.getAcciones();
		if (investor.randomInRange(0.0,1.0) < sellProbability){
			//elegir empresa antes?, ver la mejor*probabilidad de vender??			
			//For each share of exchange, I have look for my shares checking up on which I have it
			//and checking up on the profitability. If it is good, sell the share.			
			for(int id = 0; id < misAcciones.size(); id++) {
				Accion myInversion = misAcciones.get(id);
				Share share = shares.get(myInversion.getIdCompany());
				int inversionClusterTime = (investor.getTime() - myInversion.getDate()) / Properties.TIME_CLUSTER;
				/* I have the share, I have to check if is a good moment to
				 *  sell the share. look the movements. 
				 *  If the sum of last movements is better than the profitability sell it.				
				double rentabilidad = 0;
				ArrayList<Double> historico = accionesBolsa.getHistoricoAccion();
				for ( int i = iteracionesVenta-1; i<historico.size(); i++) {
					rentabilidad += historico.get(i);
				}*/				
				int sharesToSell = (int) (myInversion.getInitialQuantity() * 
					getPercentToSell(share.getValue(),myInversion.getValorCompra(),inversionClusterTime)/100);
				if (sharesToSell > 0){
					sells++;
					if(sharesToSell > myInversion.getCantidad())
						sharesToSell = myInversion.getCantidad();
					myInversion.setCantidad(myInversion.getCantidad() - sharesToSell);
					double stockLiquidity = sharesToSell * share.getValue();
					liquidity +=  stockLiquidity;
					
					//double inversionReturn = (share.getValor() - myInversion.getValorCompra()) 
					//	/ myInversion.getValorCompra();
					if((share.getValue() - myInversion.getValorCompra()) < 0)
						capitalWithNegativeReturn += stockLiquidity;
					
					addOperationClosed(myInversion, sharesToSell, stockLiquidity, investor.getTime());
					
					//investor.updateFinancialReputation(ibex35);
					//System.out.println("("+ time +") id :" + getId() + ", vendo ("+  number2sell + " de " + 
					//	accionesAntesVenta + "): " + accionesBolsa.getNombre() + ", total ingreso: " + stockLiquidity);						
					if (myInversion.getCantidad() <= 0) {
						misAcciones.remove(myInversion);
						id--;							
					}							
				}
				
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+" Perc:"+perception+" anx:"+anxiety+" invClus:"+
				//		inversionClusterTime+" imp:"+impulsive+" s2s:"+sharesToSell+" val:"+share.getValor()+
				//		" liq:"+liquidez+" num:"+investor.misAcciones.size()+" rent:"+rentabilidadCompra);				
			}			
		}
		
		//Buy
		if (liquidity > 0 && investor.randomInRange(0.0,1.0) < buyProbability){
			//para cada accion de la bolsa, tendre que ver si me interesa comprar
			// si compro le tengo que construir un objeto accion y meterlo en todas las acciones
			// de la bolsa
			for (Share share : shares.values()) {
				ArrayList<Double> historico = share.getVariationsHistory();
				double suma = 0;					
				for ( int i = iteraccionesCompra-1; i<historico.size(); i++) {
					suma += historico.get(i);
				}				
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+" nombre:"+accionesBolsa.getNombre()+
				//		" rentabilidad a comprar:"+suma);
				if (historico.size() > 1 && (historico.get(historico.size()-1) + historico.get(historico.size()-2)) < 0) { 
					//suma <= rentabilidadCompra) {
					if(isDiversifier) {
						double actualInversion = actualInversionOnShare(share);
						double buyProbability = Properties.MAX_BUY_VALUE/(actualInversion*2);
						if(impulsive)
							buyProbability *= suma/rentabilidadCompra;
						if(actualInversion > 0 && investor.randomInRange(0.0,1.0) > buyProbability)
							continue;
					}					
					int limite1 = (int)Math.floor(maxValorCompra / share.getValue());
					int limite2 = (int)(liquidity / share.getValue());
					int number2buy = 0;
					if (limite1 > 0 && limite2 > 0){										
						number2buy =  ((int)investor.randomInRange(1, limite1));
						if(impulsive) {
							number2buy = (int) Math.abs(number2buy * suma/rentabilidadCompra);
							if(number2buy <= 0)
								continue;
						}
						if(number2buy > limite2)
							number2buy = limite2;
						buys++;
						Accion accionComprada = new Accion(number2buy, share.getValue(),
								share.getName(), investor.getTime());
						liquidity -=  number2buy*share.getValue();
						investCapital += number2buy*share.getValue();
						misAcciones.add(accionComprada);

						//System.out.println("("+ time +") id :" + getId() + ", compro(" + number2buy + ") de " + accionesBolsa.getNombre() 
						//		+ ", total gasto: " + number2buy*accionesBolsa.getValor());					
					}
				}
			}
			//Estimates the capital I have.
			//investor.setCapital(miBolsa, liquidez);
			this.maxValorCompra = Math.max(Properties.MAX_BUY_VALUE, liquidity*0.08);
		}		
	}
	
	private double actualInversionOnShare (Share share) {
		double actualInversion = 0;
		for(Accion inversion : misAcciones) {
			if(inversion.getIdCompany().equalsIgnoreCase(share.getName()))
				actualInversion += inversion.getCantidad() * share.getValue(); //inversion.getValorCompra()	
		}
		return actualInversion;
	}
	
	private double getPercentToSell (double actualValue, double firstValue, int clusterTime) {
		int value = 0;
		double inversionReturn = (actualValue - firstValue) / firstValue;
		if(liquidity < (Properties.MAX_BUY_VALUE*4)) {
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
		if(impulsive)
			agentType += "IMP,";
		if(perception)
			agentType += "PER,";
		if(anxiety)
			agentType += "ANX,";
		if(memory)
			agentType += "MEM,";
		if(isDiversifier)
			agentType += "DIV,";
		return agentType + String.format("%.2f",rentabilidadCompra) + "]";
	}
	
}
