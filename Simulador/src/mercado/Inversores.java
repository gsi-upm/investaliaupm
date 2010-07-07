package mercado;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ascape.model.CellOccupant;
import org.ascape.model.HostCell;
import org.ascape.model.Agent;
import org.ascape.model.Scape;

public class Inversores extends CellOccupant {
	/*
	 * Common of all investor. I think already I don't need it.
	 */
	protected static int iteraciones = 0;
	private static int time = 0;
	protected static boolean esPrimero = true;
	protected static int cuenta = 0;
	protected static int setId = 1;
	private static int EXPERIMENTED_INVESTOR = 0;
	private static int AMATEUR_INVESTOR = 1;
	private static int FREQUENT_USER = 0;
	private static int OCASIONAL_USER = 1;
	private static int GOOD_WRITER = 0;
	private static int BAD_WRITER = 1;
	
	private int tipoAgente[];
	private int id;
	protected int miIteracion = 0;
	protected Color myColor;
	private int played = 0;
	private double capital;	
	protected ArrayList<Mensaje> misMensajes;
	protected ArrayList<Accion> misAcciones;
	
	private int messageHistory[];
	private int readerHistory[];
	private int uniqueReaderHistory[];
	private int followerHistory[];
	private int uniqueFollowerHistory[];
	
	private double liquidez;
	// The limit of money that one investor can invest
	private double maxValorCompra;
	// activity factor
	private double actividadComprar;
	private double actividadVender;
	//profitability
	private double rentabilidadVenta;
	private double rentabilidadCompra;
	//messages and comments probabilities:
	private double probabilidadLeer;
	private double probabilidadComentar;
	private double probabilidadPostear;
	//good message probability
	private double probabilidadBuenMensaje;
	
	//reputations:
	private double popularity = 0;
	private double financialReputation = 0;
	private int globalLiquidity = 0;
	private double globalReturn = 0;
	private double activityReputation[];
	
	/* Thresholds to sell (buy). If a share get down (up) a number of
	 * iteracionesVenta (iteracionesCompra) the investor makes a decision 
	 */
	private int iteracionesVenta;
	private int iteraccionesCompra;
	
	
	//create the list of rules for scape
	public void scapeCreated() {
		getScape().addInitialRule(INITIALIZE_RULE);
		getScape().addInitialRule(MOVE_RANDOM_LOCATION_RULE);
		//method update
		getScape().addRule(ITERATE_RULE);
		getScape().addRule(UPDATE_RULE);
		getScape().addRule(RANDOM_WALK_RULE);
		//getScape().addRule(PLAY_RANDOM_NEIGHBOR_RULE);
	}

	/*
	 * One way or other is right. You can define the rule in SimulateSocialExchange class
	 * or here with method initialize
	 */
	
    public void eligeInversor() {
    	setId(setId++);
    	misMensajes = new ArrayList<Mensaje>();
    	misAcciones = new ArrayList<Accion>();
    	tipoAgente = new int[3];
    	activityReputation = new double[2];
    	messageHistory = new int[]{0,0};
    	readerHistory = new int[]{0,0};
    	uniqueReaderHistory = new int[]{0,0};
    	followerHistory = new int[]{0,0};
    	uniqueFollowerHistory = new int[]{0,0};
    	//Investor:
        if (randomInRange(0, 1) > 0) {
            //setColor(Color.darkGray);
            tipoAgente[0] = EXPERIMENTED_INVESTOR;
            //aggressive investor
            iteraccionesCompra = 3;
            iteracionesVenta = 2;
            setLiquidez(randomInRange(3000,10000));
            maxValorCompra = getLiquidez()*0.7;
            actividadComprar = 0.6;
            actividadVender = 0.7;
            //if one share along the movements decrease his value in 15% you might buy
            rentabilidadCompra = -0.15;
            //if one share along the movements increase his value in 10% you may sell
            rentabilidadVenta = 0.1;         
        } else {
        	tipoAgente[0] = AMATEUR_INVESTOR;
            //setColor(Color.magenta);
            //calm investor
            iteraccionesCompra = 4;
            iteracionesVenta = 5;
            setLiquidez(randomInRange(1000,3000));
            maxValorCompra = getLiquidez()*0.4;
            actividadComprar = 0.6;
            actividadVender = 0.4;
            //if one share along the movements decrease his value in 22% you might buy
            rentabilidadCompra = -0.22;
            //if one share along the movements increase his value in 17% you may sell
            rentabilidadVenta = 0.17;
        }
        //Activity of the user:
        if(randomInRange(0, 1) > 0) {
        	tipoAgente[1] = FREQUENT_USER;
        	probabilidadLeer = randomInRange(0.6,0.9);
        	//probabilidadComentar = randomInRange(0.6,0.8);
        	probabilidadComentar = randomInRange(0.2,0.36);
        	probabilidadPostear = randomInRange(0.08,0.14);
        } else {
        	tipoAgente[1] = OCASIONAL_USER;
        	probabilidadLeer = randomInRange(0.05,0.3);
        	//probabilidadComentar = randomInRange(0.05,0.2);
        	probabilidadComentar = randomInRange(0.03,0.08);
        	probabilidadPostear = randomInRange(0.01,0.04);
        }
        //Good o bad writer?
        if(randomInRange(0, 1) > 0) {
        	tipoAgente[2] = GOOD_WRITER;
        	probabilidadBuenMensaje = randomInRange(0.7,0.9);
        } else {
        	tipoAgente[2] = BAD_WRITER;
        	probabilidadBuenMensaje = randomInRange(0,0.2);
        }
        setColorByAgentType();
    }
   
    private void setColorByAgentType () {
    	float red;
    	float green;
    	float blue;
    	if(tipoAgente[0] == EXPERIMENTED_INVESTOR) {
    		red = 0;
    	} else {
    		red = 1;
    	}
    	if(tipoAgente[1] == FREQUENT_USER) {
    		green = 0;
    	} else {
    		green = 0.8f;
    	}
    	if(tipoAgente[2] == GOOD_WRITER) {
    		blue = 0;
    	} else {
    		blue = 1;
    	}
    	setColor(new Color(red, green, blue));	
    }
        
	public void iterate(){
		// Ascape must just call it one time per iteration of the first agent...
		/*
		 * I think already I don't need it.
		 */
		if ((iteraciones % ((SimulateSocialExchange)getRoot()).getnInversores() == 0) 
				&& esPrimero ){
			esPrimero = false;
			time++;
			//Generate popularity and reputation of investor´messages
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++)
				((SimulateSocialExchange)getRoot()).getPopularMessages()[i].clear();
			for(Object cell : scape) {
				if(cell instanceof Inversores) {
					for(Mensaje mensaje : ((Inversores)cell).getMensajes()) {
						if(mensaje.generateReputation(time) < Properties.MAX_DIFFERENCE_CLUSTERS) {
							((SimulateSocialExchange)getRoot()).sortAddMessages(mensaje);
						}
					}
				}
			}
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++) {
				int sizePopularMessages = ((SimulateSocialExchange)getRoot()).getPopularMessages()[i].size();
				int initMessage = (int) Math.ceil(sizePopularMessages/Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR);
				for(int j = initMessage; j < sizePopularMessages; j++) {
					//((SimulateSocialExchange)getRoot()).getPopularMessages().get(i).setPopularity
					//	((double)i/sizePopularMessages*Mensaje.POPULARITY_INCREMENTATION_LINEAL_FACTOR);
					((SimulateSocialExchange)getRoot()).getPopularMessages()[i].get(j).setPopularity
						(i,Math.pow(Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR, 
						 j*Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR/sizePopularMessages-1));
				}
			}
			if(time % Properties.STATISTICS_INTERVAL == 1) { //Generate reputation statistics
				System.out.println("Estadistica en intervalo "+time+":");
				for(Object cell : scape) {
					if(cell instanceof Inversores)
						((Inversores)cell).generateActivityReputation();
				}
				List<Inversores> sortInvestorByFinance = sortByFinancialReputation(scape);
				for(int i = 0; i < sortInvestorByFinance.size(); i++) {
					Inversores cell = sortInvestorByFinance.get(i);
					System.out.println("  id:" + cell.getId() + cell.getAgentTypeToString() + " with financial reputation:" +
							cell.getGlobal_return() );					
				}
				
				double reputationByAgentType[][][] = new double[2][2][2 + 5];
				long messageStatistics[][] = {{0,0,0,0,0}, {0,0,0,0,0}};
				List<Inversores> sortInvestorByMessage = sortByMessageReputation(scape);
				for(int i = 0; i < sortInvestorByMessage.size(); i++) {
					Inversores cell = sortInvestorByMessage.get(i);
					System.out.print("  id:" + cell.getId() + cell.getAgentTypeToString() + " with messages[");
					int statistics[][] = cell.messageStatistics();
					int historyStatistics[][] = cell.historyMessageStatistics();
					for(int j = 0; j < statistics.length; j++) {
						for(int k = 0; k < statistics[j].length; k++) {
							messageStatistics[j][k] += statistics[j][k] + historyStatistics[j][k];
							System.out.print(statistics[j][k]+"-");
						}
						System.out.print(";");
					}
					System.out.print("],play:" + cell.getPlayed() + ", activ rep:");
					for(int j = 0; j < cell.getActivityReputation().length; j++)
						System.out.print(cell.getActivityReputation()[j]+",");
					System.out.println();
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][0]++;
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][1] += cell.getPlayed();
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][2] += cell.getNumMensajes() + cell.getMessagesHistory();
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][3] += cell.probabilidadPostear;
					reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][4] += cell.probabilidadBuenMensaje;
					for(int j = 0; j < cell.getActivityReputation().length; j++) {
						reputationByAgentType[cell.getTipoAgente()[1]][cell.getTipoAgente()[2]][j+5] += cell.getActivityReputation()[j];
					}
				}
				for(int i = 0; i < reputationByAgentType.length; i++) {
					for(int j = 0; j < reputationByAgentType[i].length; j++) {
						printReputationByAgentType(i,j,reputationByAgentType[i][j]);
					}
				}
				printMessageStatistics(messageStatistics);
				//Clean messages users'history of many time ago
				if(time % Properties.CLEAN_INTERVAL == 1) {
					for(Object cell : scape) {
						if(cell instanceof Inversores) {							
							((Inversores)cell).cleanMessages(time - Properties.MESSAGE_TIME_TO_CLEAN);
						}
					}
				}
			}		
		}
		cuenta++;
		if (cuenta == ((SimulateSocialExchange)getRoot()).getnInversores()){
			cuenta = 0;
			esPrimero = true;
		}
	}
	
	public void printReputationByAgentType (int activityType, int writerType, double reputation[]) {
		String agentType = "[";		
		if(activityType == FREQUENT_USER)
			agentType += "FRE_US";
		else
			agentType += "OCA_US";
		agentType += ":"+String.format("%.3f", reputation[3]/reputation[0])+",";
		if(writerType == GOOD_WRITER)
			agentType += "GOOD_WR";
		else
			agentType += "BAD_WRI";
		agentType += ":"+String.format("%.3f", reputation[4]/reputation[0])+"]";
		System.out.print("  " + agentType + " size:" + reputation[0] + " play:" +
				String.format("%.2f", reputation[1]/reputation[0]) + " mes:" +
				String.format("%.1f", reputation[2]/reputation[0]) + " activity rep:");
		for(int i = 5; i < reputation.length; i++)
			System.out.print(reputation[i]/reputation[0]+",");
		System.out.println();
	}
	
	public void printMessageStatistics(long messageStatistics[][]) {
		String messageString = "  Good Messages:" + messageStatistics[0][0] + ", R:" + messageStatistics[0][1] + ", uniqR:" + 
			messageStatistics[0][2];
		System.out.println(messageString);
		messageString = "  Bad Messages:" + messageStatistics[1][0] + ", R:" + messageStatistics[1][1] + ", uniqR:" + 
			messageStatistics[1][2];
		System.out.println(messageString);
	}
	
	public int[][] messageStatistics() {
		int statistics[][] = new int[2][5];
		statistics[0][0] = 0; statistics[0][1] = 0; statistics[0][2] = 0; statistics[0][3] = 0; statistics[0][4] = 0;
		statistics[1][0] = 0; statistics[1][1] = 0; statistics[1][2] = 0; statistics[1][3] = 0; statistics[1][4] = 0;
		int max_time = Properties.TIME_CLUSTER * Properties.MAX_DIFFERENCE_CLUSTERS;
		for(Mensaje mensaje : getMensajes()) {
			if((time-mensaje.getDate()) < max_time) {
				if(mensaje.isGood()) {
					statistics[0][0]++;
					statistics[0][1] += mensaje.getNumReaders();
					statistics[0][2] += mensaje.getUniqueNumReaders();
					statistics[0][3] += mensaje.getNumFollowers();
					statistics[0][4] += mensaje.getUniqueNumFollowers();
				} else {
					statistics[1][0]++;
					statistics[1][1] += mensaje.getNumReaders();
					statistics[1][2] += mensaje.getUniqueNumReaders();
					statistics[1][3] += mensaje.getNumFollowers();
					statistics[1][4] += mensaje.getUniqueNumFollowers();
				}
			}
		}
		return statistics;
	}
	
	public int[][] historyMessageStatistics() {
		int statistics[][] = {{messageHistory[0],readerHistory[0],uniqueReaderHistory[0],
			followerHistory[0],uniqueFollowerHistory[0]}, {messageHistory[1],readerHistory[1], 
			uniqueReaderHistory[1],followerHistory[1],uniqueFollowerHistory[1]}};
		return statistics;
	}
	
	private void cleanMessages (int timeLimit) {
		for(int i = 0; i < misMensajes.size(); i++) {
			Mensaje message = misMensajes.get(i);
			if(message.getDate() < timeLimit) {
				if(message.isGood()) {
					messageHistory[0]++;
					readerHistory[0] += message.getNumReaders();
					uniqueReaderHistory[0] += message.getUniqueNumReaders();
					followerHistory[0] += message.getNumReaders();
					uniqueFollowerHistory[0] += message.getUniqueNumFollowers();
				} else {
					messageHistory[1]++;
					readerHistory[1] += message.getNumReaders();
					uniqueReaderHistory[1] += message.getUniqueNumReaders();
					followerHistory[1] += message.getNumReaders();
					uniqueFollowerHistory[1] += message.getUniqueNumFollowers();
				}
				misMensajes.remove(i);				
				i--;
			}
			else
				return;
		}
	}
	
	private List<Inversores> sortByMessageReputation(Scape agentsList) {
		List<Inversores> sortList = new ArrayList<Inversores>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Inversores) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Inversores)cell).getActivityReputation()[0] >= sortList.get(i).getActivityReputation()[0]) {
						sortList.add(i, (Inversores)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Inversores)cell);
			}
		}
		return sortList;
	}
	
	private List<Inversores> sortByFinancialReputation(Scape agentsList) {
		List<Inversores> sortList = new ArrayList<Inversores>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Inversores) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Inversores)cell).getGlobal_return() >= sortList.get(i).getGlobal_return()) {
						sortList.add(i, (Inversores)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Inversores)cell);
			}
		}
		return sortList;
	}
	
	public void update(){
		iteraciones++;
		miIteracion++;
		//String info = this.toString();
		//System.out.println("id :" + getId() + ", soy:" + info + " , numero:" + miIteracion);
		
		//post in own page.
		if(randomInRange(0,1.0) < probabilidadPostear){
			postear();
		}
		//update thresholds
	}
	
	public void chooseNeighborToPlay() {
		List<Inversores> neighbors = findWithin(Properties.NEIGHBOR_DISTANCE_TO_PLAY);
		for(Inversores neighbor : neighbors) {
			double probabilityToPlay = Math.pow(Properties.NEIGHBOR_DISTANCE_EXPONENCIAL_DEGRADATION,
					calculateDistance(neighbor)) * probabilidadLeer;
			if(randomInRange(0.0,1.0) < probabilityToPlay)
				play(neighbor);
			//System.out.println("("+ time +") id :" + getId() + ",pob:" + probabilityToPlay + " cal:" + calculateDistance(neighbor));
		}
		//System.out.println("("+ time +") id :" + getId() + ", played:" + played + " " + neighbors.size());
	}
	
	/**
	 *  Interactuamos con la bolsa
	 * @param miBolsa
	 */
	public void jugarEnBolsa(Ibex35 miBolsa){
		//posiblidad de si entra en vender, la probabilidad de entrar en comprar sea menor 
		//y al reves

		//cojo todas las acciones de la bolsa

		HashMap<String, Acciones> accionesDeBolsa = miBolsa.getAcciones();

		//vender
		if (randomInRange(0,1)> actividadVender){
			//elegir empresa antes?, ver la mejor*probabilidad de vender??			
			//For each share of exchange, I have look for my shares checking up on which I have it
			//and checking up on the profitability. If it is good, sell the share.			
			for(int id = 0; id < misAcciones.size(); id++) {
				Accion miAccion = misAcciones.get(id);
				Acciones accionesBolsa = accionesDeBolsa.get(miAccion.getIdCompany());
				/*
				 *  I have the share, I have to check if is a good moment to
				 *  sell the share. look the movements. 
				 *  If the sum of last movements is better than the profitability sell it.
				*/
				double rentabilidad = 0;
				ArrayList<Double> historico = accionesBolsa.getHistoricoAccion();
				
				for ( int i = iteracionesVenta-1; i<historico.size(); i++) {
					rentabilidad += historico.get(i);
				}
				if (rentabilidad > rentabilidadVenta && 
						accionesBolsa.getValor() > miAccion.getValorCompra()){
					//Si es rentable, dado el momento y ahora cuesta mas de lo 
					// que me costo (pueden quedar acciones que nunca venda
					// puedo hacer que cada muchas iteraciones la compruebe y las
					//venda todas) pues vendo.
					int accionesAntesVenta = miAccion.getCantidad();
					int number2sell =  ((int)randomInRange(1, accionesAntesVenta));
					miAccion.setCantidad(miAccion.getCantidad()-number2sell);
					//tendre que borrar el objeto del array list si el numero de acciones es 0
					double stockLiquidity = number2sell*accionesBolsa.getValor();
					liquidez +=  stockLiquidity;
					double inversionReturn = (accionesBolsa.getValor() - miAccion.getValorCompra()) / miAccion.getValorCompra();
					updateFinancialReputation(stockLiquidity, inversionReturn);
					//System.out.println("("+ time +") id :" + getId() + ", vendo ("+  number2sell + " de " + 
					//	accionesAntesVenta + "): " + accionesBolsa.getNombre() + ", total ingreso: " + stockLiquidity);
										
					if (miAccion.getCantidad() == 0 && number2sell != 0) {
					//erase the share
						misAcciones.remove(miAccion);
						//System.out.println("("+ time +") id:" + getId() + " inversion " + miAccion.getIdCompany() + 
						//	" borrada, no le quedan mas acciones en esta inversion");
						id--;							
					}							
				}
			}			
		}

		//comprar
		if (liquidez > 0 && randomInRange(0,1)> actividadComprar){
			//para cada accion de la bolsa, tendre que ver si me interesa comprar
			// si compro le tengo que construir un objeto accion y meterlo en todas las acciones
			// de la bolsa
			for (Acciones accionesBolsa : accionesDeBolsa.values()) {
				ArrayList<Double> historico = accionesBolsa.getHistoricoAccion();
				double suma = 0;					
				for ( int i = iteraccionesCompra-1; i<historico.size(); i++) {
					suma += historico.get(i);
				}
				if (suma <= rentabilidadCompra) {
					int limite1 = (int)Math.floor(maxValorCompra / accionesBolsa.getValor());
					int limite2 = (int)(liquidez / accionesBolsa.getValor());
					int number2buy = 0;
					if (limite1 > 0 && limite2 > 0){
						if (limite1 > limite2) {
							number2buy =  ((int)randomInRange(1, limite2));
						} else {
							number2buy =  ((int)randomInRange(1, limite1));
						}
						Accion accionComprada = new Accion(number2buy, accionesBolsa.getValor(),
								accionesBolsa.getNombre(), time);
						liquidez -=  number2buy*accionesBolsa.getValor();
						misAcciones.add(accionComprada);

						//System.out.println("("+ time +") id :" + getId() + ", compro(" + number2buy + ") de " + accionesBolsa.getNombre() 
						//		+ ", total gasto: " + number2buy*accionesBolsa.getValor());					
					}
				}
			}
			//Estimates the capital I have.
			setCapital(miBolsa);
		}
	}
	
	public void updateFinancialReputation (double inversionLiquidity, double inversionReturn) {
		globalReturn = (inversionReturn * inversionLiquidity + globalReturn * globalLiquidity) / 
			(inversionLiquidity + globalLiquidity);
		globalLiquidity += inversionLiquidity;
	}
	
	public double[] generateActivityReputation () {
		for(int i = 0; i < activityReputation.length/2; i++)
			activityReputation[i] = 0;
		int timeLimit = Properties.MAX_DIFFERENCE_CLUSTERS * Properties.TIME_CLUSTER;
		int sizeMessageByLimit = 0;
		for (Mensaje mensaje : misMensajes ) {			
			if((time - mensaje.getDate()) < timeLimit)
				sizeMessageByLimit++;
			for(int i = 0; i < mensaje.getReputation().length; i++)
				activityReputation[i] += mensaje.getReputation()[i];						
		}
		for(int i = 0; i < activityReputation.length/2; i++)
			activityReputation[i+activityReputation.length/2] = activityReputation[i];		
		if(sizeMessageByLimit > Properties.MINIMUM_MESSAGES_TO_DEGRADATE) {
			if(Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR == Math.E) {
				for(int i = 0; i < activityReputation.length/2; i++) {
					activityReputation[i] /= Math.log(sizeMessageByLimit/Properties.MINIMUM_MESSAGES_TO_DEGRADATE*Math.E);
				}
			} else { //and MINIMUM_MESSAGES_TO_DEGRADATE = 10
				for(int i = 0; i < activityReputation.length/2; i++) {
					activityReputation[i] /= Math.log10(sizeMessageByLimit);
				}				
			}
		}
		return activityReputation;
	}
		
	public double[] getActivityReputation () {
		return activityReputation;
	}
		
	/**
	 * Sell a share
	 * aqui? o en ibex 35
	 */
	public void venderAccion(){
		
	}
	
	/**
	 * Buy a share
	 */
	public void comprarAccion(){
	
	}
	
	
	/**
	 * Comentamos un post de otro inversor si tiene. Esto ocurre para los vecinos 
	 * @param agent
	 */
	public void play(Agent partner){
		boolean isFirst = true;		
		
		/*ArrayList<Mensaje> mensajesDeOtro = ((Inversores) partner).getMensajes();
		for (int id = mensajesDeOtro.size()-1; id >= 0; id--){
			Mensaje mensaje = mensajesDeOtro.get(id);
			double probabilities[] = getReadAndCommentProbability(mensaje, isFirst);
			if(probabilities == null) //All previous messages have a time difference greater and return null too
				return;
			if(randomInRange(0, 1.0) < probabilities[0]) {
				played++;
				isFirst = false;
				mensaje.addReader(this);
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+probabilidadLeer +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Inversores) partner).getId());						
				if(randomInRange(0, 1.0) < probabilities[1]) {
					mensaje.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+probabilidadComentar +","+
					//		probabilities[1]+") "+id + " del inversor "+((Inversores) partner).getId());
				}
			}			
		}*/
		//TODO: probabilidad de leer por cronologia y el resto de leer por reputacion total (mensajes recomendados / mensajes cronologicos)
		//Leer mensaje con mas reputacion (por cronologia y/o reputacion total)
		// las demás lecturas ir bajando probabilidad de leer -> Probabilidad_leer_recomendados/cronologicos
		//TODO: afinidad de agentes, si un agente lee mucho de otro se le aumenta su afinidad de forma que juegue con de forma
		//  más probable
		//By cronology		
		ArrayList<Mensaje> messagesFromPartner = ((Inversores) partner).getMensajes();
		double consecutiveMessageDegradation = 1.0;
		int notReadMessages = 0;
		for (int id = messagesFromPartner.size()-1; id >= 0; id--){
			Mensaje message = messagesFromPartner.get(id);
			double probabilities[] = getReadAndCommentProbability(message, isFirst);
			if(probabilities == null) //
				return;
			if(randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				played++;
				isFirst = false;
				message.addReader(this);
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+probabilidadLeer +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Inversores) partner).getId());						
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+probabilidadComentar +","+
					//		probabilities[1]+") "+id + " del inversor "+((Inversores) partner).getId());
				}
			} else if(++notReadMessages >= Properties.NOT_READ_MESSAGES_TO_LEAVE_USER)
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_CRONOLOGY_READ_DEGRADATION;
		}
		
		/*
		//By recommendation
		int previousPosition = -1;
		while (  (previousPosition = 
				getNextMessageByOwner( ((SimulateSocialExchange)getRoot()).getPopularMessages(), this, previousPosition) ) != -1  ){
			Mensaje message = ((SimulateSocialExchange)getRoot()).getPopularMessages().get(previousPosition);
			double probabilities[] = getReadAndCommentProbability(message);
			if(probabilities == null) //
				return;
			if(isFirst || randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				played++;
				isFirst = false;
				message.addReader(this);
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+probabilidadLeer +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Inversores) partner).getId());						
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+probabilidadComentar +","+
					//		probabilities[1]+") "+id + " del inversor "+((Inversores) partner).getId());
				}
			} else
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_RECOMMENDATION_READ_DEGRADATION;
		}
		*/
	}
	
	public int getNextMessageByOwner (List<Mensaje> messages, Inversores owner, int previousPosition) {
		for(int i = previousPosition + 1; i < messages.size(); i++) {
			Mensaje message = messages.get(i);
			if(message.getIdOwner() == owner.getId())
				return i;
		}
		return -1;
	}
	
	/**
	 * - time difference: clustering the time difference in intervals of TIME_CLUSTER
	 *      and applying cronology degradation (negative exponencial with factor: CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR)
	 *      with a maximum iteration difference (MAX_DIFFERENCE_CLUSTERS)
	 *  - bad message degradation indicating by BAD_MESSAGE_DEGRADATION
	 *  
	 * Read probability:
	 *  - cronology degradation, bad message degradation and popularity (its position into the popularMessages list)
	 *  - if user has not commented the message:
	 *     - if user has already read the message: only read the message if it is good and with 
	 *        a degradation: ALREADY_READ_MESSAGE
	 *     - if user has not read the message: normal probability 
	 *  - if user has commented the message:
	 *     - and new comments have been appended to the message: normal probability
	 *     - no comments have been appended to the message: read the message with a degradation: ALREADY_COMMENTED_MESSAGE
	 *     
	 * Comment probability:
	 *  - cronology degradation, bad message degradation and read probability
	 *  - if user has not commented the message: normal probability
	 *  - if user has already commented the message: degradation of ALREADY_COMMENTED_MESSAGE
	 *  
	 */
	private double[] getReadAndCommentProbability (Mensaje mensaje, boolean isFirst) {
		double probabilities[] = new double[] {0,0};
		int time_difference = (time - mensaje.getDate()) / Properties.TIME_CLUSTER;
		if(time_difference > Properties.MAX_DIFFERENCE_CLUSTERS)
			return null;
		double cronology_degradation = 
			Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);
		double bad_message_degradation = mensaje.isGood() ? 1 : Properties.BAD_MESSAGE_DEGRADATION;
		int followersPosition = mensaje.getFollowers().lastIndexOf(this);
		if(followersPosition == -1) {
			if(mensaje.getReaders().contains(this)) {
				if(mensaje.isGood())
					probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
						* Properties.ALREADY_READ_MESSAGE * mensaje.getPopularity()[0];
			}
			else
				probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
					* bad_message_degradation * mensaje.getPopularity()[0];			
		}
		else if (followersPosition != mensaje.getFollowers().size()-1)
			probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
				* bad_message_degradation * mensaje.getPopularity()[0];
		else
			probabilities[0] = (isFirst ? 1.0 : cronology_degradation * probabilidadLeer)
				* bad_message_degradation * Properties.ALREADY_COMMENTED_MESSAGE * mensaje.getPopularity()[0];
		if(followersPosition == -1)
			probabilities[1] = cronology_degradation * probabilidadComentar;
		else
			probabilities[1] = cronology_degradation * probabilidadComentar * Properties.ALREADY_COMMENTED_MESSAGE;
		return probabilities;
	}
	
	
	/**
	 * Post a comment
	 */
	public void postear(){
	  //System.out.println("("+ time +") id: " + this.getId() + " posteo");
	  misMensajes.add(new Mensaje(this, time, (randomInRange(0,1.0) < probabilidadBuenMensaje)? true : false));
	}
	
	/**
	 * Return us the list of comments
	 */
	public ArrayList<Mensaje> getMensajes(){
		return misMensajes;
	}
	
	/*
	 * Hace falta un metodo que devuelva todos tus seguidores (unicos y repetidos)
	 * y empezar a valorar
	 * 
	 * tb que inviertan de alguna manera
	 */
	public int getNumMensajes(){
		return misMensajes.size();
	}
	
	
	public int getReaders(){
		int total = 0;
		for(Mensaje message : misMensajes){
			total += message.getNumReaders();
		}
		return total;
	}
	
	public int getReadersUnicos(){
		HashSet<Inversores> uniqueReaders = new HashSet<Inversores>();
		for(Mensaje message : misMensajes){
			HashSet<Inversores> followers1Comment = message.getUniqueReaders();
			for(Inversores investor : followers1Comment){
				uniqueReaders.add(investor);
			}
		}
		return uniqueReaders.size();
	}
	
	/**
	 * Ask for the all of followers of whole comments that Investor has. Doesn't care
	 * if the investors has repeated.
	 * 
	 * @return The number of people that have responded at one o more comments of one investor 
	 */
	public int getFollowers(){
		int suma = 0;
		for(Mensaje mensaje : misMensajes){
			suma += mensaje.getNumFollowers();
		}
		return suma;
	}	
	
	/**
	 * Ask for the singles followers of a Investor
	 * 
	 * @return The number of single investors had responded you in a comment 
	 */
	public int getFollowersUnicos(){
		HashSet<Inversores> followersUnicos = new HashSet<Inversores>();
		for(Mensaje mensaje : misMensajes){
			HashSet<Inversores> followers1Comment = mensaje.getUniqueFollowers();
			for(Inversores inversor : followers1Comment){
				followersUnicos.add(inversor);
			}
		}
		return followersUnicos.size();
	}
	
	/**
	 * Getter of Color
	 * @param ncolor
	 */
	//hay que llamalos setColor y getColor para que ascape los pinte
	public void setColor(Color ncolor) {
		myColor = ncolor;
	}
	/**
	 * Setter of Color
	 */
	public Color getColor() {
		return myColor;
	}
	
	/**
	 * Getter of Id investor
	 * @return id
	 */
	public int getId() {
		return id;
	}
	/**
	 * Setter of Id investor
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	public double getGlobal_return () {
		return globalReturn;
	}
	
	public void setLiquidez(double liquidez) {
		this.liquidez = liquidez;
	}

	public double getLiquidez() {
		return liquidez;
	}	
	
	public double getFinancialReputation() {
		return globalReturn;
	}

	public void setFinancialReputation(double financialReputation) {
		this.financialReputation = financialReputation;
	}

	public void setCapital(Ibex35 miBolsa){
		HashMap<String, Acciones> accionesDeBolsa = miBolsa.getAcciones();
		double suma = 0;
		for(Acciones accionBolsa : accionesDeBolsa.values()){
			for(Accion miAccion : misAcciones){
				if (accionBolsa.getNombre().equals(miAccion.getIdCompany())) {
					suma += miAccion.getCantidad()*accionBolsa.getValor(); 
				}
			}
		}
		this.capital = suma + liquidez;
	}
	
	public double getCapital(){
		return capital;		
	}	
	
	private int getMessagesHistory () {
		int messagesNum = 0;
		for(int messages : messageHistory)
			messagesNum += messages;
		return messagesNum;
	}
	
	private int[] getTipoAgente() {
		return tipoAgente;
	}
	
	public int getPlayed () {
		return played;
	}
	
	public void setTipoAgente(int[] tipoAgente) {
		this.tipoAgente = tipoAgente;
	}
	
	public String getMessageHistory() {
		String messagesHistory = "";
		int goodBadCount[] = new int[]{0,0};
		for(Mensaje message : misMensajes) {
			if(message.isGood())
				goodBadCount[0]++;
			else
				goodBadCount[1]++;
		}
		for(int i = 0; i < messageHistory.length; i++) {
			messagesHistory += (messageHistory[i] + goodBadCount[i])+",";
		}
		return messagesHistory;
	}
	
	public String getAgentTypeToString () {
		String agentType = "[";
		if(tipoAgente[0] == EXPERIMENTED_INVESTOR)
			agentType += "EXP_IN,";
		else
			agentType += "AMA_IN,";
		if(tipoAgente[1] == FREQUENT_USER)
			agentType += "FRE_US";
		else
			agentType += "OCA_US";
		agentType += ":"+String.format("%.2f", probabilidadPostear)+",";
		if(tipoAgente[2] == GOOD_WRITER)
			agentType += "GOOD_WR";
		else
			agentType += "BAD_WRI";
		return agentType +":"+String.format("%.2f", probabilidadBuenMensaje)+"]";
	}
}
