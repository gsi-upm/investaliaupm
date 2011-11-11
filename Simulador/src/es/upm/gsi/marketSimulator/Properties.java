package es.upm.gsi.marketSimulator;

public class Properties {
	public static double VARIATION_SCALE = 5; //4,3
	public static double STOCK_VARIATION = 1.03; //0.98;
		//Bear Market -> variation *= 0.X, Bull Market -> variation *= 1.X
	public static double LINEAL_REVERSE_SHARE_LIMIT = 2;
	
	public static double INTELLIGENT_INVESTOR_PROBABILITY = 0.75; //0.6
	public static double AMATEUR_INVESTOR_PROBABILITY = 0.125;  //0.2
	public static double RANDOM_INVESTOR_PROBABILITY = 0.125;  //0.2
	public static double IMPULSIVE_PROBABILITY = 0.5;
	public static double PERCEPTION_PROBABILITY = 0.5;
	public static double ANXIETY_PROBABILITY = 0.5;
	public static double MEMORY_PROBABILITY = 0.5;
	public static double DIVERSIFICATION_PROBABILITY = 0.5;
	
	public static double GOOD_WRITER_PROBABILITY = 0.5;
	public static double GOOD_MESSAGES_PROBABILITY_LIMITS[] = {0.75,0.85}; //Before {0.7,0.9}
	public static double BAD_MESSAGES_PROBABILITY_LIMITS[] = {0.05,0.15}; //Before {0,0.2}
	public static int MAXIMUM_SCORE = 5;
	public static Integer GOOD_MESSAGES_SCORE_PROBABILITY[] = {3,MAXIMUM_SCORE};
	public static Integer BAD_MESSAGES_SCORE_PROBABILITY[] = {0,2};
	
	public static double FREQUENT_USER_PROBABILITY = 0.5;
	public static double FREQ_USER_READ_PROBABILITY_LIMITS[] = {0.68,0.82}; //Before {0.6, 0.9}
	public static double FREQ_USER_POST_PROBABILITY_LIMITS[] = {0.2,0.36}; //
	public static double FREQ_USER_COMMENT_PROBABILITY_LIMITS[] = {0.08,0.14};
	public static double FREQ_USER_SCORE_PROBABILITY_LIMITS[] = {0.1,0.34};
	public static double OCA_USER_READ_PROBABILITY_LIMITS[] = {0.17,0.25};  //Before {0.12, 0.3}
	public static double OCA_USER_POST_PROBABILITY_LIMITS[] = {0.04,0.08}; //Before {0.03,0.08}
	public static double OCA_USER_COMMENT_PROBABILITY_LIMITS[] = {0.01,0.04};
	public static double OCA_USER_SCORE_PROBABILITY_LIMITS[] = {0.1,0.3};
		
	//public static double POPULARITY_INCREMENTATION_LINEAL_FACTOR = 4;
	public static double POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR = 2;
	public static double FINANCIAL_POPULARITY_EXPONENCIAL_FACTOR = 2.5; //4; -> too much?
	
	public static int TIME_CLUSTER = 30;   //100;
	public static int MAX_DIFFERENCE_CLUSTERS = 11;   //6;
	public static int TIME_LIMIT = TIME_CLUSTER * MAX_DIFFERENCE_CLUSTERS;	
	public static double CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR = 1.6; //1.5;
	
	public static double READER_WEIGHT = 0.3;
	public static double FOLLOWER_WEIGHT = 0.7;
	public static double SCORER_WEIGHT = 0.24; //0.12, 0.5
	public static double USERS_TO_MAXIMUM_TRUST = 4;
	public static double TRUST_WEIGHT = 10 / Properties.USERS_TO_MAXIMUM_TRUST;	
	
	public static int REPUTATION_INTERVAL = 10; 
	public static int STATISTICS_INTERVAL = 10 * REPUTATION_INTERVAL; 
		//multiple of REPUTATION_INTERVAL, TIME_CLUSTER;
	public static int CAPITAL_TIME_DIFFERENCE = 180; //difference between reputation_interval 
	public static int CLEAN_INTERVAL = 1000;
	public static int MESSAGE_TIME_TO_CLEAN = 2000; //10000
	
	public static double NEIGHBOR_DISTANCE_TO_PLAY = 6; //Before popularity = 4
	//Subdividir NEIGHBOR_DISTANCE_TO_PLAY en PLAY_OCASIONAL y PLAY_FREQUENT
	public static double NEIGHBOR_DISTANCE_EXPONENCIAL_DEGRADATION = 0.8;
	
	
	public static double PERCEPTION_DEGRADATION = 4;
	//public static double[][] anxietySellTable = {{0.01,0},{0.15,100}};
	public static double[][] anxietySellTable = {{0.1,0},{0.2,100}}; //For HistoricFiles
	public static double[] anxietySellAll = {2,4};
	
	
	//public static double[][] sellTable = {{0.03,0},{0.06,3},{0.1,7},{0.14,14},{0.21,25},{0.5,100}}; 
	//-> valido para variation 1.05 o mas
	//public static double[][] sellTable = {{0.01,0},{0.01,3},{0.02,16},{0.027,23},{0.035,32},{0.5,100}};
	// |- para 0.98
	//public static double[][] sellTable = {{0.05,0},{0.05,3},{0.10,16},{0.13,23},{0.18,32},{0.4,100}};
	// |- para 1.03
	//public static double[][] sellTable = {{0.05,0},{0.05,4},{0.10,16},{0.15,23},{0.24,32},{0.5,100}};
		
	public static double[][] sellHistoryFileTable = {{0.05,0},{0.05,4},{0.10,16},{0.15,23},{0.24,32},{0.5,100}};
	public static double[][][] sellPrudentTable = {
		{{0.03,0},{0.06,3},{0.1,7},{0.14,14},{0.21,25},{0.5,100}},
		{{0.05,0},{0.05,3},{0.10,16},{0.13,23},{0.18,32},{0.4,100}},
		{{0.01,0},{0.01,3},{0.02,16},{0.027,23},{0.035,32},{0.05,100}}
	};	
	public static double[] chooseTableByStockVariation = {1.5,1.3,0.98};	
	public static double[] sellAll = {4,8};		
	
	public static double[][] sellAmateurTable = null; //{{0.01-0.05,100}};
	//public static double[] sellAmateurRange = {0.01, 0.28}; //-> variation 1.05 or more
	//public static double[] sellAmateurRange = {0.02, 0.08}; //variation > 1
	//public static double[] sellAmateurRange = {0.01, 0.05}; //variation < 1
	public static double[] sellAmateurRange = {0.04, 0.25}; //HistoricFiles
	public static double[] sellAmateurAll = {2,4};
	
	public static int INITIAL_LIQUIDITY = 10000;
	public static double MAX_BUY_VALUE = INITIAL_LIQUIDITY * 0.07; //0.1 -> MUCHAS WhL y WZ!!
	public static double MAX_BUY_VALUE_BY_LIQUIDITY = 0.06; //0.08; -> MUCHAS WhL y WZ!!
	public static double CAPITAL_DECREMENT_TO_SELL_ALL = 0.2;
	public static double IMPULSIVE_INCREMENTATION = 2;
	public static double MAX_INCREMENT_DIVERSIFIER = 3;
	public static double BUY_PROFITABILITY[] = {-0.09,-0.03}; //{-0.13,-0.04}
	public static double BUY_PROBABILITY = 0.25; //0.85;
	public static double SELL_PROBABILITY = 0.9;
	public static double RAND_INV_BUY_PROBABILITY = 0.11;  
		//According to the programation, it has to depend of the share size of Ibex35
	public static double RAND_INV_SELL_PROBABILITY = 0.05;  //0.15
	
	public static double OPERATION_CLOSED_WEIGHT = 0.2;
	public static double OPERATION_OPENED_WEIGHT = 0.2;
	public static double OPERATIONS_WEIGHT = 0.5;
	public static double CAPITAL_INCREMENT_WEIGHT = 0.5;
	public static double IF_RENTABILITY_NEGATIVE_DECREMENT = 1.5;	
	
	public static int NUM_INVESTORS = 400;
	
	//Make reads and comments
	public static double CONSECUTIVE_MESSAGE_CRONOLOGY_READ_DEGRADATION = 0.94; //0.93; 0.9;
	//public static double CONSECUTIVE_MESSAGE_RECOMMENDATION_READ_DEGRADATION = 0.9;
	public static int NOT_READ_MESSAGES_TO_LEAVE_USER = 12;
	public static double BAD_MESSAGE_DEGRADATION = 0.20; //0.25;
	public static double ALREADY_READ_MESSAGE = 0.2;
	public static double ALREADY_COMMENTED_MESSAGE = 0.2;
	public static double ALREADY_SCORED_MESSAGE = 0.2;
	
	//Make Friends
	public static int FRIEND_DEGRADATION = (int) Math.log10(NUM_INVESTORS) * 15;
	public static double FRIENDLY_PROBABILITY = 0.5;
	public static double FRIENDLY_COMMON = 0.3;
	public static double FRIENDLY_PROBABILITY_LIMITS[] = {0.04,0.08}; //{0.04,0.06};
	public static double NO_FRIENDLY_PROBABILITY_LIMITS[] = {0.025,0.04}; //{0.015,0.02};
	public static double LINEAL_FRIEND_STRENGH_INCREMENT = 3;
	public static double EXPONENCIAL_FRIEND_STRENGH_DECREMENT = 3;
	
	//Activity Reputation Sets
	public static double COMMON_FRIEND_WEIGHT[] = {3,1,8};
	public static double NO_COMMON_FRIEND_WEIGHT[] = {7,9,2};
	public static double FRIEND_DEGRADATION_FACTOR[] = {0, Math.E};
	public static int MINIMUM_MESSAGES_TO_DEGRADATE[] = {10,50};
	public static double MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR[] = {0,Math.E}; //0	= none
	public static double FRIEND_WEIGHT = 1;
	public static double MESSAGE_WEIGHT = 1;
	
	public static int STOCK_MEMORY = 5;
}
