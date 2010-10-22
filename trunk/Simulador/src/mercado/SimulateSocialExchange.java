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
import org.ascape.util.data.DataGroup;
import org.ascape.util.data.StatCollector;
import org.ascape.util.data.StatCollectorCSA;
import org.ascape.util.data.StatCollectorCSAMM;
import org.ascape.util.data.StatCollectorCond;
import org.ascape.util.data.StatCollectorCondCSAMM;
import org.ascape.view.vis.ChartView;
import org.ascape.view.vis.Overhead2DView;

public class SimulateSocialExchange extends Scape {

	public Scape socialSpace;
	public Scape people;
	public Scape stock;
	//public static Bolsa miBolsa;
	private Ibex35 myStock;
	private List<Mensaje> popularMessages[] = new ArrayList[1];	
	public int nInvestors = Properties.NUM_INVESTORS;
	private int nBolsas = 1;
	
	
	Overhead2DView latticeView;
	
	public void createScape() {
        super.createScape();
        
        for(int i = 0; i < popularMessages.length; i++)
        	popularMessages[i] = new ArrayList<Mensaje>();
        
        socialSpace = new Scape(new Array2DVonNeumann());
        socialSpace.setPrototypeAgent(new HostCell());
        socialSpace.setExtent(50, 50); //spaceSocial.setExtent(30, 30);        
        socialSpace.setName("Web Site");

        Inversores miInversor = new Inversores();
        miInversor.setHostScape(socialSpace);
        people = new Scape();
        people.setName("Inversores");
        people.setPrototypeAgent(miInversor);
        people.setExecutionOrder(Scape.RULE_ORDER);
        
        myStock = new Ibex35();
        myStock.setHostScape(socialSpace);
        stock = new Scape(new Array1D(), "Bolsa- ibex35", myStock);
    
        stock.setExecutionOrder(Scape.RULE_ORDER);
        
        add(socialSpace);
        add(people);
        add(stock);	
	
        Rule jugarEnBolsa = new Rule("Invest in stock") {
	        private static final long serialVersionUID = 665608531104091849L;
	
	        public void execute(Agent a) {
	        	((Inversores) a).jugarEnBolsa(myStock);
	        }
	    };	
	    //bolsa.addRule(jugarEnBolsa);
	    people.addRule(jugarEnBolsa);			
		Rule tipoInversor = new Rule("Choose the type of the investor") {	        
	        private static final long serialVersionUID = 66560843110409183L;	
	        public void execute(Agent a) {
	        	((Inversores) a).eligeInversor();
	        }
	    };	    
	    people.addInitialRule(tipoInversor);
	    Rule readAndCommentNeighbors = new Rule("Reading and commenting to his neighbours") {	        
	        private static final long serialVersionUID = 66560843110409123L;	
	        public void execute(Agent a) {
	        	((Inversores) a).chooseNeighborToPlay();
	        }
	    };	    
	    people.addRule(readAndCommentNeighbors);
	    
	    
       
        StatCollector FinRepEXP = new StatCollectorCondCSAMM("FinRepEXP") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR);
            }
        };
        StatCollector FinRepEPerY = new StatCollectorCondCSAMM("FinRep EXP Per Y") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).perception == true);
            }
        };
        StatCollector FinRepEPerN = new StatCollectorCondCSAMM("FinRep EXP Per N") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).perception == false);
            }
        };
        StatCollector FinRepEAnxY = new StatCollectorCondCSAMM("FinRep EXP Anx Y") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).anxiety == true);
            }
        };
        StatCollector FinRepEAnxN = new StatCollectorCondCSAMM("FinRep EXP Anx N") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).anxiety == false);
            }
        };
        StatCollector FinRepEDivY = new StatCollectorCondCSAMM("FinRep EXP Div Y") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).isDiversifier == true);
            }
        };
        StatCollector FinRepEDivN = new StatCollectorCondCSAMM("FinRep EXP Div N") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Inversores)object).getInvestor()).isDiversifier == false);
            }
        };
        StatCollector FinRepAMA = new StatCollectorCondCSAMM("FinRepAMA") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.AMATEUR_INVESTOR);
            }
        };
        StatCollector FinRepRAM = new StatCollectorCondCSAMM("FinRepRAM") {
            public double getValue(Object object) {
               return ((Inversores) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Inversores) object).getTipoAgente()[0] == Inversores.RANDOM_INVESTOR);
            }
        };

        people.addStatCollector(FinRepEXP);
        people.addStatCollector(FinRepEPerY);
        people.addStatCollector(FinRepEPerN);
        people.addStatCollector(FinRepEAnxY);
        people.addStatCollector(FinRepEAnxN);
        people.addStatCollector(FinRepEDivY);
        people.addStatCollector(FinRepEDivN);
        people.addStatCollector(FinRepAMA);        
        people.addStatCollector(FinRepRAM);
	}
	
	public void scapeSetup(ScapeEvent scapeEvent) {
        ((Scape) people).setExtent(getnInversores());
        ((Scape) stock).setExtent(nBolsas);
    }

    // create views and charts
    public void createGraphicViews() {
        super.createGraphicViews();
        
        latticeView = new Overhead2DView();
        latticeView.setCellSize(7); //15
        //latticeView.setDrawNetwork(true);
        socialSpace.addView(latticeView);
         
                
        
        ChartView chart = new ChartView();
        people.addView(chart);
        chart.addSeries("Average FinRepEXP", Color.red);
        chart.addSeries("Average FinRep EXP Per Y", Color.black);
        chart.addSeries("Average FinRep EXP Per N", Color.cyan);
        chart.addSeries("Average FinRep EXP Anx Y", Color.YELLOW);
        chart.addSeries("Average FinRep EXP Anx N", Color.GRAY);
        chart.addSeries("Average FinRep EXP Div Y", Color.magenta); //.orange);
        chart.addSeries("Average FinRep EXP Div N", Color.PINK);
        chart.addSeries("Average FinRepAMA", Color.blue);
        chart.addSeries("Average FinRepRAM", Color.green);
        chart.setDisplayPoints(10000);

    }

	public void setnInversores(int nInversores) {
		this.nInvestors = nInversores;
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
		return nInvestors;
	}
	
	public Ibex35 getStock(){
		return myStock;
	}
	
}
