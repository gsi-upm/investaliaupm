package mercado;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ascape.model.Agent;
import org.ascape.model.HostCell;
import org.ascape.model.Scape;
import org.ascape.model.event.ScapeEvent;
import org.ascape.model.rule.Rule;
import org.ascape.model.space.Array1D;
import org.ascape.model.space.Array2DVonNeumann;
import org.ascape.view.vis.ChartView;
import org.ascape.view.vis.Overhead2DView;

public class SimulateSocialExchange extends Scape {

	public Scape spaceSocial;
	public Scape personas;
	public Scape bolsa;
	//public static Bolsa miBolsa;
	public Ibex35 miBolsa;
	private List<Mensaje> popularMessages[] = new ArrayList[1];	
	private int nInversores = 400;
	private int nBolsas = 1;
	
	
	Overhead2DView latticeView;
	
	public void createScape() {
        super.createScape();
        
        for(int i = 0; i < popularMessages.length; i++)
        	popularMessages[i] = new ArrayList<Mensaje>();
        
        spaceSocial = new Scape(new Array2DVonNeumann());
        spaceSocial.setPrototypeAgent(new HostCell());
        spaceSocial.setExtent(50, 50); //spaceSocial.setExtent(30, 30);        
        spaceSocial.setName("sitioweb");

        Inversores miInversor = new Inversores();
        miInversor.setHostScape(spaceSocial);
        personas = new Scape();
        personas.setName("Inversores");
        personas.setPrototypeAgent(miInversor);
        personas.setExecutionOrder(Scape.RULE_ORDER);
        
        miBolsa = new Ibex35();
        miBolsa.setHostScape(spaceSocial);
        bolsa = new Scape(new Array1D(), "Bolsa- ibex35", miBolsa);
    
        bolsa.setExecutionOrder(Scape.RULE_ORDER);
        
        add(spaceSocial);
        add(personas);
        add(bolsa);	
	
        Rule jugarEnBolsa = new Rule("Invertir en bolsa") {
	        /**        * 
	         */
	        private static final long serialVersionUID = 665608531104091849L;
	
	        public void execute(Agent a) {
	        	((Inversores) a).jugarEnBolsa(miBolsa);
	        }
	    };	
	    //bolsa.addRule(jugarEnBolsa);
	    personas.addRule(jugarEnBolsa);			
		Rule tipoInversor = new Rule("Elige el tipo de inversor") {	        
	        private static final long serialVersionUID = 66560843110409183L;	
	        public void execute(Agent a) {
	        	((Inversores) a).eligeInversor();
	        }
	    };	    
	    personas.addInitialRule(tipoInversor);
	    Rule readAndCommentNeighbors = new Rule("Lee y comenta a vecinos") {	        
	        private static final long serialVersionUID = 66560843110409123L;	
	        public void execute(Agent a) {
	        	((Inversores) a).chooseNeighborToPlay();
	        }
	    };	    
	    personas.addRule(readAndCommentNeighbors);
	}
	
	public void scapeSetup(ScapeEvent scapeEvent) {
        ((Scape) personas).setExtent(getnInversores());
        ((Scape) bolsa).setExtent(nBolsas);
    }

    // create views and charts
    public void createGraphicViews() {
        super.createGraphicViews();
        
        latticeView = new Overhead2DView();
        latticeView.setCellSize(7); //15
        //latticeView.setDrawNetwork(true);
        spaceSocial.addView(latticeView);
    }

	public void setnInversores(int nInversores) {
		this.nInversores = nInversores;
	}

	public List<Mensaje>[] getPopularMessages() {
		return popularMessages;
	}
	
	public void sortAddMessages(Mensaje mensaje) {
		for(int i = 0; i < popularMessages.length; i++) {
			boolean sorted = false;
			for(int j = 0; j < popularMessages[i].size(); j++) {			
				if(mensaje.getReputation()[i] <= popularMessages[i].get(j).getReputation()[i]) {
					popularMessages[i].add(j, mensaje);
					sorted=true;
					break;
				}
			}
			if(!sorted)
				popularMessages[i].add(mensaje);
		}
	}
	
	public int getnInversores() {
		return nInversores;
	}
	
	public Scape getBolsa(){
		return bolsa;
	}
	
}
