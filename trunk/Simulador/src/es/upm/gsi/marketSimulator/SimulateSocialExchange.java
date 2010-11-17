package es.upm.gsi.marketSimulator;

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
import org.ascape.util.data.StatCollector;
import org.ascape.util.data.StatCollectorCSAMM;
import org.ascape.util.data.StatCollectorCondCSAMM;
import org.ascape.view.vis.ChartView;
import org.ascape.view.vis.Overhead2DView;

public class SimulateSocialExchange extends Scape {
	
	private static final long serialVersionUID = 7635357833244954507L;
	private Scape socialSpace;
	private Scape people;
	private Scape stock;
	private Ibex35 myStock;
	
	private List<Message> popularMessages[] = new ArrayList[1];
	private List<Investors> sortInvestorByFinance;
	private List<Investors> sortInvestorByActivity;
	
	public int nInvestors = Properties.NUM_INVESTORS;
	private int nBolsas = 1;	
	
	Overhead2DView latticeView;
	
	@SuppressWarnings("serial")
	public void createScape() {
        super.createScape();
        
        for(int i = 0; i < popularMessages.length; i++)
        	popularMessages[i] = new ArrayList<Message>();
        
        socialSpace = new Scape(new Array2DVonNeumann());
        socialSpace.setPrototypeAgent(new HostCell());
        socialSpace.setName("Web Site");
        socialSpace.setExtent(50, 50);  //spaceSocial.setExtent(30, 30);            
        socialSpace.setExecutionOrder(Scape.RULE_ORDER);
        
        myStock = new Ibex35();
        myStock.setHostScape(socialSpace);
        stock = new Scape(new Array1D(), "Bolsa- ibex35", myStock);    
        stock.setExecutionOrder(Scape.RULE_ORDER);
        
        Investors miInversor = new Investors();        
        miInversor.setHostScape(socialSpace);
        people = new Scape();
        people.setName("Investors");
        people.setPrototypeAgent(miInversor);
        people.setExecutionOrder(Scape.RULE_ORDER);        
	    
        add(socialSpace);
        add(stock);	
        add(people); 
        
        Rule playInStock = new Rule("Invest in stock") {
	        private static final long serialVersionUID = 665608531104091849L;
	
	        public void execute(Agent a) {
	        	((Investors) a).playInStock(myStock);
	        }
	    };	    
	    people.addRule(playInStock);			
		Rule chooseInvestor = new Rule("Choose the type of the investor") {	        
	        private static final long serialVersionUID = 66560843110409183L;	
	        public void execute(Agent a) {
	        	((Investors) a).defineInvestor();
	        }
	    };	    
	    people.addInitialRule(chooseInvestor);
	    Rule readAndCommentNeighbors = new Rule("Reading, commenting, scoring to his neighbours") {	        
	        private static final long serialVersionUID = 66560843110409123L;	
	        public void execute(Agent a) {
	        	((Investors) a).chooseNeighborToPlay();
	        }
	    };	    
	    people.addRule(readAndCommentNeighbors);
	    
	    
       
        StatCollector FinRepEXP = new StatCollectorCondCSAMM("FinRepEXP") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR);
            }
        };
        StatCollector FinRepEPerY = new StatCollectorCondCSAMM("FinRep EXP Per Y") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).perception == true);
            }
        };
        StatCollector FinRepEPerN = new StatCollectorCondCSAMM("FinRep EXP Per N") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).perception == false);
            }
        };
        StatCollector FinRepEAnxY = new StatCollectorCondCSAMM("FinRep EXP Anx Y") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).anxiety == true);
            }
        };
        StatCollector FinRepEAnxN = new StatCollectorCondCSAMM("FinRep EXP Anx N") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).anxiety == false);
            }
        };
        StatCollector FinRepEDivY = new StatCollectorCondCSAMM("FinRep EXP Div Y") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).isDiversifier == true);
            }
        };
        StatCollector FinRepEDivN = new StatCollectorCondCSAMM("FinRep EXP Div N") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).isDiversifier == false);
            }
        };        
        StatCollector FinRepEMenY = new StatCollectorCondCSAMM("FinRep EXP Alcista") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).memory == true);
            }
        };
        StatCollector FinRepEMenN = new StatCollectorCondCSAMM("FinRep EXP Bajista") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).memory == false);
            }
        }; 
        StatCollector FinRepEImpY = new StatCollectorCondCSAMM("FinRep EXP Imp Y") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).impulsive == true);
            }
        };
        StatCollector FinRepEImpN = new StatCollectorCondCSAMM("FinRep EXP Imp N") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR &&
                		((IntelligentInvestor)((Investors)object).getInvestor()).impulsive == false);
            }
        };
        StatCollector FinRepAMA = new StatCollectorCondCSAMM("FinRepAMA") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.AMATEUR_INVESTOR);
            }
        };
        StatCollector FinRepRAM = new StatCollectorCondCSAMM("FinRepRAM") {
            public double getValue(Object object) {
               return ((Investors) object).getInvestor().getFinancialReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[0] == Investors.RANDOM_INVESTOR);
            }
        };
        
        StatCollector ActRepFRE_GOW_NFRIE = new StatCollectorCondCSAMM("ActRep FREQ GOWR NO_FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[1] == Investors.FREQUENT_USER &&
                		((Investors) object).getAgentType()[2] == Investors.GOOD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.NO_FRIENDLY_USER);
            }
        };
        StatCollector ActRepFRE_GOW_FRIE = new StatCollectorCondCSAMM("ActRep FREQ GOWR FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.FREQUENT_USER &&
                		((Investors) object).getAgentType()[2] == Investors.GOOD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.FRIENDLY_USER);
            }
        };
        StatCollector ActRepOCA_GOW_FRIE = new StatCollectorCondCSAMM("ActRep OCAS GOWR FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.OCASIONAL_USER &&
                		((Investors) object).getAgentType()[2] == Investors.GOOD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.FRIENDLY_USER);
            }
        };
        StatCollector ActRepOCA_GOW_NFRIE = new StatCollectorCondCSAMM("ActRep OCAS GOWR NO_FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.OCASIONAL_USER &&
                		((Investors) object).getAgentType()[2] == Investors.GOOD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.NO_FRIENDLY_USER);
            }
        };
        StatCollector ActRepFRE_BAW_NFRIE = new StatCollectorCondCSAMM("ActRep FREQ BADWR NO_FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
                return (((Investors) object).getAgentType()[1] == Investors.FREQUENT_USER &&
                		((Investors) object).getAgentType()[2] == Investors.BAD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.NO_FRIENDLY_USER);
            }
        };
        StatCollector ActRepFRE_BAW_FRIE = new StatCollectorCondCSAMM("ActRep FREQ BADWR FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.FREQUENT_USER &&
                		((Investors) object).getAgentType()[2] == Investors.BAD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.FRIENDLY_USER);
            }
        };
        StatCollector ActRepOCA_BAW_FRIE = new StatCollectorCondCSAMM("ActRep OCAS BADWR FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.OCASIONAL_USER &&
                		((Investors) object).getAgentType()[2] == Investors.BAD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.FRIENDLY_USER);
            }
        };
        StatCollector ActRepOCA_BAW_NFRIE = new StatCollectorCondCSAMM("ActRep OCAS BADWR NO_FRIEN") {
            public double getValue(Object object) {
               return ((Investors) object).getActivityReputation();
            }
            public boolean meetsCondition(Object object) {
            	return (((Investors) object).getAgentType()[1] == Investors.OCASIONAL_USER &&
                		((Investors) object).getAgentType()[2] == Investors.BAD_WRITER &&
                		((Investors) object).getAgentType()[3] == Investors.NO_FRIENDLY_USER);
            }
        };
        
        StatCollector Top10_PRU = new StatCollectorCSAMM("Prudent Investors") {
            public double getValue(Object object) {
            	int sum = 0;
            	Scape scape  = (Scape)object;
            	if(scape.getName().equalsIgnoreCase("Investors")) {
            		if(((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance() == null)
	            		return 0;	            	
            		List<Investors> investors = ((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance();
	            	for(int i = investors.size()-1; i >= investors.size()-10; i--) {
	            		Investors investor = investors.get(i);
	            		if(investor.getAgentType()[0] == Investors.EXPERIMENTED_INVESTOR)
	            			sum++;
	            	}
            	}            		
	            return sum;
            }           
        };
        StatCollector Top10_AMA = new StatCollectorCSAMM("Amateur Investors") {
            public double getValue(Object object) {
            	int sum = 0;
            	Scape scape  = (Scape)object;
            	if(scape.getName().equalsIgnoreCase("Investors")) {
            		if(((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance() == null)
	            		return 0;	            	
            		List<Investors> investors = ((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance();
	            	for(int i = investors.size()-1; i >= investors.size()-10; i--) {
	            		Investors investor = investors.get(i);
	            		if(investor.getAgentType()[0] == Investors.AMATEUR_INVESTOR)
	            			sum++;
	            	}
            	}            		
	            return sum;
            }           
        };
        StatCollector Top10_RAM = new StatCollectorCSAMM("Random Investors") {
            public double getValue(Object object) {
            	int sum = 0;
            	Scape scape  = (Scape)object;
            	if(scape.getName().equalsIgnoreCase("Investors")) {
            		if(((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance() == null)
	            		return 0;
            		List<Investors> investors = ((SimulateSocialExchange)scape.getRoot()).getSortInvestorByFinance();
	            	for(int i = investors.size()-1; i >= investors.size()-10; i--) {
	            		Investors investor = investors.get(i);
	            		if(investor.getAgentType()[0] == Investors.RANDOM_INVESTOR)
	            			sum++;
	            	}
            	}            		
	            return sum;
            }           
        };

        StatCollector TotalPlay = new StatCollectorCSAMM("TotalPlay") {
            public double getValue(Object object) {
            	return ((Investors)object).getPlay();
            }        
        };
        
        StatCollector Top10_Play = new StatCollectorCondCSAMM("Top10_Play") {
            public double getValue(Object object) {
            	return ((Investors)object).getPlay();
            }
            public boolean meetsCondition(Object object) {
            	List<Investors> investors = getSortInvestorByFinance();
            	if(investors == null)
            		return false;
            	int size = investors.size();
            	for(int i = size-1; i >= size-10; i--) {
            		if(object == investors.get(i))
            			return true;
            	}
            	return false;
            }
        };
        
        StatCollector Bottom10_Play = new StatCollectorCondCSAMM("Bottom10_Play") {
            public double getValue(Object object) {
            	return ((Investors)object).getPlay();
            }
            public boolean meetsCondition(Object object) {
            	List<Investors> investors = getSortInvestorByFinance();
            	if(investors == null)
            		return false;
            	for(int i = 0; i < 10; i++) {
            		if(object == investors.get(i))
            			return true;
            	}
            	return false;
            }
        };
        
        StatCollector Telefonica = new StatCollectorCSAMM("Telefonica") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("Telefonica");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector Cepsa = new StatCollectorCSAMM("Cepsa") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("Cepsa");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector Santander = new StatCollectorCSAMM("Santander") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("Santander");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector BBVA = new StatCollectorCSAMM("BBVA") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("BBVA");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector Endesa = new StatCollectorCSAMM("Endesa") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("Endesa");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector Iberdrola = new StatCollectorCSAMM("Iberdrola") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("Iberdrola");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector GasNatural = new StatCollectorCSAMM("GasNatural") {
            public double getValue(Object object) {
            	Share share = getStock().getShares().get("GasNatural");
            	if(share == null)
            		return 0;
            	else {
            		return share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            }           
        };
        StatCollector TotalStock = new StatCollectorCSAMM("TotalStock") {
            public double getValue(Object object) {
            	if(getStock().getShares().size() == 0)
            		return 0;
            	Double total = 0.0;
            	for(Share share : getStock().getShares().values()) {
            		total += share.getVariationsHistory().get(share.getVariationsHistory().size()-1);
            	}
            	return total/getStock().getShares().size();
            }           
        };
        
        
        /*ActRepFRE_GOW_NFRIE.setAutoCollect(false);
        ActRepFRE_GOW_FRIE.setAutoCollect(false);
        ActRepOCA_GOW_NFRIE.setAutoCollect(false);
        ActRepOCA_GOW_FRIE.setAutoCollect(false);
        ActRepFRE_BAW_NFRIE.setAutoCollect(false);
        ActRepFRE_BAW_FRIE.setAutoCollect(false);
        ActRepOCA_BAW_FRIE.setAutoCollect(false);        
        ActRepOCA_BAW_NFRIE.setAutoCollect(false);*/
        
        people.addStatCollector(FinRepEXP);
        people.addStatCollector(FinRepEPerY);
        people.addStatCollector(FinRepEPerN);
        people.addStatCollector(FinRepEAnxY);
        people.addStatCollector(FinRepEAnxN);
        people.addStatCollector(FinRepEDivY);
        people.addStatCollector(FinRepEDivN);
        people.addStatCollector(FinRepEMenY);
        people.addStatCollector(FinRepEMenN);
        people.addStatCollector(FinRepEImpY);
        people.addStatCollector(FinRepEImpN);
        people.addStatCollector(FinRepAMA);        
        people.addStatCollector(FinRepRAM);        
        
        people.addStatCollector(ActRepFRE_GOW_NFRIE);
        people.addStatCollector(ActRepFRE_GOW_FRIE);
        people.addStatCollector(ActRepOCA_GOW_NFRIE);
        people.addStatCollector(ActRepOCA_GOW_FRIE);
        people.addStatCollector(ActRepFRE_BAW_NFRIE);
        people.addStatCollector(ActRepFRE_BAW_FRIE);
        people.addStatCollector(ActRepOCA_BAW_NFRIE);
        people.addStatCollector(ActRepOCA_BAW_FRIE); 
        
        people.addStatCollector(TotalPlay);
        people.addStatCollector(Top10_Play);
        people.addStatCollector(Bottom10_Play);
        
        addStatCollector(Top10_PRU);
        addStatCollector(Top10_AMA);
        addStatCollector(Top10_RAM);
        
        stock.addStatCollector(Telefonica);
        stock.addStatCollector(Cepsa);
        stock.addStatCollector(Santander);
        stock.addStatCollector(BBVA);
        stock.addStatCollector(Iberdrola);
        stock.addStatCollector(Endesa);
        stock.addStatCollector(GasNatural);
        stock.addStatCollector(TotalStock);
	}
	
	public void scapeSetup(ScapeEvent scapeEvent) {
		((Scape) stock).setExtent(nBolsas);
        ((Scape) people).setExtent(getnInversores());        
    }

    // create views and charts
    public void createGraphicViews() {
        super.createGraphicViews();
        
        latticeView = new Overhead2DView();
        latticeView.setCellSize(7); //15
        //latticeView.setDrawNetwork(true);
        socialSpace.addView(latticeView); 
        
        ChartView financialIntelligentChart = new ChartView("Intelligent Reputation");
        people.addView(financialIntelligentChart);
        financialIntelligentChart.addSeries("Average FinRepEXP", Color.RED);
        financialIntelligentChart.addSeries("Average FinRep EXP Per Y", Color.BLACK);
        financialIntelligentChart.addSeries("Average FinRep EXP Per N", Color.CYAN);
        financialIntelligentChart.addSeries("Average FinRep EXP Anx Y", Color.YELLOW);
        financialIntelligentChart.addSeries("Average FinRep EXP Anx N", Color.BLUE);
        financialIntelligentChart.addSeries("Average FinRep EXP Div Y", Color.MAGENTA); //.orange);
        financialIntelligentChart.addSeries("Average FinRep EXP Div N", Color.PINK);
        financialIntelligentChart.addSeries("Average FinRep EXP Alcista", Color.GRAY);
        financialIntelligentChart.addSeries("Average FinRep EXP Bajista", Color.GREEN);
        financialIntelligentChart.setDisplayPoints(10000);
        
        ChartView financialIMPIntelligentChart = new ChartView("Impulsive Intelligent Reputation");
        people.addView(financialIMPIntelligentChart);
        financialIMPIntelligentChart.addSeries("Average FinRep EXP Imp Y", Color.BLACK);
        financialIMPIntelligentChart.addSeries("Average FinRep EXP Imp N", Color.RED);
         
        ChartView financialANXIntelligentChart = new ChartView("Anxiety Intelligent Reputation");
        people.addView(financialANXIntelligentChart);
        financialANXIntelligentChart.addSeries("Average FinRep EXP Anx Y", Color.BLACK);
        financialANXIntelligentChart.addSeries("Average FinRep EXP Anx N", Color.RED);
        
        ChartView financialMEMIntelligentChart = new ChartView("Memory Intelligent Reputation");
        people.addView(financialMEMIntelligentChart);
        financialMEMIntelligentChart.addSeries("Average FinRep EXP Alcista", Color.BLACK);
        financialMEMIntelligentChart.addSeries("Average FinRep EXP Bajista", Color.RED);
        
        ChartView financialPERIntelligentChart = new ChartView("Perception Intelligent Reputation");
        people.addView(financialPERIntelligentChart);
        financialPERIntelligentChart.addSeries("Average FinRep EXP Per Y", Color.BLACK);
        financialPERIntelligentChart.addSeries("Average FinRep EXP Per N", Color.RED);
        
        ChartView financialDIVIntelligentChart = new ChartView("Diversifier Intelligent Reputation");
        people.addView(financialDIVIntelligentChart);
        financialDIVIntelligentChart.addSeries("Average FinRep EXP Div Y", Color.BLACK);
        financialDIVIntelligentChart.addSeries("Average FinRep EXP Div N", Color.RED);
        
        ChartView financialChart = new ChartView("Financial Reputation");
        people.addView(financialChart);
        financialChart.addSeries("Average FinRepEXP", Color.RED);
        /*financialChart.addSeries("Average FinRep EXP Per Y", Color.BLACK);
        financialChart.addSeries("Average FinRep EXP Per N", Color.CYAN);
        financialChart.addSeries("Average FinRep EXP Anx Y", Color.YELLOW);
        financialChart.addSeries("Average FinRep EXP Anx N", Color.GRAY);
        financialChart.addSeries("Average FinRep EXP Div Y", Color.MAGENTA);
        financialChart.addSeries("Average FinRep EXP Div N", Color.PINK);
        financialChart.addSeries("Average FinRep EXP Alcista", Color.DARK_GRAY);
        financialChart.addSeries("Average FinRep EXP Bajista", Color.ORANGE);*/
        financialChart.addSeries("Average FinRepAMA", Color.BLUE);
        financialChart.addSeries("Average FinRepRAM", Color.GREEN);        
        financialChart.setDisplayPoints(10000);
        
        ChartView playChart = new ChartView("Play Iteraction Number");
        people.addView(playChart);
        playChart.addSeries("Average TotalPlay", Color.RED);
        playChart.addSeries("Average Top10_Play", Color.BLUE);
        playChart.addSeries("Average Bottom10_Play", Color.GREEN);
        
        //System.out.println("componets:"+financialChart.getPanel().getRootPane().getComponents()+", "
        //		+financialChart.getPanel().getComponentPopupMenu()+"  -  "+
        //		financialChart.getPanel().getRootPane().getParent().getComponents());
        
        ChartView activityChart = new ChartView("Activity Reputation");
        people.addView(activityChart);
        activityChart.addSeries("Average ActRep FREQ GOWR NO_FRIEN", Color.red);
        activityChart.addSeries("Average ActRep FREQ GOWR FRIEN", Color.black);
        activityChart.addSeries("Average ActRep OCAS GOWR NO_FRIEN", Color.cyan);
        activityChart.addSeries("Average ActRep OCAS GOWR FRIEN", Color.YELLOW);
        activityChart.addSeries("Average ActRep FREQ BADWR NO_FRIEN", Color.magenta); //.orange);
        activityChart.addSeries("Average ActRep FREQ BADWR FRIEN", Color.PINK);
        activityChart.addSeries("Average ActRep OCAS BADWR NO_FRIEN", Color.blue);
        activityChart.addSeries("Average ActRep OCAS BADWR FRIEN", Color.green);
        activityChart.setDisplayPoints(3000);
        
        ChartView top10Chart = new ChartView("Top10");
        addView(top10Chart);
        top10Chart.addSeries("Maximum Prudent Investors", Color.red);
        top10Chart.addSeries("Maximum Amateur Investors", Color.blue);
        top10Chart.addSeries("Maximum Random Investors", Color.green);
        
        ChartView stockChart = new ChartView("Stock Variation");
        stock.addView(stockChart);
        //stockChart.addSeries("Maximum Telefonica", Color.red);
        //stockChart.addSeries("Maximum Cepsa", Color.black);
        //stockChart.addSeries("Maximum Santander", Color.cyan);
        //stockChart.addSeries("Maximum BBVA", Color.YELLOW);
        //stockChart.addSeries("Maximum Iberdrola", Color.magenta);
        //stockChart.addSeries("Maximum Endesa", Color.PINK);
        // stockChart.addSeries("Maximum GasNatural", Color.blue);
        stockChart.addSeries("Maximum TotalStock", Color.BLACK);
        
        financialChart.setIterationsPerRedraw(Properties.REPUTATION_INTERVAL);
        activityChart.setIterationsPerRedraw(Properties.REPUTATION_INTERVAL);
        top10Chart.setIterationsPerRedraw(Properties.REPUTATION_INTERVAL);
        /*ChartView activityChart1 = new ChartView(ChartView.HISTOGRAM);
        people.addView(activityChart1);
        activityChart1.addSeries("Average FinRepEXP");
        activityChart1.addSeries("Average FinRepAMA");
        activityChart1.addSeries("Average FinRepRAM");
        activityChart1.setDisplayPoints(300);*/
    }

	public void setnInversores(int nInversores) {
		this.nInvestors = nInversores;
	}

	public List<Message>[] getPopularMessages() {
		return popularMessages;
	}
	
	public void sortAddMessages(Message message) {
		for(int i = 0; i < popularMessages.length; i++) {
			boolean sorted = false;
			for(int j = 0; j < popularMessages[i].size(); j++) {			
				if(message.getReputation()[i] <= popularMessages[i].get(j).getReputation()[i]) {
					popularMessages[i].add(j, message);
					sorted=true;
					break;
				}
			}
			if(!sorted)
				popularMessages[i].add(message);
		}
	}
	
	public int getnInversores() {
		return nInvestors;
	}
	
	public Ibex35 getStock(){
		return myStock;
	}
	
	public Scape getPeople() {
		return people;
	}

	public List<Investors> getSortInvestorByFinance() {
		return sortInvestorByFinance;
	}

	public void setSortInvestorByFinance(List<Investors> sortInvestorByFinance) {
		this.sortInvestorByFinance = sortInvestorByFinance;
	}

	public List<Investors> getSortInvestorByActivity() {
		return sortInvestorByActivity;
	}

	public void setSortInvestorByActivity(List<Investors> sortInvestorByActivity) {
		this.sortInvestorByActivity = sortInvestorByActivity;
	}	
	
}
