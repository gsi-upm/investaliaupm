package mercado;

public class Properties {
	public static double STOCK_VARIATION = 0.92;	
		//Bear Market -> variation *= 0.X, Bull Market -> variation *= 1.X
	public static double LINEAL_REVERSE_SHARE_LIMIT = 2;
	
	public static double INTELLIGENT_INVESTOR_PROBABILITY = 0.5;
	public static double AMATEUR_INVESTOR_PROBABILITY = 0.25;
	public static double RANDOM_INVESTOR_PROBABILITY = 0.25;
	public static double IMPULSIVE_PROBABILITY = 0.5;
	public static double PERCEPTION_PROBABILITY = 0.5;
	public static double ANXIETY_PROBABILITY = 0.5;
	public static double MEMORY_PROBABILITY = 0.5;
	public static double DIVERSIFICATION_PROBABILITY = 0.5;
	
	public static double GOOD_WRITER_PROBABILITY = 0.5;
	public static double GOOD_MESSAGES_PROBABILITY_LIMITS[] = {0.7,0.9};
	public static double BAD_MESSAGES_PROBABILITY_LIMITS[] = {0,0.2};
	public static Integer GOOD_MESSAGES_SCORE_PROBABILITY[] = {3,5};
	public static Integer BAD_MESSAGES_SCORE_PROBABILITY[] = {0,3};
	
	public static double FREQUENT_USER_PROBABILITY = 0.5;
	public static double FREQ_USER_READ_PROBABILITY_LIMITS[] = {0.6,0.9};
	public static double FREQ_USER_POST_PROBABILITY_LIMITS[] = {0.2,0.36};
	public static double FREQ_USER_COMMENT_PROBABILITY_LIMITS[] = {0.08,0.14};
	public static double FREQ_USER_SCORE_PROBABILITY_LIMITS[] = {0.1,0.34};
	public static double OCA_USER_READ_PROBABILITY_LIMITS[] = {0.05,0.3};
	public static double OCA_USER_POST_PROBABILITY_LIMITS[] = {0.03,0.08};
	public static double OCA_USER_COMMENT_PROBABILITY_LIMITS[] = {0.01,0.04};
	public static double OCA_USER_SCORE_PROBABILITY_LIMITS[] = {0.1,0.3};
	
	public static int MINIMUM_MESSAGES_TO_DEGRADATE = 10;
	public static double MESSAGE_DEGRADATION_LOGARITHMIC_FACTOR = Math.E; //10
	public static double CONSECUTIVE_MESSAGE_CRONOLOGY_READ_DEGRADATION = 0.9;
	public static double CONSECUTIVE_MESSAGE_RECOMMENDATION_READ_DEGRADATION = 0.9;
	public static int NOT_READ_MESSAGES_TO_LEAVE_USER = 10;
	
	public static double POPULARITY_INCREMENTATION_LINEAL_FACTOR = 4;
	public static double POPULARITY_INCREMENTATION_EXPONENCIAL_FACTOR = 2;
	public static double BAD_MESSAGE_DEGRADATION = 0.25;
	public static double ALREADY_READ_MESSAGE = 0.2;
	public static double ALREADY_COMMENTED_MESSAGE = 0.2;
	public static double ALREADY_SCORED_MESSAGE = 0.2;
	public static int TIME_CLUSTER = 100;
	public static int MAX_DIFFERENCE_CLUSTERS = 6;
	public static int TIME_LIMIT = TIME_CLUSTER * MAX_DIFFERENCE_CLUSTERS;
	
	public static double CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR = 1.5;	
	public static double READER_WEIGHT = 0.3;
	public static double FOLLOWER_WEIGHT = 0.7;
	public static double SCORER_WEIGHT = 0.5;
	
	public static int STATISTICS_INTERVAL = 100;
	public static int CLEAN_INTERVAL = 1000;
	public static int MESSAGE_TIME_TO_CLEAN = 10000;
	
	public static double NEIGHBOR_DISTANCE_TO_PLAY = 4;
	//Subdividir NEIGHBOR_DISTANCE_TO_PLAY en PLAY_OCASIONAL y PLAY_FREQUENT
	public static double NEIGHBOR_DISTANCE_EXPONENCIAL_DEGRADATION = 0.7;
	
	
	public static double PERCEPTION_DEGRADATION = 4;
	public static double[][] anxietySellTable = {{0.01,0},{0.02,100}};
	public static double[] anxietySellAll = {0,3};
	//public static double[][] sellTable = {{0,0},{1,3},{2,7},{3,20},{4,50},{5,100}};
	public static double[][] sellTable = {{0.03,0},{0.06,3},{0.1,7},{0.14,14},{0.21,25},{0.5,100}};
	public static double[] sellAll = {4,6};
	    //TODO: depender sellAll de Intelligent de la liquidez que tiene (si poca, vender)
	public static double[][] sellAmateurTable = null; //{{0.01-0.05,100}};
	public static double[] sellAmateurRange = {0.01, 0.28};
	public static double[] sellAmateurAll = {1,3};
	public static int INITIAL_LIQUIDITY = 10000;
	public static double MAX_BUY_VALUE = INITIAL_LIQUIDITY * 0.1;
	public static double IMPULSIVE_INCREMENTATION = 2;
	public static double BUY_PROFITABILITY[] = {-0.13,-0.04};
	public static double BUY_PROBABILITY = 0.8;
	public static double SELL_PROBABILITY = 0.7;
	public static double RAND_INV_BUY_PROBABILITY = 0.15;
	public static double RAND_INV_SELL_PROBABILITY = 0.15;
	
	
	public static int NUM_INVERSORES = 400;
	public static int FRIEND_DEGRADATION = (int) Math.log10(NUM_INVERSORES) * 15;
	public static double FRIENDLY_PROBABILITY = 0.5;
	public static double FRIENDLY_COMMON = 0.3;
	public static double FRIENDLY_PROBABILITY_LIMITS[] = {0.04,0.06};
	public static double NO_FRIENDLY_PROBABILITY_LIMITS[] = {0.015,0.02};
	public static double LINEAL_FRIEND_STRENGH_INCREMENT = 3;
	public static double EXPONENCIAL_FRIEND_STRENGH_DECREMENT = 2;
	
	public static int STOCK_MEMORY = 5;
}
