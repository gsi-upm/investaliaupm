package mercado;

public class Investment {

	/**
	 * A share (accion) has amount, price, id_company and date
	 */
	private int initialQuantity;
	private int quantity;
	private double buyValue;
	private String idCompany;
	private int date;
	
	public Investment(int quantity, double buyValue, String idCompany, int date){
		this.initialQuantity = quantity;
		this.setQuantity(quantity);
		this.setBuyValue(buyValue);
		this.setIdCompany(idCompany);
		this.setDate(date);
	}
	
	public double getRentability (Ibex35 ibex35) {
		Share share = ibex35.getAcciones().get(idCompany);
		return (share.getValue() - buyValue) / buyValue;
	}

	public void setQuantity(int cantidad) {
		this.quantity = cantidad;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setBuyValue(double valorCompra) {
		this.buyValue = valorCompra;
	}

	public double getBuyValue() {
		return buyValue;
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
