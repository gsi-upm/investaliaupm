package gsi.investalia.server.apirest;



import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.server.db.MysqlInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MessagesFromAPI {

	/*Tokens of the authentication URL*/
	private final static String authenticate_TOKEN1 = "http://investalia.grupogesfor.com/pg/api/rest/json/?method=rest.authenticate";
	private final static String authenticate_TOKEN2 = "&username=";
	private final static String authenticate_TOKEN3 = "&password=";
	private final static String authenticate_succesful = "{\"status\":0,\"result\":true}";

	/*Tokens of the blogs URL*/
	private final static String blogs_TOKEN1 = "http://investalia.grupogesfor.com/pg/api/rest/json/?method=rest.blogs";
	private final static String blogs_TOKEN2 = "&time_update=";

	/*Tokens of the comments in the wall URL*/
	private final static String wall_TOKEN1 = "http://investalia.grupogesfor.com/pg/api/rest/json/?method=rest.annotations";
	private final static String wall_TOKEN2 = "&time_update=";

	/*Tokens of the comments in the blog URL*/
	private final static String comments_TOKEN1 = "http://investalia.grupogesfor.com/pg/api/rest/json/?method=rest.comments";
	private final static String comments_TOKEN2 = "&time_update=";
	
	/*Tokens of the ratings in the blog URL*/
	private final static String ratings_TOKEN1 = "http://investalia.grupogesfor.com/pg/api/rest/json/?method=rest.rate";
	private final static String ratings_TOKEN2 = "&time_update=";

	/**
	 * @param source URL to connect
	 * @return String with the response from the URL
	 * Makes a connection to the INVESTALIA API
	 */
	public static String connect(String source) {
		try {
			URL direction = new URL(source);
			URLConnection directionConnection = direction.openConnection();
			String inputLine;
			StringBuffer buff = new StringBuffer();
			BufferedReader dis = new BufferedReader(new InputStreamReader(directionConnection.getInputStream()));

			while ((inputLine = dis.readLine()) != null) {
				buff.append(inputLine);
			}
			String a = buff.toString();

			dis.close();
			return a;
		} catch (MalformedURLException me) {
			return ("MalformedURLException: " + me);
		} catch (IOException ioe) {
			return ("IOException: " + ioe);
		}
	}
	
	/**
	 * Elimina etiquetas HTML y pone acentos bien.
	 */
	public static String parseHTML(String input){
		return input
		.replaceAll("&aacute;", "á")
		.replaceAll("&Aacute;", "Á")
		.replaceAll("&eacute;", "é")
		.replaceAll("&Eacute;", "É")
		.replaceAll("&iacute;", "í")
		.replaceAll("&Iacute;", "Í")
		.replaceAll("&oacute;", "ó")
		.replaceAll("&Oacute;", "Ó")
		.replaceAll("&uacute;", "ú")
		.replaceAll("&Uacute;", "Ú")
		.replaceAll("\\<.*?>","");
	}

	/**
	 * Authenticates using the Investalia API
	 * @param username
	 * @param password
	 * @return true if the authentication is successful.
	 */
	public static boolean authenticateFromAPI(String username, String password){
		if(connect(authenticate_TOKEN1+authenticate_TOKEN2+username+authenticate_TOKEN3+password).equals(authenticate_succesful))
			return true;
		else return false;
	}

	public static List<Message> getBlogsFromAPI (String lastUpdate){
		/* Last Update es un número correspondiende con la última fecha que se actualizó */
		/*Ej: 0000000000 devuelve todos los mensajes */

		ArrayList<Message> msgs = new ArrayList<Message>();

		/* PARSEADO DEL XML , ya no es necesario.
		 * try{
	            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse (blogs_TOKEN1+blogs_TOKEN2+lastUpdate);//Conecta a través de la URI
	            doc.getDocumentElement ().normalize();
	            NodeList titles = doc.getElementsByTagName("title");
	            //doc.getDocumentElement().getChildNodes().item(0).get
	            //System.out.println(titles.item(0).getTextContent());


	        }catch(Exception e){
	        	System.err.println(e.getMessage());
	        }*/

		try{

			JSONObject result;
			String resultFromAPI = connect(blogs_TOKEN1+blogs_TOKEN2+lastUpdate);
			result = new JSONObject(resultFromAPI);
			result=result.getJSONObject("result");/*Get rid of the extra field */
			JSONArray blogs = result.getJSONArray("blogs"); /*Array of all the blogs since last update*/
			for(int i = 0;i<blogs.length();i++){
				int id =Integer.parseInt(blogs.getJSONObject(i).getString("guid"));
				String userName = blogs.getJSONObject(i).getString("owner_name");
				String title= blogs.getJSONObject(i).getString("title");
				String text = parseHTML(blogs.getJSONObject(i).getString("description"));
				List<Tag> tags = new ArrayList<Tag>();
				tags.add(new Tag(35, "Blog"));
				
				//We search if the tag of the blog is in the database.
				//If so, we add the tag to this blog' ones in the database
				//If not,we first insert the tag into the Tag database, and then to the blog one.
				try{
					List <Tag> allTags = MysqlInterface.getAllTags();
					List<String> names = new ArrayList<String>();
					for( Tag thisTag:allTags){
						names.add(thisTag.getTagName());
					}
					JSONArray tags_j = blogs.getJSONObject(i).getJSONArray("tags");
					for(int j=0;j<tags_j.length();j++){
						if(!names.contains(tags_j.getString(j))){
							MysqlInterface.insertTag(tags_j.getString(j), "description");
						}
						allTags = MysqlInterface.getAllTags();
						for( Tag thisTag:allTags){
							names.add(thisTag.getTagName());
						}
						int index = names.indexOf(tags_j.getString(j));	
						Tag tag = new Tag(index, tags_j.getString(j));
					}
				}
				catch(JSONException je){
					tags.add(new Tag(0,blogs.getJSONObject(i).getString("tags")));
				}

				Date date = new Date(Long.parseLong(blogs.getJSONObject(i).getString("time_created"))*1000L);
				boolean read =false;
				boolean liked = false;
				int rating;

				if(blogs.getJSONObject(i).getString("rate")!="null"){
					rating= Integer.parseInt(blogs.getJSONObject(i).getString("rate"));
				}else{
					rating = 0;
				}		

				int timesRead = 0; //Por defecto, 0
				double affinity = 0; 
				long idMessageAPI = 0; // TODO
				Message toBeAdded = new Message(id, userName, title, text,
						tags, date, read, liked,rating, timesRead, affinity,
						idMessageAPI);
				toBeAdded.setIdMessageAPI((long)id);
				msgs.add(toBeAdded);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return msgs;
	}

	public static List<Message> getNotesInTheWallFromAPI (String lastUpdate){
		/* Last Update es un número correspondiende con la última fecha que se actualizó */
		/*Ej: 0000000000 devuelve todos los mensajes */

		ArrayList<Message> msgs = new ArrayList<Message>();

		try{

			JSONObject result;
			String resultFromAPI = connect(wall_TOKEN1+wall_TOKEN2+lastUpdate);
			result = new JSONObject(resultFromAPI);
			result=result.getJSONObject("result");/*Get rid of the extra field */
			JSONArray blogs = result.getJSONArray("mensajes"); /*Array of all the annotations since last update*/
			for(int i = 0;i<blogs.length();i++){
				int id =Integer.parseInt(blogs.getJSONObject(i).getString("id"));
				String userName = blogs.getJSONObject(i).getString("owner_name");
				String title= "Anotación en el tablón de "+
				blogs.getJSONObject(i).getString("entity_guid");
				String text = parseHTML(blogs.getJSONObject(i).getString("value"));
				List<Tag> tags = new ArrayList<Tag>();
				tags.add(new Tag(37,"Muro"));

				Date date = new Date(Long.parseLong(blogs.getJSONObject(i).getString("time_created"))*1000L);
				boolean read =false;
				boolean liked = false;
				int rating=0;	

				int timesRead = 0; //Por defecto, 0
				double affinity = 0; 
				long idMessageAPI = 0; //TODO
				Message toBeAdded = new Message(id, userName, title, text, 
						tags, date, read, liked,rating, timesRead, affinity,
						idMessageAPI);
				toBeAdded.setIdMessageAPI((long)id);
				msgs.add(toBeAdded);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return msgs;
	}

	public static List<Message> getCommentsFromAPI (String lastUpdate){
		/* Last Update es un número correspondiende con la última fecha que se actualizó */
		/*Ej: 0000000000 devuelve todos los mensajes */

		ArrayList<Message> msgs = new ArrayList<Message>();

		try{

			JSONObject result;
			String resultFromAPI = connect(comments_TOKEN1+comments_TOKEN2+lastUpdate);
			result = new JSONObject(resultFromAPI);
			result=result.getJSONObject("result");/*Get rid of the extra field */
			JSONArray blogs = result.getJSONArray("comentarios"); /*Array of all the annotations since last update*/
			for(int i = 0;i<blogs.length();i++){
				int id =Integer.parseInt(blogs.getJSONObject(i).getString("id"));
				String userName = blogs.getJSONObject(i).getString("owner_name");
				String title= "Comentario sobre "+
				blogs.getJSONObject(i).getString("entity_guid");
				String text = parseHTML(blogs.getJSONObject(i).getString("value"));
				List<Tag> tags = new ArrayList<Tag>();
				tags.add(new Tag(36,"Comentarios"));

				Date date = new Date(Long.parseLong(blogs.getJSONObject(i).getString("time_created"))*1000L);
				boolean read =false;
				boolean liked = false;
				int rating=0;	

				int timesRead = 0; //Por defecto, 0
				double affinity = 0; 
				long idMessageAPI = 0; // TODO
				Message toBeAdded = new Message(id, userName, title, 
						text,tags, date, read, liked, rating,timesRead,
						affinity, idMessageAPI);
				toBeAdded.setIdMessageAPI((long)id);
				msgs.add(toBeAdded);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return msgs;
	}

	
	public static List<String[]> getRatings (String lastUpdate){
		/* Last Update es un número correspondiende con la última fecha que se actualizó */
		/*Ej: 0000000000 devuelve todos los mensajes */

		ArrayList<String[]> msgs = new ArrayList<String[]>();

		try{

			JSONObject result;
			String resultFromAPI = connect(ratings_TOKEN1+ratings_TOKEN2+lastUpdate);
			result = new JSONObject(resultFromAPI);
			result=result.getJSONObject("result");/*Get rid of the extra field */
			JSONArray blogs = result.getJSONArray("valoraciones"); /*Array of all the ratings since last update*/
			for(int i = 0;i<blogs.length();i++){
				if(blogs.getJSONObject(i).getInt("value")>=2){
					/*Consideramos que han gustado los que tienen una valoracion mayor o igual que 2*/
					String idUser = blogs.getJSONObject(i).getString("owner_name");
					System.out.println(idUser);
					String idBlog = blogs.getJSONObject(i).getString("entity_guid");
					System.out.println(idBlog);
					System.out.println(blogs.getJSONObject(i).getInt("value"));
					String[] toBeAdded = {idUser,idBlog};
					msgs.add(toBeAdded);
				}
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return msgs;
	}

	public static void getRecommendationsFromAPI(long idMessageAPI,int idUser){
	    ArrayList<Message> message = new ArrayList<Message>();
	    message.add(MysqlInterface.getMessageByItsIdAPI(idMessageAPI, idUser));
	    MysqlInterface.updateReadAndLiked( message,  idUser);
	}
}
