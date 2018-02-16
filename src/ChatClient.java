import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;

public class ChatClient {
    BufferedReader in;
    PrintWriter out;
	ObjectInputStream inputStream = null;
	ObjectOutputStream outputStream = null;
	String userName = null;
    
	private static AES AESkey = new AES();
    JFrame frame = new JFrame("Chat Room");
    JTextField textField = new JTextField(80);
    JTextArea chatWindow = new JTextArea(20, 65);
    JTextArea userWindow = new JTextArea(20, 15);

    public ChatClient() {
    	textField.setEditable(false);
    	chatWindow.setEditable(false);
    	userWindow.setEditable(false);
    	//Set Fonts
    	textField.setFont(textField.getFont().deriveFont(16f));
    	chatWindow.setFont(chatWindow.getFont().deriveFont(16f));
    	userWindow.setFont(chatWindow.getFont().deriveFont(16f));
    	//Set Borders 
    	Border border = BorderFactory.createLineBorder(Color.BLACK);
    	chatWindow.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    	userWindow.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    	
    	DefaultCaret caret = (DefaultCaret)chatWindow.getCaret();
    	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
   
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(chatWindow), "Center");
        frame.getContentPane().add(new JScrollPane(userWindow), "East");
        frame.pack();

        textField.addActionListener(new ActionListener() {
        	//Send message when enter is pressed
            public void actionPerformed(ActionEvent e) {
            	String input = textField.getText();
            	try {
					String cipher = AESkey.encrypt(input);
					out.println(userName); // OUT TO SERVER
	                out.println(cipher); // OUT TO SERVER
	                textField.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
            }
        });
    }

    private void run() throws Exception {

    	final String serverAddress = "xxxxxxxxxxx";
        @SuppressWarnings("resource")
		Socket socket = new Socket(serverAddress, 9001);
       
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        
        //Send Server client's user name
        String message = "Choose a User Name:\n(Max 14 Characters)";
        String tempUserName = JOptionPane.showInputDialog(frame, message, "User Name Selection", JOptionPane.PLAIN_MESSAGE);
        if (tempUserName == null){ // Close app if no username is entered
        	System.exit(0);
        }

        while(true){
        	//Send initial username choice to server
        	out.println(tempUserName);
        	
        	//read servers response to username 
        	String serverCommand = in.readLine();
        	
        	//Break and add username when successful name is chosen 
        	if(serverCommand.startsWith("NAMEPASS")){
        		userName = tempUserName;
        		break;
        	}
        	//Append messages for failing username choice 
        	if(serverCommand.startsWith("NAMETAKEN")){
        		message = message + "\nNAME ALREADY TAKEN";
        	}
        	if(serverCommand.startsWith("NAMELONG")){
        		message = message + "\nNAME LONGER THAN 14 CHARACTERS";
        	}
        	if(serverCommand.startsWith("NAMEFAIL")){
        		message = message + "\nNAME INVALID";
        	}
        	//Try again with failed messages displayed
        	tempUserName = JOptionPane.showInputDialog(frame, message, "User Name Selection", JOptionPane.PLAIN_MESSAGE);
            if (tempUserName == null){ // Close app if no username is entered
            	System.exit(0);
            }
        }

		//Get Servers Public RSA Key
		inputStream = new ObjectInputStream(socket.getInputStream());
		RSA serverRSAPublicKey = (RSA)inputStream.readObject();
		
		//Send Server AES Key using Server's Public Key
		String AESkeyString = AESkey.getPrivateKey();
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		outputStream.writeObject(serverRSAPublicKey.encrypt(AESkeyString));
        
		chatWindow.append("Welcome " + userName + "\n");
		//Allow typing 
        textField.setEditable(true);
        
        //Read in from server
        while (true) {
            String user = in.readLine(); // IN FROM SERVER
            String cipher = in.readLine(); // IN FROM SERVER
            String plainText = AESkey.decrypt(cipher);
            //Message from client
            if(plainText.startsWith("MESSAGE")){
            	chatWindow.append(user + ": " + plainText.substring(7) + "\n");
            }
            //Updates user list
            else if(plainText.startsWith("CLEAR")){
            	userWindow.setText("");
            	userWindow.append("Connected Users: \n");
        		
            }else{
            	userWindow.append(plainText.substring(8) + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
		AESkey.keyGen(); //Generate Client's symmetrical AES key  
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}