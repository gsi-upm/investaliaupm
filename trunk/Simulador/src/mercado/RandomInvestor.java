package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class RandomInvestor extends InvestorType {

	public RandomInvestor(Inversores investor) {
		liquidez = Properties.INITIAL_LIQUIDITY/2;
		maxValorCompra = Properties.MAX_BUY_VALUE;
		actividadVender = 0.2;
		actividadComprar = 0.2;		
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
			investor.updateFinancialReputation(stockLiquidity, inversionReturn);
			if (myInversion.getCantidad() <= 0) {
				investor.misAcciones.remove(myInversion);
				id--;							
			}
		}		
		
		//comprar
		if (liquidez > 0){
			for (Acciones accionesBolsa : accionesDeBolsa.values()) {
				if(investor.randomInRange(0.0,1.0) > actividadComprar)
					continue;
				int limite1 = (int)Math.floor(maxValorCompra / accionesBolsa.getValor());
				int limite2 = (int)(liquidez / accionesBolsa.getValor());
				int number2buy = 0;
				if (limite1 > 0 && limite2 > 0){
					number2buy =  ((int)investor.randomInRange(1, limite1));
					if(number2buy > limite2)
						number2buy = limite2;
					Accion accionComprada = new Accion(number2buy, accionesBolsa.getValor(),
							accionesBolsa.getNombre(), investor.getTime());
					liquidez -=  number2buy*accionesBolsa.getValor();
					investor.misAcciones.add(accionComprada);									
				}
			}			
		}
	}

}
