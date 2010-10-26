package mercado;

import java.util.ArrayList;
import java.util.HashMap;

public class AmateurInvestor extends InvestorType {	
	
	public AmateurInvestor(Inversores investor) {
		initialCapital = Properties.INITIAL_LIQUIDITY;
		liquidity = initialCapital; //setLiquidez(randomInRange(3000,10000));
        maxValorCompra = Properties.MAX_BUY_VALUE;
        buyProbability = Properties.BUY_PROBABILITY;
        sellProbability = Properties.SELL_PROBABILITY/Properties.PERCEPTION_DEGRADATION;
        this.investor = investor;
        if(Properties.sellAmateurTable != null)
        	sellTable = Properties.sellAmateurTable;
        else {
        	sellTable = new double[][]{{investor.randomInRange
        		(Properties.sellAmateurRange[0], Properties.sellAmateurRange[1]),100}};
        }        	
        sellAll = Properties.sellAmateurAll;
        //debugParam = investor.getId();
	}

	@Override
	public String getAgentTypeToString() {
		if(sellTable != null)
			return "["+sellTable[0][0]+"]";
		return "";
	}

	@Override
	public void jugarEnBolsa(Ibex35 miBolsa) {
		HashMap<String, Share> shares = miBolsa.getAcciones();
		if (investor.randomInRange(0.0,1.0) < sellProbability){
			for(int id = 0; id < misAcciones.size(); id++) {
				Investment myInversion = misAcciones.get(id);
				Share share = shares.get(myInversion.getIdCompany());
				double inversionReturn = (share.getValue() - myInversion.getBuyValue()) / myInversion.getBuyValue();
				int inversionClusterTime = (investor.getTime() - myInversion.getDate()) / Properties.TIME_CLUSTER;
				boolean selling = false;				
				if(inversionClusterTime > sellAll[1]) {
					sellsAll1++;
					selling = true;
				} else if(inversionClusterTime > sellAll[0] && inversionReturn > 0) {
					sellsAll0++;
					selling = true;
				} else if(inversionReturn > sellTable[0][0])
					selling = true;
				if(selling) {
					sells++;
					int sharesToSell = myInversion.getQuantity();
					myInversion.setQuantity(0);
					double stockLiquidity = sharesToSell * share.getValue();
					if(inversionReturn < 0)
						capitalWithNegativeReturn += stockLiquidity;
					liquidity +=  stockLiquidity;
					//investor.updateFinancialReputation(ibex35);
					addOperationClosed (myInversion, sharesToSell, stockLiquidity, investor.getTime());
					misAcciones.remove(myInversion);
					id--;	
				}
			}
		}		
		//Buy
		if (liquidity > 0 && investor.randomInRange(0.0,1.0) < buyProbability){
			//para cada accion de la bolsa, tendre que ver si me interesa comprar
			// si compro le tengo que construir un objeto accion y meterlo en todas las acciones
			// de la bolsa
			for (Share share : shares.values()) {
				ArrayList<Double> historico = share.getVariationsHistory();				
				if(historico.size() == 0 || historico.get(historico.size()-1) >= 0) 
					continue;	//accionesBolsa.getUltimoPorcentaje() >= 0			
				int limite1 = (int)Math.floor(maxValorCompra / share.getValue());
				int limite2 = (int)(liquidity / share.getValue());
				int number2buy = 0;
				if (limite1 > 0 && limite2 > 0){
					buys++;
					number2buy =  ((int)investor.randomInRange(1, limite1));
					if(number2buy > limite2)
						number2buy = limite2;
					Investment accionComprada = new Investment(number2buy, share.getValue(),
							share.getName(), investor.getTime());
					liquidity -=  number2buy * share.getValue();
					investCapital += number2buy * share.getValue();
					misAcciones.add(accionComprada);									
				}
				//if(investor.getId() == debugParam)
				//	System.out.println("id:"+investor.getId()+"["+investor.getTime()+"] "+accionesBolsa.getNombre()+" compra "
				//			+number2buy+" con val:"+accionesBolsa.getValor()+" y liq post:"+liquidez+" num:"+investor.misAcciones.size());
			}
			//Estimates the capital I have.
			//investor.setCapital(miBolsa, liquidez);
			this.maxValorCompra = Math.max(Properties.MAX_BUY_VALUE, liquidity*0.1);
		}		
	}
	
}
