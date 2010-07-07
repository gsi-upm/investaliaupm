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
		ibex35.put("Telefonica",new Acciones("Telefonica", 17.970, -0.61, 25, 17));
		ibex35.put("Inditex",new Acciones("Inditex", 49.490, -0.86, 60, 10));
		ibex35.put("Santander",new Acciones("Santander", 10.62, -1.21, 15, 12));
		System.out.println("Acciones rellenadas");
	}

	public void update() {
		for (Acciones accion : ibex35.values()) {
			double valorViejo = accion.getValor();
			accion.setValor(randomInRange(accion.getMax(), accion.getMin()));
			double valorNuevo = accion.getValor();
			accion.setUltimoPorcentaje(valorNuevo/valorViejo-1);
		/*	
			System.out.println("accion: " + accion.getNombre() + 
					", valor: " + miFormato.format(valorNuevo) + 
					", incremento del: " + miFormato.format(accion.getUltimoPorcentaje()));
		*/			
			//meto el porcentaje en la memoria de la bolsa
			accion.addMovment(valorNuevo/valorViejo-1);	
		}
	}
	
	public double getValor(){
		//obsolete
		return 150.5;
	}
	
	public HashMap<String,Acciones> getAcciones() {
		return ibex35;
	}
	
}
