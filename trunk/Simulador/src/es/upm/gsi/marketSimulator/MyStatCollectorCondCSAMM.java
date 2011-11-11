package es.upm.gsi.marketSimulator;

import org.ascape.util.data.StatCollectorCondCSAMM;

public abstract class MyStatCollectorCondCSAMM extends StatCollectorCondCSAMM{	
	
	private static final long serialVersionUID = -2525835263554231425L;
	
	int index;
	int type;
	static final int ACTIVITY_REP = 0;
	static final int FRIEND_REP = 1;
	static final int MESSAGE_REP = 2;
	
	MyStatCollectorCondCSAMM(String name, int type, int index) {
		super();
		this.index = index;
		this.type = type;
		this.setName(getName(type, name, index));
	}
	
	public double getValue(Object object) {
		if(type == FRIEND_REP) {
			return ((Investors) object).getFriendReputation()[index];
		}
		if(type == MESSAGE_REP) {
			return ((Investors) object).getMessageReputation()[index];
		}
		return ((Investors) object).getActivityReputation()[index];
    }
	
	static public String getName(int type, String name, int i) {
		if(type == FRIEND_REP) {
			return "FrienRep "+name+i;
		}
		if(type == MESSAGE_REP) {
			return "MesssRep "+name+i;
		}
		return "ActivRep "+name+i;
	}
	
}
