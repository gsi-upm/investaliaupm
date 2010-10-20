package mercado;

public class Accion {

	/**
	 * A share (accion) has amount, price, id_company and date
	 */
	private int initialQuantity;
	private int cantidad;
	private double valorCompra;
	private String idCompany;
	private int date;
	
	public Accion(int cantidad, double valorCompra, String idCompany, int date){
		this.initialQuantity = cantidad;
		this.setCantidad(cantidad);
		this.setValorCompra(valorCompra);
		this.setIdCompany(idCompany);
		this.setDate(date);
	}
	
	public double getRentability (Ibex35 ibex35) {
		Acciones share = ibex35.getAcciones().get(idCompany);
		return (share.getValor() - valorCompra) / valorCompra;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setValorCompra(double valorCompra) {
		this.valorCompra = valorCompra;
	}

	public double getValorCompra() {
		return valorCompra;
	}

	public void setIdCompany(String idCompany) {
		this.idCompany = idCompany;
	}

	public String getIdCompany() {
		return idCompany;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getDate() {
		return date;
	}
	
	public int getInitialQuantity() {
		return initialQuantity;
	}
}
