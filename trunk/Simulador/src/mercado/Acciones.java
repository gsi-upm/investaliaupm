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
	private int maxReached;
	private int minReached;
	private double maximumVariation;
	private Random random;	
	private ArrayList<Double> historicoAccion = new ArrayList<Double>(Properties.STOCK_MEMORY);
	
	//Statistics
	public double variationUp = 0;
	public double variationDown = 0;

	public Acciones(String nombre, double valor, double ultimoPorcentaje
			, double max, double min, double maximumVariation){
		this.nombre = nombre;
		this.valor = valor;
		this.ultimoPorcentaje = ultimoPorcentaje;
		if(Properties.STOCK_VARIATION > 1)
			this.max = Double.POSITIVE_INFINITY;
		else
			this.max = max;
		if(Properties.STOCK_VARIATION < 1)
			this.min = 0;
		else
			this.min = min;
		this.maximumVariation = maximumVariation;
		System.out.println("Random:"+(long)(valor * (new Date()).getTime()));
		random = new Random((long)(valor * (new Date()).getTime()));
	}

	public double getVariation() {
		double variation = random.nextGaussian()/Properties.VARIATION_SCALE; 
		if(variation > 0) //Bear Market -> variation *= 0.X, Bull Market -> variation *= 1.X
			variation *= Properties.STOCK_VARIATION;
		if(variation > 1)
			variation = 1;
		else if(variation < -1)
			variation = -1;
		variation *= maximumVariation;
		if(variation > 0)
			variationUp += variation;
		else
			variationDown += variation;
		return  variation;
		//return  variation * maximumVariation;
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
	public double setValor(double valor, double variation) {
		if(valor * (1+variation) > max) {
			valor = valor * (1 - 2 * variation);
			maxReached++;
			return -Properties.LINEAL_REVERSE_SHARE_LIMIT*variation;
		}
		else if(valor * (1+variation) < min) {
			valor = valor * (1 - 2 * variation);
			minReached++;
			return -Properties.LINEAL_REVERSE_SHARE_LIMIT*variation;
		}
		this.valor = valor * (1+variation);
		return variation;
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
	
	public int getMaxReached () {
		return maxReached;
	}
	public int getMinReached () {
		return minReached;
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
