package mercado;

import java.util.ArrayList;

abstract public class Share {
	protected String name;
	protected double value;	
	private ArrayList<Double> variationsHistory = new ArrayList<Double>(Properties.STOCK_MEMORY);
	
	abstract void setNextValue();	
	
	public void setName(String nombre) {
		this.name = nombre;
	}
	public String getName() {
		return name;
	}	
	
	public void setValue(double valor) {
		this.value = valor;
	}	
	
	public double getValue() {
		return value;
	}
	
	public void setVariationsHistory(ArrayList<Double> memoriaBolsa) {
		this.variationsHistory = memoriaBolsa;
	}

	public ArrayList<Double> getVariationsHistory() {
		return variationsHistory;
	}
	
	public boolean addHistory (double movement) {
		if(variationsHistory.size() >= Properties.STOCK_MEMORY){
			variationsHistory.remove(0); // remove the first element
		}
		return variationsHistory.add(movement);
	}
	
}
