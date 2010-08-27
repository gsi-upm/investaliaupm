package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class AmateurInvestor extends InvestorType {
	public AmateurInvestor(Inversores investor) {
		liquidez = Properties.INITIAL_LIQUIDITY/2; //setLiquidez(randomInRange(3000,10000));
        maxValorCompra = Properties.MAX_BUY_VALUE;
        actividadComprar = Properties.BUY_PROBABILITY/Properties.PERCEPTION_DEGRADATION;
        actividadVender = Properties.SELL_PROBABILITY/Properties.PERCEPTION_DEGRADATION;
        this.investor = investor;
        sellTable = Properties.sellAmateurTable;
        sellAll = Properties.sellAmateurAll;
	}

	@Override
	public String getAgentTypeToString() {		
		return "";
	}

	@Override
	public void jugarEnBolsa(Ibex35 miBolsa) {
		HashMap<String, Acciones> accionesDeBolsa = miBolsa.getAcciones();
		if (investor.randomInRange(0.0,1.0) < actividadVender){
			for(int id = 0; id < investor.misAcciones.size(); id++) {
				Accion myInversion = investor.misAcciones.get(id);
				Acciones share = accionesDeBolsa.get(myInversion.getIdCompany());
				double inversionReturn = (share.getValor() - myInversion.getValorCompra()) / myInversion.getValorCompra();
				int inversionClusterTime = (investor.getTime() - myInversion.getDate()) / Properties.TIME_CLUSTER;
				boolean buying = false;				
				if(inversionClusterTime > sellAll[1])
					buying = true;
				else if(inversionClusterTime > sellAll[0] && inversionReturn > 0)
					buying = true;
				else if(inversionReturn > sellTable[0][0])
					buying = true;
				if(buying) {
					double sharesToSell = myInversion.getCantidad();
					myInversion.setCantidad(0);
					double stockLiquidity = sharesToSell * share.getValor();
					liquidez +=  stockLiquidity;
					investor.updateFinancialReputation(stockLiquidity, inversionReturn);
					investor.misAcciones.remove(myInversion);
					id--;	
				}
			}
		}
		
		//comprar
		if (liquidez > 0 && investor.randomInRange(0.0,1.0) < actividadComprar){
			//para cada accion de la bolsa, tendre que ver si me interesa comprar
			// si compro le tengo que construir un objeto accion y meterlo en todas las acciones
			// de la bolsa
			for (Acciones accionesBolsa : accionesDeBolsa.values()) {
				ArrayList<Double> historico = accionesBolsa.getHistoricoAccion();				
				if(historico.size() == 0 || historico.get(historico.size()-1) >= 0) //accionesBolsa.getUltimoPorcentaje() >= 0
					continue;
				if(investor.getId() == 8)
					System.out.println("id:"+investor.getId()+" nombre:"+accionesBolsa.getNombre()+" compramos");
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
			//Estimates the capital I have.
			investor.setCapital(miBolsa, liquidez);
		}		
	}
	
}
