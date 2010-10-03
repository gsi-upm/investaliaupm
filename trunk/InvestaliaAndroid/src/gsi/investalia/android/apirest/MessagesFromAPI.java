package gsi.investalia.android.apirest;



import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;

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
				String text = blogs.getJSONObject(i).getString("description");
				List<Tag> tags = new ArrayList<Tag>();
				try{
					JSONArray tags_j = blogs.getJSONObject(i).getJSONArray("tags");
					for(int j=0;j<tags_j.length();j++){
						tags.add(new Tag(0,tags_j.getString(j)));
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
				int idUserUpdating = 0; 
				Message toBeAdded = new Message(id, userName, title, text,tags,date, read, liked,rating,timesRead,idUserUpdating);
				msgs.add(toBeAdded);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		return msgs;
	}



}
