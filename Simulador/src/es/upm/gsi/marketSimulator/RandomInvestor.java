package es.upm.gsi.marketSimulator;

import java.util.HashMap;

public class RandomInvestor extends InvestorType {

	public RandomInvestor(Investors investor) {
		initialCapital = Properties.INITIAL_LIQUIDITY;
		liquidity = initialCapital;
		maxBuyValue = Properties.MAX_BUY_VALUE;
		sellProbability = Properties.RAND_INV_SELL_PROBABILITY;
		buyProbability = Properties.RAND_INV_BUY_PROBABILITY;		
		this.investor = investor;
		
		//debugParam = investor.getId();
	}
	
	@Override
	public String getAgentTypeToString() {		
		return "";
	}

	@Override
	public void playInStock(Ibex35 stock) {
		HashMap<String, Share> shares = stock.getShares();
		for(int id = 0; id < myPortfolio.size(); id++) {
			if(investor.randomInRange(0.0,1.0) > sellProbability)
				continue;
			sells++;
			Investment myInversion = myPortfolio.get(id);
			Share share = shares.get(myInversion.getIdCompany());			
			int sharesToSell = investor.randomInRange(Math.max(myInversion.getQuantity()/3,1)
					,myInversion.getInitialQuantity());
			if(sharesToSell > myInversion.getQuantity())
				sharesToSell = myInversion.getQuantity();
			myInversion.setQuantity(myInversion.getQuantity() - sharesToSell);
			double stockLiquidity = sharesToSell * share.getValue();
			liquidity +=  stockLiquidity;
			//double inversionReturn = (share.getValor() - myInversion.getValorCompra()) 
			//	/ myInversion.getValorCompra();
			if((share.getValue() - myInversion.getBuyValue()) < 0)
				capitalWithNegativeReturn += stockLiquidity;
			addOperationClosed (myInversion, sharesToSell, stockLiquidity, investor.getTime());
			//investor.updateFinancialReputation(ibex35);
			if (myInversion.getQuantity() < 0)
				System.out.println("WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			if (myInversion.getQuantity() <= 0) {
				myPortfolio.remove(myInversion);
				id--;							
			}			
		}		
		
		//Buy
		if (liquidity > 0){
			for (Share share : shares.values()) {
				if(investor.randomInRange(0.0,1.0) > buyProbability)
					continue;
				int limit1 = (int)Math.floor(maxBuyValue / share.getValue());
				int limit2 = (int)(liquidity / share.getValue());
				int number2buy = 0;
				if (limit1 > 0 && limit2 > 0){
					buys++;
					number2buy =  ((int)investor.randomInRange(1, limit1));
					if(number2buy > limit2) {
						withoutLiquidity++;
						number2buy = limit2;
					}
					Investment accionComprada = new Investment(number2buy, share.getValue(),
							share.getName(), share.getCategory(), investor.getTime());
					liquidity -=  number2buy*share.getValue();
					investCapital += number2buy*share.getValue();
					myPortfolio.add(accionComprada);									
				} else
					withZero++;
			}			
		}
		//Estimates the capital I have.
		//investor.setCapital(miBolsa, liquidez);
		this.maxBuyValue = Math.max(Properties.MAX_BUY_VALUE, liquidity*0.1);
	}

}
