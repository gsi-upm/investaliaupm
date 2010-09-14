package mercado;

public abstract class InvestorType {
	protected double liquidez;
	// The limit of money that one investor can invest
	protected double maxValorCompra;
	// activity factor
	protected double actividadComprar;
	protected double actividadVender;
	//profitability
	protected double rentabilidadVenta;
	protected double rentabilidadCompra;
	/* Thresholds to sell (buy). If a share get down (up) a number of
	 * iteracionesVenta (iteracionesCompra) the investor makes a decision */
	protected int iteracionesVenta;
	protected int iteraccionesCompra;
	protected Inversores investor;
	double sellTable[][];
	double sellAll[];
	
	//For statistics
	int buys = 0;
	int sells = 0;
	double capitalWithNegativeReturn = 0;
	
	public abstract void jugarEnBolsa(Ibex35 miBolsa);
	
	public abstract String getAgentTypeToString();
}
