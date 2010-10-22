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
	HashMap<String,Share> ibex35 = new HashMap<String,Share>(35);
	
	
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
		//ibex35.put("Telefonica",new RandomShare("Telefonica", 17.970, -0.61, 95, 5, 0.11));
		//ibex35.put("Inditex",new RandomShare("Inditex", 49.490, -0.86, 340, 10, 0.13));
		//ibex35.put("Santander",new RandomShare("Santander", 10.62, -1.21, 95, 2, 0.15));
		
		/*ibex35.put("Telefonica",new RandomShare("Telefonica", 17.970, -0.61, 95, 5, 0.08));
		ibex35.put("Inditex",new RandomShare("Inditex", 49.490, -0.86, 340, 10, 0.10));
		ibex35.put("Santander",new RandomShare("Santander", 10.62, -1.21, 95, 2, 0.12));		
		ibex35.put("BBVA",new RandomShare("BBVA", 9.62, -0.23, 195, 3, 0.09));*/
		
		
		ibex35.put("Telefonica",new HistoryFileShare("Telefonica","FinancialFiles/Telefonica.txt"));
		ibex35.put("Cepsa",new HistoryFileShare("Cepsa", "FinancialFiles/Cepsa.txt"));
		ibex35.put("Santander",new HistoryFileShare("Santander", "FinancialFiles/Santander.txt"));		
		ibex35.put("BBVA",new HistoryFileShare("BBVA", "FinancialFiles/BBVA.txt"));		
		
		System.out.println("RandomShare rellenadas");
	}

	public void update() {
		for (Share share : ibex35.values()) {
			share.setNextValue();
			//double variation = accion.getVariation();
			//variation = accion.setValor(accion.getValor(), variation);
			//System.out.println("Variation "+accion.getNombre()+": "+accion.getVariation()+" value:"+accion.getValor());
			//accion.setUltimoPorcentaje(variation); //accion.setUltimoPorcentaje(oldValue/newValue-1);			
			//meto el porcentaje en la memoria de la bolsa
			//accion.addMovment(variation);	
		}
	}
		
	public HashMap<String,Share> getAcciones() {
		return ibex35;
	}
	
}
