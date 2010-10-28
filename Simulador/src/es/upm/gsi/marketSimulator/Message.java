package es.upm.gsi.marketSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Message {
	/**
	 * One comment has the owner (the body, but it doesn't care),
	 * and one list of followers, not Set because we want one agent can post
	 * two or more times
	 */
	//igual es demasiado poner el owner como un objeto inversor tan grande.
	// dependerá si queremos extraer las acciones... o con el id basta
	private Investors owner;
	private int date;
	protected ArrayList<Investors> followers;
	private HashSet<Investors> uniqueFollowers;	
	private ArrayList<Investors> readers;
	private HashSet<Investors> uniqueReaders;
	private HashMap<Investors, Integer> scores;
	private int totalScore = 0;
	private boolean good;
	private double popularity[];
	private double reputation[];
	public static int ONLY_FOLLOWER_REPUTATION = 1;
	public static int READER_FOLLOWER_SCORER_REPUTATION = 0;
	public static int READER_FOLLOWER_REPUTATION = 3;
	public static int READER_FOLLOWER_FINANCIAL_REPUTATION = 2; 
		
	//String body, not implemented
	
	public Message(Investors  owner, int date, boolean good){
		this.owner = owner;
		this.date = date;
		this.good = good;
		followers = new ArrayList<Investors>();
		readers = new ArrayList<Investors>();
		uniqueFollowers = new HashSet<Investors>();
		uniqueReaders = new HashSet<Investors>();
		scores = new HashMap<Investors, Integer>();
		popularity = new double[1];
		reputation = new double[1];
	}
	
	public int generateReputation(int time) {
		int time_difference = (time - getDate()) / Properties.TIME_CLUSTER;
		
		//reputation[READER_FOLLOWER_SCORER_REPUTATION] = (getUniqueNumReaders() * 
		//	Properties.READER_WEIGHT + getUniqueNumFollowers() * Properties.FOLLOWER_WEIGHT 
		//	+ getScore() * Properties.SCORER_WEIGHT);
			//*	Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference); 
			// |-> Se hace en generateReputation de la agregaci�n de todos los mensajes
		
		reputation[READER_FOLLOWER_SCORER_REPUTATION] = getUniqueNumReaders() * 
				Properties.READER_WEIGHT + getUniqueNumFollowers()* Properties.FOLLOWER_WEIGHT;
		if(scores.size() > 0) {
			double trust_degree;			
			if(scores.size() >= Properties.USERS_TO_MAXIMUM_TRUST)
				trust_degree = 1;	//Between log10(2,5) and log10(5)
			else
				trust_degree = Math.log10(scores.size() * Properties.TRUST_WEIGHT);
			reputation[READER_FOLLOWER_SCORER_REPUTATION] += Properties.SCORER_WEIGHT * 
					reputation[READER_FOLLOWER_SCORER_REPUTATION] * trust_degree 
					* (totalScore/scores.size() - Properties.MAXIMUM_SCORE/2);
		}
		
		
		//reputation[ONLY_FOLLOWER_REPUTATION] = getUniqueNumFollowers()* Math.pow(Message.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);		
		
		//reputation[READER_FOLLOWER_REPUTATION] = (getUniqueNumReaders() * Properties.READER_WEIGHT 
		//		+ getUniqueNumFollowers() * Properties.FOLLOWER_WEIGHT ) *
		//	Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);
		
		/*
		reputation[READER_FOLLOWER_SCORER_FINANCIAL_REPUTATION] = 
				getUniqueNumReaders() * Properties.READER_WEIGHT;
		double followerReputation = 0;
		for (Investors inversor: uniqueFollowers)
			followerReputation +=  inversor.getFinancialReputation();
		reputation[READER_FOLLOWER_FINANCIAL_REPUTATION] += followerReputation * Properties.FOLLOWER_WEIGHT;
		double trust_degree = Math.min(2,getScore,descomentado ());
		double scorerReputation = trust_degree * (totalScore Properties.MAXIMUM_SCORE/2) * Properties.SCORER_WEIGHT;
		reputation[READER_FOLLOWER_FINANCIAL_REPUTATION] += Properties.SCORER_WEIGHT * 
					reputation[READER_FOLLOWER_SCORER_REPUTATION] * scorerReputation;
		*/
		for(int i = 0; i < popularity.length; i++)
			popularity[i] = 1;
		return time_difference;
	}
	
	/**
	 * Add a comment
	 * @param inversorPosteador
	 */
	public void addComment(Investors inversorPosteador){	
		//System.out.print(", el inversor: " + inversorPosteador.getId() +
		//		" se ha añadido como follower de : " + owner.getId());
		followers.add(inversorPosteador);
		uniqueFollowers.add(inversorPosteador);
	}	
	public void addReader(Investors inversorReader){	
		//System.out.print(", el inversor: " + inversorReader.getId() +
		//		" se ha añadido como reader de : " + owner.getId());
		readers.add(inversorReader);
		uniqueReaders.add(inversorReader);
	}
	public void addScore(Investors inversorScorer, Integer score){
		if(scores.containsKey(inversorScorer)) {			
			totalScore -= scores.get(inversorScorer);			
		}
		scores.put(inversorScorer, score);
		totalScore += score;
	}	
	
	public ArrayList<Investors> getReaders() {
		return readers;
	}
	public int getNumReaders() {
		return readers.size();
	}
	
	public double[] getReputation() {
		return reputation;
	}
	
	public ArrayList<Investors> getFollowers(){
		return followers;
	}
	public int getNumFollowers(){
		return followers.size();
	}
	
	
	public int getUniqueNumFollowers () {
		/*HashSet<Investors> uniqueFollowers = new HashSet<Investors>();
		for(Investors follower : followers) {
			uniqueFollowers.add(follower);
		}*/
		return uniqueFollowers.size();
	}
	
	public HashSet<Investors> getUniqueFollowers () {
		return uniqueFollowers;
	}
	
	public int getUniqueNumReaders () {
		/*HashSet<Investors> uniqueReaders = new HashSet<Investors>();
		for(Investors reader : readers) {
			uniqueReaders.add(reader);
		}*/
		return uniqueReaders.size();
	}
	
	public HashSet<Investors> getUniqueReaders () {
		return uniqueReaders;
	}
	
	public double getScore () {
		return totalScore;
		//double totalScore = 0;
		//for(Integer score: scores.values())
		//	totalScore += score;	
		//return totalScore;
		
		//int trust_degree = 0;
		//for(Investors investor : scores.keySet()) {
		//	Integer score = scores.get(investor);
		//	totalScore += score * investor.getFinancialReputation();
		//	trust_degree += investor.getFinancialReputation();
		//}		
		//totalScore = totalScore/scores.size();
		//return trust_degree;
	}	
	
	public HashMap<Investors,Integer> getScores() {
		return scores;
	}
	
	public void setPopularity (int index, double popularity) {
		this.popularity[index] = popularity;
	}
	
	public double[] getPopularity () {
		return popularity;
	}
	
	public int getIdOwner(){
		return owner.getId();
	}
	public void setDate(int date) {
		this.date = date;
	}
	public int getDate() {
		return date;
	}
	public boolean isGood() {
		return good;
	}
	public void setGood(boolean good) {
		this.good = good;
	}	
	
}
