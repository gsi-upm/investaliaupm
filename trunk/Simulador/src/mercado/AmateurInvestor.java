package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class AmateurInvestor extends InvestorType {
	static int debugParam;
	
	public AmateurInvestor(Inversores investor) {
		liquidez = Properties.INITIAL_LIQUIDITY/2; //setLiquidez(randomInRange(3000,10000));
        maxValorCompra = Properties.MAX_BUY_VALUE;
        actividadComprar = Properties.BUY_PROBABILITY/Properties.PERCEPTION_DEGRADATION;
        actividadVender = Properties.SELL_PROBABILITY/Properties.PERCEPTION_DEGRADATION;
        this.investor = investor;
        if(Properties.sellAmateurTable != null)
        	sellTable = Properties.sellAmateurTable;
        else {
        	sellTable = new double[][]{{investor.randomInRange
        		(Properties.sellAmateurRange[0], Properties.sellAmateurRange[1]),100}};
        }        	
        sellAll = Properties.sellAmateurAll;
        debugParam = investor.getId();
	}

	@Override
	public String getAgentTypeToString() {
		if(sellTable != null)
			return "["+sellTable[0][0]+"]";
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
				boolean selling = false;				
				if(inversionClusterTime > sellAll[1])
					selling = true;
				else if(inversionClusterTime > sellAll[0] && inversionReturn > 0)
					selling = true;
				else if(inversionReturn > sellTable[0][0])
					selling = true;
				if(selling) {
					sells++;
					double sharesToSell = myInversion.getCantidad();
					myInversion.setCantidad(0);
					double stockLiquidity = sharesToSell * share.getValor();
					if(inversionReturn < 0)
						capitalWithNegativeReturn += stockLiquidity;
					liquidez +=  stockLiquidity;
					investor.updateFinancialReputation(stockLiquidity, inversionReturn);
					investor.misAcciones.remove(myInversion);
					id--;	
				}
			}
		}		
		//Buy
		if (liquidez > 0 && investor.randomInRange(0.0,1.0) < actividadComprar){
			//para cada accion de la bolsa, tendre que ver si me interesa comprar
			// si compro le tengo que construir un objeto accion y meterlo en todas las acciones
			// de la bolsa
			for (Acciones accionesBolsa : accionesDeBolsa.values()) {
				ArrayList<Double> historico = accionesBolsa.getHistoricoAccion();				
				if(historico.size() == 0 || historico.get(historico.size()-1) >= 0) 
					continue;	//accionesBolsa.getUltimoPorcentaje() >= 0			
				int limite1 = (int)Math.floor(maxValorCompra / accionesBolsa.getValor());
				int limite2 = (int)(liquidez / accionesBolsa.getValor());
				int number2buy = 0;
				if (limite1 > 0 && limite2 > 0){
					buys++;
					number2buy =  ((int)investor.randomInRange(1, limite1));
					if(number2buy > limite2)
						number2buy = limite2;
					Accion accionComprada = new Accion(number2buy, accionesBolsa.getValor(),
							accionesBolsa.getNombre(), investor.getTime());
					liquidez -=  number2buy*accionesBolsa.getValor();
					investor.misAcciones.add(accionComprada);									
				}
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+"["+investor.getTime()+"] "+accionesBolsa.getNombre()+" compra "
				//			+number2buy+" con val:"+accionesBolsa.getValor()+" y liq post:"+liquidez+" num:"+investor.misAcciones.size());
			}
			//Estimates the capital I have.
			investor.setCapital(miBolsa, liquidez);
			this.maxValorCompra = Math.max(Properties.MAX_BUY_VALUE, liquidez*0.1);
		}		
	}
	
}
