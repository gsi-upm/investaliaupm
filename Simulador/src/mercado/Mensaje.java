package mercado;

import java.util.ArrayList;
import java.util.HashSet;

public class Mensaje {
	/**
	 * One comment has the owner (the body, but it doesn't care),
	 * and one list of followers, not Set because we want one agent can post
	 * two or more times
	 */
	//igual es demasiado poner el owner como un objeto inversor tan grande.
	// dependerá si queremos extraer las acciones... o con el id basta
	private Inversores owner;
	private int date;
	protected ArrayList<Inversores> followers;
	private HashSet<Inversores> uniqueFollowers;	
	private ArrayList<Inversores> readers;
	private HashSet<Inversores> uniqueReaders;
	private boolean good;
	private double popularity[];
	private double reputation[];
	public static int ONLY_FOLLOWER_REPUTATION = 1;
	public static int READER_FOLLOWER_REPUTATION = 0;
	public static int READER_FOLLOWER_FINANCIAL_REPUTATION = 2; 
	
	
	//String body, not implemented
	
	public Mensaje(Inversores  owner, int date, boolean good){
		this.owner = owner;
		this.date = date;
		this.good = good;
		followers = new ArrayList<Inversores>();
		readers = new ArrayList<Inversores>();
		uniqueFollowers = new HashSet<Inversores>();
		uniqueReaders = new HashSet<Inversores>();
		popularity = new double[1];
		reputation = new double[1];
	}
	
	public int generateReputation(int time) {
		int time_difference = (time - getDate()) / Properties.TIME_CLUSTER;
		//reputation[ONLY_FOLLOWER_REPUTATION] = getUniqueNumFollowers()* Math.pow(Mensaje.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);		
		
		reputation[READER_FOLLOWER_REPUTATION] = (getUniqueNumReaders() * Properties.READER_WEIGHT 
				+ getUniqueNumFollowers() * Properties.FOLLOWER_WEIGHT ) *
			Math.pow(Properties.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);
		
		/*
		reputation[READER_FOLLOWER_FINANCIAL_REPUTATION] = getUniqueNumReaders();
		for (Inversores inversor: uniqueFollowers)
			reputation[READER_FOLLOWER_FINANCIAL_REPUTATION] +=  inversor.getFinancialReputation();
		reputation[READER_FOLLOWER_FINANCIAL_REPUTATION] *= Math.pow(Mensaje.CRONOLOGY_DREGADATION_EXPONENCIAL_FACTOR,-time_difference);
		*/
		for(int i = 0; i < popularity.length; i++)
			popularity[i] = 1;
		return time_difference;
	}
	
	/**
	 * Add a comment
	 * @param inversorPosteador
	 */
	public void addComment(Inversores inversorPosteador){	
		//System.out.print(", el inversor: " + inversorPosteador.getId() +
		//		" se ha añadido como follower de : " + owner.getId());
		followers.add(inversorPosteador);
		uniqueFollowers.add(inversorPosteador);
	}	
	public void addReader(Inversores inversorReader){	
		//System.out.print(", el inversor: " + inversorReader.getId() +
		//		" se ha añadido como reader de : " + owner.getId());
		readers.add(inversorReader);
		uniqueReaders.add(inversorReader);
	}
	
	public ArrayList<Inversores> getReaders() {
		return readers;
	}
	public int getNumReaders() {
		return readers.size();
	}
	
	public double[] getReputation() {
		return reputation;
	}
	
	public ArrayList<Inversores> getFollowers(){
		return followers;
	}
	public int getNumFollowers(){
		return followers.size();
	}
	
	public int getUniqueNumFollowers () {
		/*HashSet<Inversores> uniqueFollowers = new HashSet<Inversores>();
		for(Inversores follower : followers) {
			uniqueFollowers.add(follower);
		}*/
		return uniqueFollowers.size();
	}
	
	public HashSet<Inversores> getUniqueFollowers () {
		return uniqueFollowers;
	}
	
	public int getUniqueNumReaders () {
		/*HashSet<Inversores> uniqueReaders = new HashSet<Inversores>();
		for(Inversores reader : readers) {
			uniqueReaders.add(reader);
		}*/
		return uniqueReaders.size();
	}
	
	public HashSet<Inversores> getUniqueReaders () {
		return uniqueReaders;
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
	private void setDate(int date) {
		this.date = date;
	}
	public int getDate() {
		return date;
	}
	public boolean isGood() {
		return good;
	}
	private void setGood(boolean good) {
		this.good = good;
	}	
	
}
