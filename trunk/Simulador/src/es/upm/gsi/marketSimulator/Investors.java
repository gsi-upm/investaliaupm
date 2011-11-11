package es.upm.gsi.marketSimulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ascape.model.CellOccupant;
import org.ascape.model.Agent;
import org.ascape.model.Scape;

public class Investors extends CellOccupant {	
	private static final long serialVersionUID = -3651394341932621403L;
	
	protected static boolean isNewIteration = true;
	protected static int setId = 1;
	protected static int EXPERIMENTED_INVESTOR = 0;
	protected static int AMATEUR_INVESTOR = 1;
	protected static int RANDOM_INVESTOR = 2;
	
	private InvestorType investor;
	
	protected static int FREQUENT_USER = 0;
	protected static int OCASIONAL_USER = 1;
	protected static int GOOD_WRITER = 0;
	protected static int BAD_WRITER = 1;
	
	protected static int FRIENDLY_USER = 0;
	protected static int NO_FRIENDLY_USER = 1;
	
	private int agentType[];
	private int id;
	protected Color myColor;	
	protected ArrayList<Message> myMessages;	
	
	private int messageHistory[];
	private int readerHistory[];
	private int uniqueReaderHistory[];
	private int followerHistory[];
	private int uniqueFollowerHistory[];
	private int scorerHistory[];
	private int scoreHistory[];
	
	private int sizeMessageByLimit = 0;
	
	private HashSet<Investors> friends;
	private HashSet<Investors> followers;
	public int afinityAverage = 1;
	
	//post messages, read messages, add comments, score message... probabilities:
	private double readProbability;
	private double commentProbability;
	private double postProbability;
	private double scoreProbability;	
	private double goodMessageProbability;
	private double friendlyProbability;
	
	//reputations:
	private double popularity = 1;		
	private double messageReputation[];
	private double friendReputation[];
	
	//For statistics:
	public int reads = 0;
	public int isRead = 0;
	public int plays = 0;
	public int isPlayed = 0;
	public double isReadProbability = 0;
	public int probabilitiesNum = 0;
	
	//create the list of rules for scape
	public void scapeCreated() {
		//getScape().addInitialRule(INITIALIZE_RULE);
		getScape().addInitialRule(MOVE_RANDOM_LOCATION_RULE);
		//Add iteration rules: iterate, update and random_walk 
		getScape().addRule(ITERATE_RULE);
		getScape().addRule(UPDATE_RULE);
		getScape().addRule(RANDOM_WALK_RULE);
		//getScape().addRule(PLAY_RANDOM_NEIGHBOR_RULE);
	}

	/*
	 * One way or other is right. You can define the rule in SimulateSocialExchange class
	 * or here with method initialize
	 */
	
    public void defineInvestor() {
    	setId(setId++);
    	myMessages = new ArrayList<Message>();
    	friends = new HashSet<Investors>();
    	followers = new HashSet<Investors>();
    	agentType = new int[4];
    	messageReputation = new double[Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR.length *
    	                               Properties.MINIMUM_MESSAGES_TO_DEGRADATE.length];
    	friendReputation = new double[Properties.NO_COMMON_FRIEND_WEIGHT.length *
    	                              Properties.FRIEND_DEGRADATION_FACTOR.length];
    	messageHistory = new int[]{0,0};
    	readerHistory = new int[]{0,0};
    	uniqueReaderHistory = new int[]{0,0};
    	followerHistory = new int[]{0,0};
    	uniqueFollowerHistory = new int[]{0,0};
    	scorerHistory = new int[]{0,0};
    	scoreHistory = new int[]{0,0};
    	//Investor:
    	double totalProbability = Properties.INTELLIGENT_INVESTOR_PROBABILITY +
    		Properties.AMATEUR_INVESTOR_PROBABILITY + Properties.RANDOM_INVESTOR_PROBABILITY;	
    	double investorType = randomInRange(0.0, totalProbability);
        if (investorType < Properties.INTELLIGENT_INVESTOR_PROBABILITY) {
            agentType[0] = EXPERIMENTED_INVESTOR;            
            investor = new IntelligentInvestor(this);                    
        } else if(investorType < (Properties.INTELLIGENT_INVESTOR_PROBABILITY +
        		Properties.AMATEUR_INVESTOR_PROBABILITY)) {
        	agentType[0] = AMATEUR_INVESTOR;
        	investor = new AmateurInvestor(this);        	       	           
        } else {
        	agentType[0] = RANDOM_INVESTOR;
        	investor = new RandomInvestor(this);
        }
        //Activity of the user:
        if(randomInRange(0.0, 1.0) < Properties.FREQUENT_USER_PROBABILITY) {
        	agentType[1] = FREQUENT_USER;
        	readProbability = randomInRange(Properties.FREQ_USER_READ_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_READ_PROBABILITY_LIMITS[1]);
        	commentProbability = randomInRange(Properties.FREQ_USER_COMMENT_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_COMMENT_PROBABILITY_LIMITS[1]);
        	postProbability = randomInRange(Properties.FREQ_USER_POST_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_POST_PROBABILITY_LIMITS[1]);
        	scoreProbability = randomInRange(Properties.FREQ_USER_SCORE_PROBABILITY_LIMITS[0],
        			Properties.FREQ_USER_SCORE_PROBABILITY_LIMITS[1]);        	
        } else {
        	agentType[1] = OCASIONAL_USER;
        	readProbability = randomInRange(Properties.OCA_USER_READ_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_READ_PROBABILITY_LIMITS[1]);
        	commentProbability = randomInRange(Properties.OCA_USER_COMMENT_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_COMMENT_PROBABILITY_LIMITS[1]);
        	postProbability = randomInRange(Properties.OCA_USER_POST_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_POST_PROBABILITY_LIMITS[1]);
        	scoreProbability = randomInRange(Properties.OCA_USER_SCORE_PROBABILITY_LIMITS[0],
        			Properties.OCA_USER_SCORE_PROBABILITY_LIMITS[1]);        	
        }
        //Good o bad writer?
        if(randomInRange(0.0, 1.0) < Properties.GOOD_WRITER_PROBABILITY) {
        	agentType[2] = GOOD_WRITER;
        	goodMessageProbability = randomInRange(Properties.GOOD_MESSAGES_PROBABILITY_LIMITS[0],
        			Properties.GOOD_MESSAGES_PROBABILITY_LIMITS[1]);
        } else {
        	agentType[2] = BAD_WRITER;
        	goodMessageProbability = randomInRange(Properties.BAD_MESSAGES_PROBABILITY_LIMITS[0],
        			Properties.BAD_MESSAGES_PROBABILITY_LIMITS[1]);
        }
        //Friendly or not_friendly
        if(randomInRange(0.0, 1.0) < Properties.FRIENDLY_PROBABILITY) {
        	agentType[3] = FRIENDLY_USER;
        	friendlyProbability = randomInRange(Properties.FRIENDLY_PROBABILITY_LIMITS[0],
        			Properties.FRIENDLY_PROBABILITY_LIMITS[1]);
        } else {
        	agentType[3] = NO_FRIENDLY_USER;
        	friendlyProbability = randomInRange(Properties.NO_FRIENDLY_PROBABILITY_LIMITS[0],
        			Properties.NO_FRIENDLY_PROBABILITY_LIMITS[1]);
        }
        setColorByAgentType();
        System.out.println("id:"+getId()+" Configured Agent:"+getAgentTypeToString ());       
    }
   
    private void setColorByAgentType () {
    	float red;
    	float green;
    	float blue;
    	if(agentType[0] == EXPERIMENTED_INVESTOR) {
    		red = 0;
    	} else if(agentType[0] == AMATEUR_INVESTOR){
    		red = 0.5f;
    	} else
    		red = 1;
    	if(agentType[1] == FREQUENT_USER) {
    		green = 0;
    	} else {
    		green = 0.8f;
    	}
    	if(agentType[2] == GOOD_WRITER) {
    		blue = 0;
    	} else {
    		blue = 1;
    	}
    	setColor(new Color(red, green, blue));	
    }
        
	public void iterate(){		
		if (isNewIteration){
			isNewIteration = false;			
			//Generate reputation of investor´messages
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++)
				((SimulateSocialExchange)getRoot()).getPopularMessages()[i].clear();
			for(Object cell : scape) {
				if(cell instanceof Investors) {
					for(Message message : ((Investors)cell).getMessages()) {
						if((getIteration() - message.getDate()) < Properties.TIME_LIMIT) {
							message.generateReputation(getIteration());
							((SimulateSocialExchange)getRoot()).sortAddMessages(message);
						}
					}					
				}
			}
			if(getIteration() % Properties.REPUTATION_INTERVAL == 0) { //Generate reputations
				generateReputation();
				((SimulateSocialExchange)getRoot()).setSortInvestorByFinance(sortByFinancialReputation(scape));
				generateFinancialPopularity();
			}
			if(getIteration() % Properties.STATISTICS_INTERVAL == 0) { //Generate statistics
				Statistics.generateStatistics(this);				
			}
			//Clean messages users'history of many time ago
			if(getIteration() % Properties.CLEAN_INTERVAL == 1) {
				for(Object cell : scape) {
					if(cell instanceof Investors) {							
						((Investors)cell).cleanMessages(getIteration() - Properties.MESSAGE_TIME_TO_CLEAN);
					}
				}
			}
			//Generate popularity 
			for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getPopularMessages().length; i++) {
				int sizePopularMessages = ((SimulateSocialExchange)getRoot()).getPopularMessages()[i].size();
				int initMessage = (int) Math.ceil(sizePopularMessages/Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR);
				for(int j = initMessage; j < sizePopularMessages; j++) {
					//((SimulateSocialExchange)getRoot()).getPopularMessages().get(i).setPopularity
					//	((double)i/sizePopularMessages*Message.POPULARITY_INCREMENTATION_LINEAL_FACTOR);
					((SimulateSocialExchange)getRoot()).getPopularMessages()[i].get(j).setPopularity
						(i,Math.pow(Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR, 
						 j*Properties.POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR/(double)sizePopularMessages-1.0));
				}
			}			
		}		
	}
	
	public void generateReputation () {
		Ibex35 ibex35 = ((SimulateSocialExchange)getRoot()).getStock();	
		for(Object cellOcupant : scape) {
			if(cellOcupant instanceof Investors) {
				Investors investor = (Investors)cellOcupant;
				investor.generateActivityReputation();
				investor.investor.updateFinancialReputation(ibex35);
				double capital = investor.investor.getActualCapital(ibex35);
				investor.investor.addCapitalToHistory(capital);
			}					
		}				
	}
	
	private void generateFinancialPopularity() {
		int limit = (int) (((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().size() *
				(Properties.FINANCIAL_POPULARITY_EXPONENCIAL_FACTOR-1)/
				  Properties.FINANCIAL_POPULARITY_EXPONENCIAL_FACTOR);
		for(int i = 0; i < ((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().size(); i++) {
			//((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().get(i).popularity = 1;
			Investors investor = ((SimulateSocialExchange)getRoot()).getSortInvestorByFinance().get(i);
			investor.popularity = Math.pow(Properties.FINANCIAL_POPULARITY_EXPONENCIAL_FACTOR, 
						 i/(double)limit - 1.0);			
		}
	}	
	
	public int[][] historyMessageStatistics() {
		int statistics[][] = {{messageHistory[0],readerHistory[0],uniqueReaderHistory[0],
			followerHistory[0],uniqueFollowerHistory[0],scorerHistory[0],scoreHistory[0]}, 
			{messageHistory[1],readerHistory[1],uniqueReaderHistory[1],followerHistory[1],
			uniqueFollowerHistory[1],scorerHistory[1],scoreHistory[1]}};
		return statistics;
	}
	
	private void cleanMessages (int timeLimit) {
		for(int i = 0; i < myMessages.size(); i++) {
			Message message = myMessages.get(i);
			if(message.getDate() < timeLimit) {
				if(message.isGood()) {
					messageHistory[0]++;
					readerHistory[0] += message.getNumReaders();
					uniqueReaderHistory[0] += message.getUniqueNumReaders();
					followerHistory[0] += message.getNumReaders();
					uniqueFollowerHistory[0] += message.getUniqueNumFollowers();
					scorerHistory[0] += message.getScores().size();
					scoreHistory[0] += message.getScore();
				} else {
					messageHistory[1]++;
					readerHistory[1] += message.getNumReaders();
					uniqueReaderHistory[1] += message.getUniqueNumReaders();
					followerHistory[1] += message.getNumReaders();
					uniqueFollowerHistory[1] += message.getUniqueNumFollowers();
					scorerHistory[1] += message.getScores().size();
					scoreHistory[1] += message.getScore();
				}
				myMessages.remove(i);				
				i--;
			}
			else
				return;
		}
	}
	
	static public List<Investors> sortByMessageReputation(Scape agentsList) {
		List<Investors> sortList = new ArrayList<Investors>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Investors) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Investors)cell).getMessageReputation()[0] 
					        >= sortList.get(i).getMessageReputation()[0]) {
						sortList.add(i, (Investors)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Investors)cell);
			}
		}
		return sortList;
	}
	
	private List<Investors> sortByFinancialReputation(Scape agentsList) {
		List<Investors> sortList = new ArrayList<Investors>();
		boolean sorted;
		for (Object cell : agentsList) {
			if(cell instanceof Investors) {
				sorted = false;
				for(int i = 0; i < sortList.size(); i++) {
					if(((Investors)cell).investor.getFinancialReputation() <= 
							sortList.get(i).investor.getFinancialReputation()) {
						sortList.add(i, (Investors)cell);
						sorted=true;
						break;
					}
				}
				if(!sorted)
					sortList.add((Investors)cell);
			}
		}
		return sortList;
	}
	
	public void update(){
		//iterations++;
		isNewIteration = true;
		//post in own page.
		if(randomInRange(0,1.0) < postProbability){
			post();
		}
		//update thresholds
	}
	
	@SuppressWarnings("unchecked")
	public void chooseNeighborToPlay() {
		List<Investors> neighbors = findWithin(Properties.NEIGHBOR_DISTANCE_TO_PLAY);
		for(Investors neighbor : neighbors) {
			double probabilityToPlay = Math.pow(Properties.NEIGHBOR_DISTANCE_EXPONENCIAL_DEGRADATION,
					calculateDistance(neighbor)) * readProbability; // * neighbor.popularity;
			if(randomInRange(0.0,1.0) < probabilityToPlay) {
				play(neighbor);
			}
			//System.out.println("("+ time +") id :" + getId() + ",pob:" + 
			//probabilityToPlay + " cal:" + calculateDistance(neighbor));
		}
		//System.out.println("("+ time +") id :" + getId() + ", played:" + played + " " + neighbors.size());
	}	
	
	/**
	 *  Interactuamos con la bolsa
	 * @param miBolsa
	 */
	public void playInStock(Ibex35 myStock){
		investor.playInStock(myStock);	
	}	
	
	public void generateActivityReputation () {
		int size = messageReputation.length/
			Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR.length;
		for(int i = 0; i < size; i++) {
			messageReputation[i] = 0;
		}
		int timeLimit = Properties.MAX_DIFFERENCE_CLUSTERS * Properties.TIME_CLUSTER;
		sizeMessageByLimit = 0;
		for (int j = myMessages.size()-1; j >= 0; j--) { //Message message : myMessages ) {
			Message message = myMessages.get(j);	
			int timeDifference = getIteration() - message.getDate();
			if(timeDifference >= timeLimit) {
				break;
			}
			sizeMessageByLimit++;
			for(int i = 0; i < message.getReputation().length; i++) {
				int clusterDifference = timeDifference / Properties.TIME_CLUSTER;
				messageReputation[i] += message.getReputation()[i] 
				        * Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR, -clusterDifference);
				//LINEAL: messageReputation[i] += mensaje.getReputation()[i] / clusterDifference;
			}
		}
		for(int i = 0; i < size; i++) {
			messageReputation[i+messageReputation.length/2] = messageReputation[i];
		}
		for(int i = 0; i < Properties.MINIMUM_MESSAGES_TO_DEGRADATE.length; i++) {
			if(sizeMessageByLimit > Properties.MINIMUM_MESSAGES_TO_DEGRADATE[i]) {
				for(int j = 0; j < Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR.length; j++) {
					if(Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR[j] == Math.E) {
						messageReputation[j*size+i] /= Math.log(((double)sizeMessageByLimit)
								/Properties.MINIMUM_MESSAGES_TO_DEGRADATE[i]*Math.E);
					} else if(Properties.MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR[j] == 10) {
						messageReputation[j*size+i] /= Math.log(((double)sizeMessageByLimit)
								/Properties.MINIMUM_MESSAGES_TO_DEGRADATE[i]*10);
					} else {  //0 = none
						
					}
				}
			}
		}
		//friendReputation
		int commonFriends = 0;
		for(Investors friend: friends) {
			if(followers.contains(friend))
				commonFriends++;
		}
		for(int i = 0; i < Properties.NO_COMMON_FRIEND_WEIGHT.length; i++) {
			for(int j = 0; j < Properties.FRIEND_DEGRADATION_FACTOR.length; j++) {	
				if(commonFriends == 0) {
					if(followers.size() > 0) {
						if(Properties.FRIEND_DEGRADATION_FACTOR[j] == Math.E) {
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
								Properties.NO_COMMON_FRIEND_WEIGHT[i] * 
								Math.log(followers.size());
						} else { //0 = none
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
									Properties.NO_COMMON_FRIEND_WEIGHT[i] * 
									(followers.size());
						}						
					} 
				} else {
					if(followers.size()-commonFriends > 0) {
						 if(Properties.FRIEND_DEGRADATION_FACTOR[j] == Math.E) {
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
								Properties.NO_COMMON_FRIEND_WEIGHT[i] * 
								Math.log(followers.size()-commonFriends) + 
								Properties.COMMON_FRIEND_WEIGHT[i] * Math.log(commonFriends);
						} else { //0 = noew
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
								Properties.NO_COMMON_FRIEND_WEIGHT[i] * 
								(followers.size()-commonFriends) + 
								Properties.COMMON_FRIEND_WEIGHT[i] * commonFriends;
						}						 
					} else {
						if(Properties.FRIEND_DEGRADATION_FACTOR[j] == Math.E) {
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
								Properties.COMMON_FRIEND_WEIGHT[i] * Math.log(commonFriends);
						} else { //0 = none
							friendReputation[i*Properties.FRIEND_DEGRADATION_FACTOR.length+j] = 
									Properties.COMMON_FRIEND_WEIGHT[i] * commonFriends;
						}						
					}
				}
			}			
		}		
	}
	
	public double[] getActivityReputation () {
		double activityReputation[] = new double[messageReputation.length *
		                                         friendReputation.length];
		for(int i = 0; i < messageReputation.length; i++) {
			for(int j = 0; j < friendReputation.length; j++) {
				activityReputation[i*friendReputation.length+j] = Properties.MESSAGE_WEIGHT
					* messageReputation[i] + Properties.FRIEND_WEIGHT * friendReputation[j];
			}
		}
		return activityReputation;
	}
		
	/**
	 * Comentamos un post de otro inversor si tiene. Esto ocurre para los vecinos 
	 * @param agent
	 */
	public void play(Agent partner){
		boolean isFirst = true;		
		//For statistics
		plays++;
		((Investors) partner).isPlayed++;		
			
		//TODO: probabilidad de leer por cronologia y el resto de leer por reputacion
		//      total (mensajes recomendados / mensajes cronologicos)
		//TODO: afinidad de agentes, si un agente lee mucho de otro se le aumenta su
		//      afinidad de forma que juegue con de forma más probable
		
		//By cronology:		 
		//UNCOMMENT THIS TO EXECUTE ACTIVITY AGENT BY ARTICLES, COMMENTS AND SCORES:
		ArrayList<Message> messagesFromPartner = ((Investors) partner).getMessages();			
		double consecutiveMessageDegradation = 1.0;
		int notReadMessages = 0;
		for (int id = messagesFromPartner.size()-1; id >= 0; id--){
			Message message = messagesFromPartner.get(id);
			double probabilities[] = getReadCommentAndScoreProbability(message, isFirst);
			if(probabilities == null) //If message has a time creation greater than the threshold
				return;
			((Investors) partner).isReadProbability += probabilities[0] * consecutiveMessageDegradation;
			((Investors) partner).probabilitiesNum++;
			if(randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				reads++;
				((Investors) partner).isRead++;
				message.addReader(this);
				
				//Score
				if(randomInRange(0.0,1.0) < probabilities[2]) {
					if(message.isGood()) {
						message.addScore(this, randomInRange(
								Properties.GOOD_MESSAGES_SCORE_PROBABILITY[0],
								Properties.GOOD_MESSAGES_SCORE_PROBABILITY[1]));
					} else {
						message.addScore(this, randomInRange(
								Properties.BAD_MESSAGES_SCORE_PROBABILITY[0],
								Properties.BAD_MESSAGES_SCORE_PROBABILITY[1]));
					}					
				}
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje
				//("+readProbability +","+probabilities[0]+") "+id + "("+ mensaje.getPopularity()
				//+ ") del inversor "+((Investors) partner).getId());
				
				//Comment:
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+commentProbability +","+
					//		probabilities[1]+") "+id + " del inversor "+((Investors) partner).getId());
				}
				
				//Friend Relationship <- move from the beginning, only add friend in you read, not only play
				if(isFirst) {							
					if(randomInRange(0.0,1.0) < friendlyProbability && !friends.contains(partner)) {			
						if(randomInRange(0.0,1.0) < getFriendlyProbability((Investors) partner)) {
							friends.add((Investors)partner);
							((Investors) partner).followers.add(this);
						}			
					}
				}
				isFirst = false;				
			} else if(++notReadMessages >= Properties.NOT_READ_MESSAGES_TO_LEAVE_USER)
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_CRONOLOGY_READ_DEGRADATION;
		}		
		
		/*
		//By recommendation
		int previousPosition = -1;
		while (  (previousPosition = 
				getNextMessageByOwner( ((SimulateSocialExchange)getRoot()).getPopularMessages(), this, previousPosition) ) != -1  ){
			Message message = ((SimulateSocialExchange)getRoot()).getPopularMessages().get(previousPosition);
			double probabilities[] = getReadAndCommentProbability(message);
			if(probabilities == null) //
				return;
			if(isFirst || randomInRange(0, 1.0) < (probabilities[0] * consecutiveMessageDegradation) ) {
				played++;
				isFirst = false;
				message.addReader(this);
				//System.out.println("("+ time +") id: " + this.getId() + " lee mensaje("+readProbability +","+
				//		probabilities[0]+") "+id + "("+ mensaje.getPopularity() + ") del inversor "+((Investors) partner).getId());						
				if(randomInRange(0, 1.0) < probabilities[1]) {
					message.addComment(this);
					//System.out.println("("+ time +") id: " + this.getId() + " comenta mensaje("+commentProbability +","+
					//		probabilities[1]+") "+id + " del inversor "+((Investors) partner).getId());
				}
			} else
				return;
			consecutiveMessageDegradation *= Properties.CONSECUTIVE_MESSAGE_RECOMMENDATION_READ_DEGRADATION;
		}
		*/
	}
	
	public double calculateFriendAffinity(List<Message> messagesFromPartner) {
		double affinity = 0;
		for(int i = messagesFromPartner.size()-1; i >= 0; i--) {
			Message message = messagesFromPartner.get(i);
			if((getIteration() - message.getDate()) > Properties.TIME_LIMIT)
				break;
			affinity += message.getUniqueReaders().contains(this)?1:0  +
					(message.getUniqueFollowers().contains(this)?1:0) * 3 + 
					(message.getScores().containsKey(this)?
					(message.getScores().get(this)-Properties.MAXIMUM_SCORE/2):0);
		}
		return affinity;
	}
	
	public double getFriendlyProbability(Investors partner) {
		double probability = 1.5;
		double affinity = calculateFriendAffinity(partner.getMessages());		
		
		afinityAverage = 1;
		for(Investors friend : friends) {
			afinityAverage += calculateFriendAffinity(friend.getMessages());			
		}
		if(friends.size() > 0) {
			afinityAverage /= friends.size();		
			probability *= affinity / afinityAverage;
			int commonFriends = 1;
			for(Investors friend : friends) {
				if(partner.getFriends().contains(friend)) 
					commonFriends++;			
			}
			if(commonFriends > 1/Properties.FRIENDLY_COMMON)
				probability *= Properties.FRIENDLY_COMMON * commonFriends;
			//else
			//	probability *= Properties.FRIENDLY_COMMON;
		}		
		if(friends.size() > Properties.FRIEND_DEGRADATION) {
			probability *= Math.pow(((double)friends.size())/Properties.FRIEND_DEGRADATION, 
					-Properties.EXPONENCIAL_FRIEND_STRENGH_DECREMENT);
		}
		return probability;
	}
	
	public int getNextMessageByOwner (List<Message> messages, Investors owner, int previousPosition) {
		for(int i = previousPosition + 1; i < messages.size(); i++) {
			Message message = messages.get(i);
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
	private double[] getReadCommentAndScoreProbability (Message message, boolean isFirst) {
		double probabilities[] = new double[] {0,0,0};
		int cluster_difference = (getIteration() - message.getDate())/Properties.TIME_CLUSTER;
		if(cluster_difference > Properties.MAX_DIFFERENCE_CLUSTERS)
			return null;
		double cronology_degradation = 
			Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-cluster_difference);
		double bad_message_degradation = message.isGood() ? 1.0 : Properties.BAD_MESSAGE_DEGRADATION;
		int followersPosition = message.getFollowers().lastIndexOf(this);
		if(followersPosition == -1) {
			if(message.getReaders().contains(this)) {
				if(message.isGood()) { //Is message is bad, never read again
					probabilities[0] = (isFirst ? 1.0 : readProbability) * cronology_degradation
						* Properties.ALREADY_READ_MESSAGE * message.getPopularity()[0];
				}
			} else {
				probabilities[0] = (isFirst ? 1.0 : readProbability) * cronology_degradation
					* bad_message_degradation * message.getPopularity()[0];
			}
		} else if (followersPosition != message.getFollowers().size()-1) {
			probabilities[0] = (isFirst ? 1.0 : readProbability) * cronology_degradation
				* bad_message_degradation * message.getPopularity()[0];
		} else {
			probabilities[0] = (isFirst ? 1.0 : readProbability) * cronology_degradation
				* bad_message_degradation * Properties.ALREADY_COMMENTED_MESSAGE * message.getPopularity()[0];
		}
		if(followersPosition == -1)
			probabilities[1] = cronology_degradation * commentProbability;
		else
			probabilities[1] = cronology_degradation * commentProbability * Properties.ALREADY_COMMENTED_MESSAGE;
		if(message.getScores().containsKey(this))
			probabilities[2] = cronology_degradation * scoreProbability * Properties.ALREADY_SCORED_MESSAGE;
		else
			probabilities[2] = cronology_degradation * scoreProbability;
		return probabilities;
	}
	
	
	/**
	 * Post a message
	 */
	public void post(){
	  //System.out.println("("+ time +") id: " + this.getId() + " posts");
	  myMessages.add(new Message(this, getIteration(), 
			  (randomInRange(0,1.0) < goodMessageProbability)? true : false));
	}
	
	
	public HashSet<Investors> getFollower() {
		return followers;
	}
	
	public double[] getMessageReputation () {
		return messageReputation;
	}
	
	public double[] getFriendReputation () {
		return friendReputation;
	}	
	
	public double getGoodMessageProbability() {
		return goodMessageProbability;
	}
	
	public double getPostProbability() {
		return postProbability;
	}
	
	public int getSizeMessageByLimit() {
		return sizeMessageByLimit;
	}
	
	/**
	 * Return us the list of written messages
	 */
	public ArrayList<Message> getMessages(){
		return myMessages;
	}
		
	public int getNumMensajes(){
		return myMessages.size();
	}
	
	
	public int getReaders(){
		int total = 0;
		for(Message message : myMessages){
			total += message.getNumReaders();
		}
		return total;
	}	
	
	/**
	 * Ask for the all of followers of whole comments that Investor has. Doesn't care
	 * if the investors has repeated.
	 * 
	 * @return The number of people that have responded at one o more comments of one investor 
	 */
	public int getFollowers(){
		int suma = 0;
		for(Message message : myMessages){
			suma += message.getNumFollowers();
		}
		return suma;
	}	
	
	/**
	 * Ask for the singles followers of a Investor	 
	 * @return The number of single investors had responded you in a comment 
	 */
	/*public int getFollowersUnicos(){
		HashSet<Investors> followersUnicos = new HashSet<Investors>();
		for(Message mensaje : myMessages){
			HashSet<Investors> followers1Comment = mensaje.getUniqueFollowers();
			for(Investors inversor : followers1Comment){
				followersUnicos.add(inversor);
			}
		}
		return followersUnicos.size();
	}*/
	
	/**
	 * Getter of Color
	 * @param ncolor
	 */	
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
	
	public int getTime() {
		return getIteration();
	}	

	public InvestorType getInvestor() {
		return investor;
	}
	
	public int getMessagesHistory () {
		int messagesNum = 0;
		for(int messages : messageHistory)
			messagesNum += messages;
		return messagesNum;
	}
	
	public int[] getAgentType() {
		return agentType;
	}
	
	public int getReads() {
		return reads;
	}
	
	public int getPlays() {
		return plays;
	}
	
	public HashSet<Investors> getFriends () {
		return friends;
	}
	
	public void setAgentType(int[] tipoAgente) {
		this.agentType = tipoAgente;
	}
		
	public String getMessageHistory() {
		String messagesHistory = "";
		int goodBadCount[] = new int[]{0,0};
		for(Message message : myMessages) {
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
		String agentTypeString = "[";
		if(agentType[0] == EXPERIMENTED_INVESTOR)
			agentTypeString += "PRU_IN,";
		else if(agentType[0] == AMATEUR_INVESTOR)
			agentTypeString += "AMA_IN,";
		else
			agentTypeString += "RAM_IN,";
		if(agentType[1] == FREQUENT_USER)
			agentTypeString += "FRE_US";
		else
			agentTypeString += "OCA_US,";
		agentTypeString += ":"+String.format("%.2f", postProbability)+",";
		if(agentType[2] == GOOD_WRITER)
			agentTypeString += "GOOD_W,";
		else
			agentTypeString += "BAD_WR";
		agentTypeString += ":"+String.format("%.2f", goodMessageProbability);
		if(agentType[3] == FRIENDLY_USER)
			agentTypeString += ",FRI";
		else
			agentTypeString += ",NOF";
		return agentTypeString + ":"+friends.size()+String.format("-%.2f",friendlyProbability) 
				+ "]" + investor.getAgentTypeToString();
	}
}
