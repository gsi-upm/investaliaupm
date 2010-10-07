package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class RandomInvestor extends InvestorType {

	public RandomInvestor(Inversores investor) {
		liquidez = Properties.INITIAL_LIQUIDITY/2;
		maxValorCompra = Properties.MAX_BUY_VALUE;
		actividadVender = Properties.RAND_INV_BUY_PROBABILITY;
		actividadComprar = Properties.RAND_INV_SELL_PROBABILITY;		
		this.investor = investor;
	}
	
	@Override
	public String getAgentTypeToString() {		
		return "";
	}

	@Override
	public void jugarEnBolsa(Ibex35 miBolsa) {
		HashMap<String, Acciones> accionesDeBolsa = miBolsa.getAcciones();
		for(int id = 0; id < investor.misAcciones.size(); id++) {
			if(investor.randomInRange(0.0,1.0) > actividadVender)
				continue;
			sells++;
			Accion myInversion = investor.misAcciones.get(id);
			Acciones share = accionesDeBolsa.get(myInversion.getIdCompany());
			int sharesToSell = investor.randomInRange(1,myInversion.getInitialQuantity());
			if(sharesToSell > myInversion.getCantidad())
				sharesToSell = myInversion.getCantidad();
			myInversion.setCantidad(myInversion.getCantidad() - sharesToSell);
			double stockLiquidity = sharesToSell * share.getValor();
			liquidez +=  stockLiquidity;
			double inversionReturn = (share.getValor() - myInversion.getValorCompra()) 
				/ myInversion.getValorCompra();
			if(inversionReturn < 0)
				capitalWithNegativeReturn += stockLiquidity;
			investor.updateFinancialReputation(stockLiquidity, inversionReturn);
			if (myInversion.getCantidad() <= 0) {
				investor.misAcciones.remove(myInversion);
				id--;							
			}
		}		
		
		//Buy
		if (liquidez > 0){
			for (Acciones accionesBolsa : accionesDeBolsa.values()) {
				if(investor.randomInRange(0.0,1.0) > actividadComprar)
					continue;
				int limit1 = (int)Math.floor(maxValorCompra / accionesBolsa.getValor());
				int limit2 = (int)(liquidez / accionesBolsa.getValor());
				int number2buy = 0;
				if (limit1 > 0 && limit2 > 0){
					buys++;
					number2buy =  ((int)investor.randomInRange(1, limit1));
					if(number2buy > limit2)
						number2buy = limit2;
					Accion accionComprada = new Accion(number2buy, accionesBolsa.getValor(),
							accionesBolsa.getNombre(), investor.getTime());
					liquidez -=  number2buy*accionesBolsa.getValor();
					investor.misAcciones.add(accionComprada);									
				}
			}			
		}
		//Estimates the capital I have.
		investor.setCapital(miBolsa, liquidez);
		this.maxValorCompra = Math.max(Properties.MAX_BUY_VALUE, liquidez*0.1);
	}

}
