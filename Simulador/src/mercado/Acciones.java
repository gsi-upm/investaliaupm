package mercado;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Acciones {
	
	private String nombre;
	private double valor;
	private double ultimoPorcentaje;
	private double max;
	private double min;
	private double maximumVariation;
	private Random random;	
	private ArrayList<Double> historicoAccion = new ArrayList<Double>(Properties.STOCK_MEMORY);

	public Acciones(String nombre, double valor, double ultimoPorcentaje
			, double max, double min, double maximumVariation){
		this.nombre = nombre;
		this.valor = valor;
		this.ultimoPorcentaje = ultimoPorcentaje;
		this.setMax(max);
		this.min = min;
		this.maximumVariation = maximumVariation;
		random = new Random((long)(valor * (new Date()).getTime()));
	}

	public double getVariation() {
		double variation = random.nextGaussian()/3;
		if(variation > 1)
			variation = 1;
		else if(variation < -1)
			variation = -1;
		return  variation * maximumVariation;
	}
	
	// return a random value in the range [0..1] with normal distribution around 0.
	// Implements the Marsaglia Polar Method, as described in wikipedia, but only re
	public double randomNormal()
	{
	  double x = 1.0, y = 1.0, s = 2.0; // s = x^2 + y^2
	  while(s >= 1.0)
	  {
	    x = random.nextDouble()*2 - 1;
	    y = random.nextDouble()*2 - 1;
	    s = x*x + y*y;
	  }
	  //System.out.println("Variation "+getNombre()+" x:"+x+" y:"+y+" s:"+ (x * Math.sqrt(-2.0f * Math.log10(s)/s)));
	  return x * Math.sqrt(-2.0f * Math.log10(s)/s);
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
		if(historicoAccion.size() >= Properties.STOCK_MEMORY){
			historicoAccion.remove(0); // remove the first element
		}
		return historicoAccion.add(movement);
	}
	
	
	
	
}
