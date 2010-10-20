package mercado;

public class OperationClosed {
	//private int quantity;
	private double sellValue;
	private double boughtValue;
	private String idCompany;
	private double rentability;
	private int date;
	
	public OperationClosed(double sellValue, double boughtValue, String idCompany, int date){
		//this.quantity = quantity;
		this.sellValue = sellValue;
		this.boughtValue = boughtValue;
		this.idCompany = idCompany;
		this.date = date;	
		rentability = (sellValue - boughtValue) / boughtValue;
	}
	
	/*public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}*/

	public double getSellValue() {
		return sellValue;
	}

	public void setSellValue(double sellValue) {
		this.sellValue = sellValue;
	}

	public double getBoughtValue() {
		return boughtValue;
	}

	public void setBoughtValue(double boughtValue) {
		this.boughtValue = boughtValue;
	}

	public String getIdCompany() {
		return idCompany;
	}

	public void setIdCompany(String idCompany) {
		this.idCompany = idCompany;
	}

	public void setDate(int date) {
		this.date = date;
	}	
	
	public int getDate() {
		return date;
	}
	
	public double getRentability() {
		return rentability;
	}
	
}
