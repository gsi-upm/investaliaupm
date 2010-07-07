package mercado;

import java.util.ArrayList;

public class Acciones {
	
	private String nombre;
	private double valor;
	private double ultimoPorcentaje;
	private double max;
	private double min;
	// añadir memoria a la bolsa?
	public final int MEMORIA_BOLSA = 5;
	private ArrayList<Double> historicoAccion = new ArrayList<Double>(MEMORIA_BOLSA);

	public Acciones(String nombre, double valor, double ultimoPorcentaje
			, double max, double min){
		this.nombre = nombre;
		this.valor = valor;
		this.ultimoPorcentaje = ultimoPorcentaje;
		this.setMax(max);
		this.min = min;
	}

	//getters and setters
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getNombre() {
		return nombre;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public double getValor() {
		return valor;
	}
	//en porcentaje
	public void setUltimoPorcentaje(double ultimo) {
		this.ultimoPorcentaje = ultimo;
	}
	//en porcentaje
	public double getUltimoPorcentaje() {
		return ultimoPorcentaje;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMax() {
		return max;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMin() {
		return min;
	}

	
	public void setHistoricoAccion(ArrayList<Double> memoriaBolsa) {
		this.historicoAccion = memoriaBolsa;
	}

	public ArrayList<Double> getHistoricoAccion() {
		return historicoAccion;
	}
	
	/**
	 * add a movement to the memory of the exchange
	 * 
	 * @param movement
	 * @return true if the movement can put at the last position
	 */
	
	public boolean addMovment (double movement) {
		if(historicoAccion.size() >= MEMORIA_BOLSA){
			historicoAccion.remove(0); // remove the first element
		}
		return historicoAccion.add(movement);
	}
	
	
	
	
}
