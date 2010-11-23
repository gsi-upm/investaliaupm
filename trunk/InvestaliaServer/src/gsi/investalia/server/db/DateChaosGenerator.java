package gsi.investalia.server.db;

import gsi.investalia.domain.Message;

import java.sql.Timestamp;
import java.util.Date;

/* This class disorders the dates to see if the ability
 * of downloading by date works correctly
 */
public class DateChaosGenerator {
	private static final Date END_DATE = new Date();
	private static final Date INI_DATE = new Date(END_DATE.getTime() - 1000*60*60*24*3000);
	
	public static void generateChaos() {
		System.out.println("Disorder dates");
		long iniTime = INI_DATE.getTime();
		long length = END_DATE.getTime() - iniTime + 1;
		for(int i = 1; i < 1060; i++) {
			Message m = Message.getZeroMessage();
			m.setId(i);
			long newTime = iniTime + (long) (length * Math.random());
			m.setDate(new Date(newTime));
			System.out.println(new Timestamp(m.getDate().getTime()));
			MysqlInterface.updateDate(m);
		}
	}
}
