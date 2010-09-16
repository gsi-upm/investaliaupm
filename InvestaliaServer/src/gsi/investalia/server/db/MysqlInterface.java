package gsi.investalia.server.db;

import es.upm.multidimensional.RecommendationGenerator;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ListModel;

/**
 * Saves and gets all the needed information of the database. It works
 * with the domain classes.
 * @author luis
 */
public class MysqlInterface {
	private static Connection con;
	private static Statement stmt;

	/**
	 * Connects to the database
	 */
	public static void connectToDatabase() {
		try {
			String url = "jdbc:mysql://localhost/";
			String dbName = "investalia";
			String userName = "user";
			String pass = "pass";

			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(
					url+dbName, userName, pass);
			stmt = con.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves a new message
	 */
	public static void saveMessage(Message message) {
		connectToDatabase();

		// Save the message itself
		String query = "INSERT INTO messages VALUES (Null, ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setInt(1, getUser(message.getUserName()).getId());
			prep.setString(2, message.getTitle());
			prep.setString(3, message.getText());
			prep.setTimestamp(4, new Timestamp (message.getDate().getTime()));
			prep.setInt(5, 0); // Rating = 0 at start
			prep.setInt(6, 0); // Not still read
			prep.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Set the id generated by the db
		setMessageId(message);

		// Save the tags
		query = "INSERT INTO messages_tags VALUES (Null, ?, ?)";
		try {
			prep = con.prepareStatement(query);
			for (Tag tag : message.getTags()) {
				prep.setInt(1, message.getId());
				prep.setInt(2, tag.getId());
				prep.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnectionDatabase();
	}

	/**
	 * Updates the read and liked properties for a message list and user
	 */
	public static void updateReadAndLiked(List<Message> messages) {
		for (Message m : messages) {
			updateReadAndLiked(m);
		}
	}

	/**
	 * Updates the read and liked properties for a message and user
	 */
	public static boolean updateReadAndLiked(Message m) {
		
		int idUser = m.getIdUserUpdating();
		Message oldMessage = getMessage(m, idUser);
		// If the "new message" is not read, the method does nothing
		if (!m.isRead()) {
			return true;
		}
		// New and old message read
		if (oldMessage.isRead()) {
			try {
				// If liked does not change, the method does nothing
				if (m.isLiked() == oldMessage.isLiked()) {
					return true;
				}
				connectToDatabase();
				// Liked has changed, update the db
				stmt.executeUpdate("UPDATE users_messages"
						+ " SET liked = " + (m.isLiked()?1:0)
						+ " WHERE idmessage = " + m.getId() + " AND idUser = " + idUser);
				closeConnectionDatabase();
				/*
				// Old disliked and new likes: rating++
				if (m.isLiked()) {
					stmt.executeUpdate("UPDATE messages "
							+ "SET rating = (rating + 1) WHERE idMessage = "
							+ m.getId());
				}
				// Old liked and new dislikes: rating--
				else {
					stmt.executeUpdate("UPDATE messages "
							+ "SET rating = (rating - 1) WHERE idMessage = "
							+ m.getId());
				}
				 */
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			try {
				connectToDatabase();
				// Increase timesRead
				stmt.executeUpdate("UPDATE messages"
						+ " SET times_read = (times_read + 1)"
						+ " WHERE idMessage = " + m.getId());
				// Add row in read table
				stmt.executeUpdate("INSERT INTO users_messages VALUES (Null, " + m.getId()
						+ ", " + idUser + ", " + (m.isLiked()?1:0) + ", Null)");
				/*
				// If liked, rating++
				if (m.isLiked()) {
					stmt.executeUpdate("UPDATE messages "
							+ "SET rating = (rating + 1) WHERE idMessage = "
							+ m.getId());
				}
				 */
				closeConnectionDatabase();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
	}


	/**
	 * Gets a user by its userName
	 */
	public static User getUser(String userName) {
		return getUserFromQuery("SELECT * FROM users WHERE userName = '"
				+ userName + "'");
	}

	/**
	 * Gets a user by its id
	 */
	public static User getUser(int idUser) {
		return getUserFromQuery("SELECT * FROM users WHERE idUser = " + idUser);
	}

	/**
	 * Gets a user by its userName and password
	 */
	public static User getUser(String userName, String password) {
		return getUserFromQuery("SELECT * FROM users WHERE userName = '"
				+ userName + "' AND password = '" + password + "'");
	}

	public static boolean saveNewUser(User user) {
		
		// Check the username is not already used
		User alreadyRegisteredUser = getUser(user.getUserName());
		if (alreadyRegisteredUser != null) {
			System.out.println("Already registered user");
			return false;
		}

		connectToDatabase();
		boolean saved = false;
		// Save the user
		String query = "INSERT INTO users values (Null, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setString(1, user.getUserName());
			prep.setString(2, user.getPassword());
			prep.setString(3, user.getName());
			prep.setString(7, user.getLocation());
			prep.setString(8, user.getEmail());
			prep.executeUpdate();
			saved=true;
		} catch (SQLException e) {
			System.out.println("SQL exception inserting new user");
		}
		closeConnectionDatabase();
		return saved;
	}
	
	public static boolean updateUser(User user) {
		
		// User with the new username from database
		User newUsernameUser = getUser(user.getUserName());
		// Check if the new username (if changed) is not used
		if (newUsernameUser != null && newUsernameUser.getId() != user.getId()) {
			return false;
		}
		
		connectToDatabase();
		
		// Update the user
		String query = "UPDATE users SET username=?, password=?, name=?, location=?, email=? WHERE iduser=?";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);
			prep.setString(1, user.getUserName());
			prep.setString(2, user.getPassword());
			prep.setString(3, user.getName());
			prep.setString(4, user.getLocation());
			prep.setString(5, user.getEmail());
			prep.setInt(6, user.getId());
			prep.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL exception updating user details");
			return false;
		}

		// Delete old tags
		query = "DELETE FROM users_tags WHERE iduser = ?";
		try {
			prep = con.prepareStatement(query);
			prep.setInt(1, user.getId());
			prep.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQL exception deleting the old tags");
			return false;
		}

		// Save the new tags
		query = "INSERT INTO users_tags VALUES (Null, ?, ?)";
		try {
			prep = con.prepareStatement(query);
			for(Tag tag: user.getTagsFollowing()) {
				prep.setInt(1, user.getId());
				prep.setInt(2, tag.getId());
				prep.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println("SQL exception deleting the old tags");
			return false;
		}
		
		closeConnectionDatabase();
		return true;
	}

	/**
	 * Gets the list of all the messages that a user is following
	 */
	public static List<Message> getAllUserMessages(String userName) {
		int idUser = getUser(userName).getId();
		return getMessagesFromQuery("SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt WHERE m.idmessage = mt.idmessage AND idtag IN (SELECT idtag FROM users_tags WHERE iduser = " 
				+ idUser + ")", idUser);
	}

	/**
	 * Gets the list of the messages that a user is following after one given
	 */
	public static List<Message> getUserMessagesSinceLast(String userName,
			int idMessageLast) {
		int idUser = getUser(userName).getId();
		String query = "SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt WHERE m.idmessage = mt.idmessage AND idtag IN (SELECT idtag FROM users_tags WHERE iduser = " 
			+ idUser + ") AND m.idmessage > " + idMessageLast;
		return getMessagesFromQuery(query, idUser);
	}

	/**
	 * Gets a message with the read and liked attributes for a given user
	 */
	private static Message getMessage(Message m, int idUser) {
		connectToDatabase();
		Message message = null;
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM messages WHERE idMessage = " + m.getId());
			if (rs.next()) {
				message = getMessageFromRS(rs, idUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return message;
	}

	private static List<Message> getMessagesFromQuery(String query, int idUser) {
		connectToDatabase();
		List<Message> messages = new ArrayList<Message>();
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				messages.add(getMessageFromRS(rs, idUser));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
		return messages;
	}

	private static List<Tag> getTagListFromQuery(String query) {
		connectToDatabase();
		List<Tag> tags = new ArrayList<Tag>();
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				tags.add(new Tag(rs.getInt(1), rs.getString(2)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tags;
	}

	private static User getUserFromQuery(String query) {
		connectToDatabase();
		User user = null;
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				user = new User(rs.getInt(1), rs.getString(2), rs.getString(3),
						rs.getString(4), rs.getString(5), rs.getString(6),
						getTagsFollowing(rs.getInt(1)));
				// Constructor:
				// id,userName,password,name,location,email,tagsFollowing,lastUpdate
				// Db: IDUSER,USERNAME,PASSWORD,NAME,LOCATION,EMAIL
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	private static List<Tag> getTagsFollowing(int idUser) {
		return getTagListFromQuery("SELECT t.* FROM users_tags AS ut, tags AS t WHERE t.idTag = ut.idTag AND idUser = "
				+ idUser);
	}

	private static List<Tag> getMessageTags(int idMessage) {
		return getTagListFromQuery("SELECT t.* FROM messages_tags AS mt, tags AS t WHERE t.idTag = mt.idTag AND idMessage = "
				+ idMessage);
	}

	public static List<Tag> getTagsSinceLast(int idLastTag) {
		return getTagListFromQuery("SELECT * FROM tags WHERE idtag > "
				+ idLastTag);
	}

	private static void setMessageId(Message message) {
		connectToDatabase();

		String query = "SELECT idMessage" +
		" FROM messages " +
		" WHERE iduser = " + getUser(message.getUserName()).getId() +
		" AND text = '" + message.getText() + "'" +
		" ORDER BY idmessage DESC LIMIT 1;";
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				message.setId(rs.getInt("idmessage"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks and sets the read and liked attribute for a given message and user
	 */
	private static void setReadAndLiked(Message m, int idUser) {
		connectToDatabase();

		try {
			ResultSet rs = stmt
			.executeQuery("SELECT liked FROM users_messages WHERE iduser = "
					+ idUser + " AND idmessage = " + m.getId());
			if (rs.next()) {
				m.setRead(true);
				if(rs.getInt(1) == 1)
					m.setLiked(true);
				else
					m.setLiked(false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
	}

	private static Message getMessageFromRS(ResultSet rs, int idUser) {
		// Create the message; read and liked false by default
		Message m;
		try {
			m = new Message(rs.getInt(1), getUser(rs.getInt(2)).getUserName(),
					rs.getString(3), rs.getString(4), getMessageTags(rs
							.getInt(1)), new Date(rs.getTimestamp(5).getTime()), false, false, rs
							.getInt(6), rs.getInt(7), idUser);
		} catch (SQLException e) {
			System.out.println("SQL Exception");
			return null;
		}
		// Set the read and liked properties
		setReadAndLiked(m, idUser);
		return m;
		// Constructor:
		// id,userName,title,text,tags,date,read,liked,rating,timesRead
		// Db: IDMESSAGE,IDUSER,TITLE,TEXT,DATE,RATING,TIMES_READ
	}

	private static HashMap<Long, HashMap<Long,Float>> getUsersMessagesReadAndLiked (int idUser){
		connectToDatabase();

		HashMap<Long, HashMap<Long,Float>> userHashMap = null;
		HashMap<Long,Float> auxHashMapRead = new HashMap<Long,Float>();
		HashMap<Long,Float> auxHashMapLiked = new HashMap<Long,Float>();

		try {
			ResultSet rs = stmt.executeQuery("SELECT idmessage liked FROM users_messages WHERE iduser = " + idUser);
			while (rs.next()) {
				if(rs.getInt("users_messages.liked") == 0)
					auxHashMapRead.put(Long.valueOf(rs.getInt("users_messages.idmessage")), new Float(1));
				else
					auxHashMapLiked.put(Long.valueOf(rs.getInt("users_messages.idmessage")), new Float(1));
			}
			
			userHashMap = new HashMap<Long, HashMap<Long,Float>>();
			userHashMap.put(Long.valueOf(idUser), auxHashMapRead);
			userHashMap.put(Long.valueOf(idUser), auxHashMapLiked);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnectionDatabase();
		return userHashMap;
	} 

	public static void takeRecommendationData (HashMap<String,Object> recommendationData,
			String dimensions[],String table, String userColumn,String messageColumn, String likedColumn) {		

		connectToDatabase();

		String query = "SELECT "+userColumn+","+messageColumn+","+likedColumn+
		" from "+table+" order by "+userColumn+","+messageColumn;
		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int user = rs.getInt(userColumn);
				int message = rs.getInt(messageColumn);
				int liked = rs.getInt(likedColumn);
				HashMap<Long,HashMap<Long,Float>> readData = 
					(HashMap<Long,HashMap<Long,Float>>) recommendationData.get(dimensions[0]);
				HashMap<Long,Float> hashMapByUid = (HashMap<Long,Float>)readData.get(Long.valueOf(user));
				if (hashMapByUid == null) {
					HashMap<Long,Float> newUserHashMap = new HashMap<Long,Float>();
					newUserHashMap.put(Long.valueOf(message), 1f);
					readData.put(Long.valueOf(user), newUserHashMap);			      
				} else
					hashMapByUid.put(Long.valueOf(message), Float.valueOf(liked));
				if(liked != 0) {
					HashMap<Long,HashMap<Long,Float>> likedData = 
						(HashMap<Long,HashMap<Long,Float>>) recommendationData.get(dimensions[1]);
					hashMapByUid = (HashMap<Long,Float>)likedData.get(Long.valueOf(user));
					if (hashMapByUid == null) {
						HashMap<Long,Float> newUserHashMap = new HashMap<Long,Float>();
						newUserHashMap.put(Long.valueOf(message), Float.valueOf(liked));
						likedData.put(Long.valueOf(user), newUserHashMap);			      
					} else
						hashMapByUid.put(Long.valueOf(message), Float.valueOf(liked));
				}			    
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnectionDatabase();
	}

	public static void updateRecommendationData (RecommendationGenerator recommender,String dimensions[], 
			Date date, String table, String userColumn,String messageColumn, String likedColumn, String updateDate) {
		connectToDatabase();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String query = "SELECT "+userColumn+","+messageColumn+","+likedColumn+
		" from "+table+" where "+updateDate+" > \'"+dateFormat.format(date)+"\' order by "+userColumn+","+messageColumn;
		try {
			System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int user = rs.getInt(userColumn);
				int message = rs.getInt(messageColumn);
				int liked = rs.getInt(likedColumn);
				System.out.println(user+","+message+","+liked);
				try { Thread.sleep(1000);} catch (Exception e) {}
				recommender.putRating(dimensions[0], user, message, 1);
				if(liked != 0)
					recommender.putRating(dimensions[1], user, message, liked);		    
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnectionDatabase();
	}

	public static void saveUserRecommendationData (int userID, HashMap<Long,Float> userRecommendations){

		String query = "INSERT INTO users_recommendations VALUES (Null, ?, ?, ?)";
		PreparedStatement prep;
		try {
			prep = con.prepareStatement(query);

			prep.setInt(2, userID);

			Iterator<Long> recomendationsIterator = userRecommendations.keySet().iterator();
			while (recomendationsIterator.hasNext()) {
				Long idMessage = recomendationsIterator.next();
				prep.setInt(1, idMessage.intValue());
				prep.setFloat(3, userRecommendations.get(idMessage));
				prep.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteUserRecommendationData (int userID){

		try {
			stmt.executeUpdate("DELETE FROM users_recommendations" +
								" WHERE idUser = " + userID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void updateUserRecommendationData (int userID, HashMap<Long,Float> userRecommendations){
		
		connectToDatabase();
		
		deleteUserRecommendationData(userID);
		
		saveUserRecommendationData(userID, userRecommendations);
		
		closeConnectionDatabase();

	}

	public static HashMap<Long,Float> getUserRecommendationData (String userName){

		connectToDatabase();

		HashMap<Long, Float> userRecommendationData = new HashMap<Long, Float>();

		String query = "SELECT idMessage, user_affinity" +
		" FROM users_recommendations" +
		" WHERE idUser = " + getUser(userName).getId();

		try {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				userRecommendationData.put(
						new Long(rs.getInt("idMessage")),
						rs.getFloat("user_affinity"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		closeConnectionDatabase();
		return userRecommendationData;
	}
		
		public static Float getUserRecommendationForAMessage (int userID, long idMessage){

			String query = "SELECT idMessage, user_affinity" +
			" FROM users_recommendations" +
			" WHERE idUser = " + userID + " AND idMessage = " + idMessage;

			try {
				ResultSet rs = stmt.executeQuery(query);
				if (rs.next()) {
					return rs.getFloat("user_affinity");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

		return new Float(0);
	}

	public static void closeConnectionDatabase() {
		try {
			if(stmt != null)
				stmt.close();
			if(con != null)
				con.close();
		} catch (SQLException e) {

		}
	}
}