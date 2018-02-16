import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class ChatServer {

    private static final int PORT = 9001;
	private static RSA RSAKey = new RSA();
    private static HashMap<String, User> clients = new HashMap<String, User>();
    
    public static void main(String[] args) throws Exception {
    	log(null, "RSA key generating");
    	RSAKey.keyGen(); //Generate server RSA key
    	log(null, "The chat server is running");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread{
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
    	private ObjectInputStream inputStream = null;
    	private ObjectOutputStream outputStream = null;
    	private String userName = null;
        
        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //Get Client's User name
                
                while(true){
                	String tempUserName = in.readLine();
                	
                	if(clients.containsKey(tempUserName)){
                		out.println("NAMETAKEN");
                	}
                	else if(tempUserName.length() > 14){
                		out.println("NAMELONG");
                	}
                	else if(tempUserName.equals("") || tempUserName == null){
                		out.println("NAMEFAIL");
                	}
                	else{
                		out.println("NAMEPASS");
                		userName = tempUserName;
                		break;
                	}
                }
                log(userName, "Initiated a Connection");

    			//Add User to HashMap
                User user = new User(out, handShake());
                clients.put(userName, user);
    			log(userName, "Joined the Chat Room");
    			
    			//Update Client's user lists
    			broadcastMessage("CLEAR", "", "");
    			for (Map.Entry<String, User>client: clients.entrySet()) {
    				broadcastMessage("USERNAME", userName, client.getKey());
    			}
    			
                while (true) {
                	//Decrypt Messages coming in from users
                	String clientName = in.readLine(); //IN FROM CLIENTS
                    String input = in.readLine(); //IN FROM CLIENTS
                	String inputDecrypted = clients.get(clientName).getKey().decrypt(input);

                    //Send Messages to clients encrypted with AES keys from each user
                	broadcastMessage("MESSAGE", clientName, inputDecrypted);
                }
            } catch (IOException e) {
            	//Send disconnect message to clients
            	if(userName != null)
            	log(userName, "Left the Chat Room");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//Remove client from HashMap after client disconnects
                if (clients.get(userName) != null) {
                    clients.remove(userName);
                    
                	try {
                		//Update Client's user lists
                		broadcastMessage("CLEAR", "", "");
            			for (Map.Entry<String, User>client: clients.entrySet()) {
            				broadcastMessage("USERNAME", userName, client.getKey());
            			}
    				} catch (Exception e1) {
    					e1.printStackTrace();
    				}
                }
                try {
                    socket.close();
                } catch (IOException e) {
                	e.printStackTrace();
                }
            }
        }
        private AES handShake() throws IOException, ClassNotFoundException{
			//Send Client Server's RSA Public Key
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeObject(RSAKey.getPublicKey());
			log("Server", "Sent RSA Key");
			
			//Get Clients Encrypted AES Key
			inputStream = new ObjectInputStream(socket.getInputStream());
			String clientAESPrivateKeyStringEncrypted = (String) inputStream.readObject();
			log("Server", "Received Encrypted AES Key");
			
			//Decrypt Client AES Key
			String clientAESPrivateKeyStringDecrypted = RSAKey.decrypt(clientAESPrivateKeyStringEncrypted);
			AES clientAESPrivateKey = new AES();
			clientAESPrivateKey.decodeKey(clientAESPrivateKeyStringDecrypted);
			log("Server", "Decrypted AES Key");
			return clientAESPrivateKey;
        }
        //Send message to all connected clients 
        private void broadcastMessage(String action, String clientName, String message) throws Exception{
            for (Map.Entry<String, User>client: clients.entrySet()) {
            	User usr = client.getValue();
            	String cipher = usr.getKey().encrypt(action + message);
            	usr.getWriter().println(clientName); // SEND TO CLIENTS
                usr.getWriter().println(cipher); // SEND TO CLIENTS
            }
        }
    }
    //Prints log messages to console
    public static void log(String user, String message){
    	if (user == null)
    	System.out.printf("%-40s%s%n", message, new Date().toString());
    	else
    	System.out.printf("%-40s%s%n", user + ": " + message, new Date().toString());
    }
}