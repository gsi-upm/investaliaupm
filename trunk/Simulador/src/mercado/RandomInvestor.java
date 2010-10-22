package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class RandomInvestor extends InvestorType {

	public RandomInvestor(Inversores investor) {
		initialCapital = Properties.INITIAL_LIQUIDITY;
		liquidity = initialCapital;
		maxValorCompra = Properties.MAX_BUY_VALUE;
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
	public void jugarEnBolsa(Ibex35 stock) {
		HashMap<String, Share> shares = stock.getAcciones();
		for(int id = 0; id < misAcciones.size(); id++) {
			if(investor.randomInRange(0.0,1.0) > sellProbability)
				continue;
			sells++;
			Accion myInversion = misAcciones.get(id);
			Share share = shares.get(myInversion.getIdCompany());			
			int sharesToSell = investor.randomInRange(Math.max(myInversion.getCantidad()/3,1)
					,myInversion.getInitialQuantity());
			if(sharesToSell > myInversion.getCantidad())
				sharesToSell = myInversion.getCantidad();
			myInversion.setCantidad(myInversion.getCantidad() - sharesToSell);
			double stockLiquidity = sharesToSell * share.getValue();
			liquidity +=  stockLiquidity;
			//double inversionReturn = (share.getValor() - myInversion.getValorCompra()) 
			//	/ myInversion.getValorCompra();
			if((share.getValue() - myInversion.getValorCompra()) < 0)
				capitalWithNegativeReturn += stockLiquidity;
			addOperationClosed (myInversion, sharesToSell, stockLiquidity, investor.getTime());
			//investor.updateFinancialReputation(ibex35);
			if (myInversion.getCantidad() < 0)
				System.out.println("WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			if (myInversion.getCantidad() <= 0) {
				misAcciones.remove(myInversion);
				id--;							
			}			
		}		
		
		//Buy
		if (liquidity > 0){
			for (Share share : shares.values()) {
				if(investor.randomInRange(0.0,1.0) > buyProbability)
					continue;
				int limit1 = (int)Math.floor(maxValorCompra / share.getValue());
				int limit2 = (int)(liquidity / share.getValue());
				int number2buy = 0;
				if (limit1 > 0 && limit2 > 0){
					buys++;
					number2buy =  ((int)investor.randomInRange(1, limit1));
					if(number2buy > limit2)
						number2buy = limit2;
					Accion accionComprada = new Accion(number2buy, share.getValue(),
							share.getName(), investor.getTime());
					liquidity -=  number2buy*share.getValue();
					investCapital += number2buy*share.getValue();
					misAcciones.add(accionComprada);									
				}
			}			
		}
		//Estimates the capital I have.
		//investor.setCapital(miBolsa, liquidez);
		this.maxValorCompra = Math.max(Properties.MAX_BUY_VALUE, liquidity*0.1);
	}

}
