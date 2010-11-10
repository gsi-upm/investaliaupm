package es.upm.gsi.marketSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class InvestorType {
	protected double initialCapital;
	protected double liquidity;
	private List<OperationClosed> financialHistory = new ArrayList<OperationClosed>();
	private List<Double> capitalHistory = new ArrayList<Double>();
	protected ArrayList<Investment> myPortfolio = new ArrayList<Investment>();
	private double financialReputation = 0;
	// The limit of money that one investor can invest
	protected double maxValorCompra;
	// activity factor
	protected double buyProbability;
	protected double sellProbability;
	//profitability
	//protected double rentabilidadVenta;
	protected double rentabilityToBuy;
	/* Thresholds to sell (buy). If a share get down (up) a number of
	 * iteracionesVenta (iteracionesCompra) the investor makes a decision */
	//protected int iteracionesVenta;
	protected int iterationsToBuy;
	protected Investors investor;
	protected double sellTable[][];
	protected double sellAll[];
	
	//For statistics
	int buys = 0;
	int sells = 0;
	int sellsAll0 = 0;
	int sellsAll1 = 0;
	double capitalWithNegativeReturn = 0;
	double investCapital = 0;
	
	//For debug
	static int debugParam;
	
	public abstract void playInStock(Ibex35 miBolsa);
	
	public abstract String getAgentTypeToString();	

	public void addOperationClosed (Investment myInversion, int quantity, double inversionReturn, int time) {
		financialHistory.add(new OperationClosed(inversionReturn, myInversion.getBuyValue() * quantity, 
				myInversion.getIdCompany(), time));
	}
	
	public double getActualCapital(Ibex35 miBolsa){
		HashMap<String, Share> shares = miBolsa.getShares();
		double sum = 0;
		for(Investment myInversion : myPortfolio){
			Share share = shares.get(myInversion.getIdCompany());
			sum += myInversion.getQuantity() * share.getValue();
			//if(myInversion.getCantidad() <= 0)
			//	System.out.println("WARNING!!!\n"+myInversion.getCantidad()+"\n" +
			//			getClass()+"\n\n\n\n\n\n");
		}		
		return sum + liquidity;
	}
	
	public void updateFinancialReputation (Ibex35 ibex35) {
		financialReputation = 0;
		//Clean financialHistory each update for optimization
		for(int i = 0; i < financialHistory.size(); i++) {
			OperationClosed operation = financialHistory.get(i);
			if( (investor.getTime() - operation.getDate()) > Properties.TIME_LIMIT) {
				financialHistory.remove(i);
				i--;
			}
			else
				break;
		}
		double capitalClosed = 0;
		double operationsClosed = 0;
		for(int i = 0; i < financialHistory.size(); i++) {
			OperationClosed operation = financialHistory.get(i);
			if(operation.getRentability() < 0) {
				//System.out.println("id:"+investor.getId()+" class:"+getClass()+" fR:"+
				//1.5 * operation.getRentability() * operation.getBoughtValue()+" C:"+operation.getBoughtValue());
				operationsClosed += Properties.IF_RENTABILITY_NEGATIVE_DECREMENT * operation.getRentability()
						* operation.getBoughtValue();
			}
			else
				operationsClosed += operation.getRentability() * operation.getBoughtValue();
			capitalClosed += operation.getBoughtValue();
			//if(investor.getId() == debugParam) {
			//	System.out.println("OC->Com:"+operation.getIdCompany()+" BV:"+
			//			operation.getBoughtValue()+" SV:"+operation.getSellValue()+" Dat:"+operation.getDate());
			//}
		}
		if(capitalClosed != 0)
			operationsClosed /= capitalClosed; //= i
		
		double capitalOpened = 0;
		double operationOpened = 0;
		capitalOpened = 0;
		for(int i = 0; i < myPortfolio.size(); i++) {
			Investment operation = myPortfolio.get(i);
			double rentability = operation.getRentability(ibex35);
			operationOpened += rentability * operation.getBuyValue() * operation.getQuantity();
			//if(rentability < 0)				
			//	operationOpened += 1.5 * rentability * operation.getValorCompra() * operation.getCantidad();
			//else
			//	operationOpened += rentability * operation.getValorCompra() * operation.getCantidad();
			capitalOpened += operation.getBuyValue() * operation.getQuantity();
			//if(investor.getId() == debugParam) 
			//	System.out.println("Com:"+operation.getIdCompany()+" Can:"+operation.getCantidad()+" BV:"+
			//	operation.getValorCompra()+" AV:"+ibex35.getAcciones().get(operation.getIdCompany()).getValor()+
			//	" Dat:"+operation.getDate()+" InCan:"+operation.getInitialQuantity());			
		}
		if(capitalOpened != 0)
			operationOpened /= capitalOpened;
		
		if(capitalClosed != 0 && capitalOpened != 0) {
			double difference = capitalClosed / capitalOpened;
			if(difference < Properties.OPERATION_CLOSED_WEIGHT)
				financialReputation = Properties.OPERATION_CLOSED_WEIGHT * operationsClosed + 
					(1-Properties.OPERATION_CLOSED_WEIGHT) * operationOpened;
			else if(difference > 1/Properties.OPERATION_OPENED_WEIGHT)
				financialReputation = Properties.OPERATION_OPENED_WEIGHT * operationsClosed + 
					(1-Properties.OPERATION_OPENED_WEIGHT) * operationOpened;
			financialReputation = (operationsClosed * difference + operationOpened) / (difference + 1);
			//financialReputation = (Properties.OPERATION_OPENED_WEIGHT * operationsClosed * capitalClosed + 
			//	Properties.OPERATION_OPENED_WEIGHT * operationOpened * capitalOpened) / (capitalClosed+capitalOpened);
		} else
			financialReputation = operationsClosed + operationOpened;
		
		double capitalRentability = 0;
		if(capitalHistory.size() < Properties.CAPITAL_TIME_DIFFERENCE)
			capitalRentability = (getActualCapital(ibex35) - initialCapital) / initialCapital;
		else
			capitalRentability = (getActualCapital(ibex35) - capitalHistory.get(0)) / capitalHistory.get(0);
		
		financialReputation = capitalRentability * Properties.CAPITAL_INCREMENT_WEIGHT +
				financialReputation * Properties.OPERATIONS_WEIGHT;
		
		System.out.println("id:"+investor.getId()+"("+getClass()+") OC:"+operationsClosed+
				" OO:"+operationOpened+" capC:"+capitalClosed+" capO:"+capitalOpened+" CR:"+capitalRentability
				+" fr:"+financialReputation);
	}	
	
	public double getFinancialReputation() {
		return financialReputation;
	}
	
	public void addCapitalToHistory (double capital) {
		if(capitalHistory.size() == Properties.CAPITAL_TIME_DIFFERENCE)
			capitalHistory.remove(0);
		capitalHistory.add(capital);
	}
	
}
