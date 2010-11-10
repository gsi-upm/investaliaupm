package es.upm.gsi.marketSimulator;

import java.util.HashMap;
import org.ascape.model.Agent;
import org.ascape.model.CellOccupant;
import org.ascape.model.rule.Rule;

public class Ibex35 extends CellOccupant {		
	private static final long serialVersionUID = 4200869887037516524L;
	HashMap<String,Share> ibex35 = new HashMap<String,Share>(35);
	
	
	//create the list of rules for scape
	public void scapeCreated() {
		//rellenamos por primera vez las acciones
		getScape().addInitialRule(new Rule("generateShares"){			
			private static final long serialVersionUID = -5487259943273733988L;

			@Override
			public void execute(Agent agent) {
				((Ibex35) agent).generateShares();
				
			}
        });
		//metodo update
		getScape().addRule(UPDATE_RULE);
	
	}
	
	
	public void generateShares(){
		//ibex35.put("Telefonica",new RandomShare("Telefonica", 17.970, -0.61, 95, 5, 0.11));
		//ibex35.put("Inditex",new RandomShare("Inditex", 49.490, -0.86, 340, 10, 0.13));
		//ibex35.put("Santander",new RandomShare("Santander", 10.62, -1.21, 95, 2, 0.15));
		
		/*ibex35.put("Telefonica",new RandomShare("Telefonica", 17.970, -0.61, 95, 5, 0.08));
		ibex35.put("Inditex",new RandomShare("Inditex", 49.490, -0.86, 340, 10, 0.10));
		ibex35.put("Santander",new RandomShare("Santander", 10.62, -1.21, 95, 2, 0.12));		
		ibex35.put("BBVA",new RandomShare("BBVA", 9.62, -0.23, 195, 3, 0.09));*/
		
		
		ibex35.put("Telefonica",new HistoryFileShare("Telefonica","Communication","FinancialFiles/Telefonica.txt"));
		ibex35.put("Cepsa",new HistoryFileShare("Cepsa","Petrol","FinancialFiles/Cepsa.txt"));
		ibex35.put("Santander",new HistoryFileShare("Santander","Bank","FinancialFiles/Santander.txt"));		
		ibex35.put("BBVA",new HistoryFileShare("BBVA","Bank","FinancialFiles/BBVA.txt"));
		ibex35.put("Endesa",new HistoryFileShare("Endesa","Power","FinancialFiles/Endesa.txt"));
		ibex35.put("Iberdrola",new HistoryFileShare("Iberdrola","Power","FinancialFiles/Iberdrola.txt"));
		ibex35.put("GasNatural",new HistoryFileShare("GasNatural","Power","FinancialFiles/GasNatural.txt"));
		
		System.out.println("Shares filled");
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
		
	public HashMap<String,Share> getShares() {
		return ibex35;
	}
	
}
