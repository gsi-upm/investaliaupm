package mercado;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.ascape.model.Agent;
import org.ascape.model.CellOccupant;
import org.ascape.model.rule.Rule;

public class Ibex35 extends CellOccupant {
	
	DecimalFormat miFormato = new DecimalFormat("0.000");
	HashMap<String,Acciones> ibex35 = new HashMap<String,Acciones>(35);
	
	
	//create the list of rules for scape
	public void scapeCreated() {
		//rellenamos por primera vez las acciones
		getScape().addInitialRule(new Rule("rellenarAcciones"){
			@Override
			public void execute(Agent agent) {
				((Ibex35) agent).rellenarAcciones();
				
			}
        });
		//metodo update
		getScape().addRule(UPDATE_RULE);
	
	}
	
	
	public void rellenarAcciones(){
		//ibex35.put("Telefonica",new Acciones("Telefonica", 17.970, -0.61, 95, 5, 0.11));
		//ibex35.put("Inditex",new Acciones("Inditex", 49.490, -0.86, 340, 10, 0.13));
		//ibex35.put("Santander",new Acciones("Santander", 10.62, -1.21, 95, 2, 0.15));
		ibex35.put("Telefonica",new Acciones("Telefonica", 17.970, -0.61, 95, 5, 0.08));
		ibex35.put("Inditex",new Acciones("Inditex", 49.490, -0.86, 340, 10, 0.10));
		ibex35.put("Santander",new Acciones("Santander", 10.62, -1.21, 95, 2, 0.12));		
		ibex35.put("BBVA",new Acciones("BBVA", 9.62, -0.23, 195, 3, 0.09));
		System.out.println("Acciones rellenadas");
	}

	public void update() {
		for (Acciones accion : ibex35.values()) {
			double variation = accion.getVariation();
			//TODO: variation in stocks can overpass maximum and minimun value 
			//      of the stocks configured in the constructor
			//accion.setValor(accion.getValor() * (1 + variation));
			variation = accion.setValor(accion.getValor(), variation);
			//System.out.println("Variation "+accion.getNombre()+": "+accion.getVariation()+" value:"+accion.getValor());
			
			//accion.setValor(randomInRange(accion.getMax(), accion.getMin()));			
			accion.setUltimoPorcentaje(variation); //accion.setUltimoPorcentaje(oldValue/newValue-1);			
			//meto el porcentaje en la memoria de la bolsa
			accion.addMovment(variation);	
		}
	}
		
	public HashMap<String,Acciones> getAcciones() {
		return ibex35;
	}
	
}
