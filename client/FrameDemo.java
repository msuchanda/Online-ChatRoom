import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings("serial")
public class FrameDemo extends JFrame implements Runnable{
	ChatPanel cp;
	DrawPanel dp;
	//ListDemo ld;
	ControlPanel conp;
	String GloBalClientID = "";
	boolean connected;
	
	Socket s;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	public FrameDemo(){}
	public FrameDemo(String s){
		super(s);
		cp = new ChatPanel(this);
		dp = new DrawPanel(this);
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(cp, BorderLayout.WEST);
		container.add(dp, BorderLayout.EAST);
        getContentPane().add(container, BorderLayout.CENTER);
		conp = new ControlPanel(this);
		getContentPane().add(conp, BorderLayout.SOUTH);
		//Display the window.		
		container.setBackground( new Color(107, 106, 104));
		Toolkit tk = Toolkit.getDefaultToolkit();  
		int xSize = ((int) tk.getScreenSize().getWidth());  
		int ySize = ((int) tk.getScreenSize().getHeight());  
		setSize(xSize,ySize-40);  
		conp.setBackground( new Color(107, 106, 104));
		setLocationRelativeTo(null);
        setVisible(true);
        Font font = cp.ta.getFont();
		cp.ta.setFont(new Font(font.getFontName(), font.getStyle(), 22));
		conp.b2.setEnabled(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	

	public void setConnected(boolean c){
		connected = c;
	}
	public boolean isConnected(){
		return connected;
	}
	public void connect(){
		setConnected(true);
		try{
			s = new Socket("afsaccess1.njit.edu", 3000);
			oos = new ObjectOutputStream(s.getOutputStream());
			/*
			Broadcast Joined MEssage
			*/
			String hostname = "";
			if(GloBalClientID != ""){
				hostname = GloBalClientID;
			}else{
			    try {
			    	InetAddress addr = null;
					addr = InetAddress.getLocalHost();
					hostname = addr.getHostName();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			    
			}
			OuterDataObject objj = new OuterDataObject(hostname, true, false);
			try {
				oos.writeObject(objj);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			/***********************/
			new Thread(this).start();
			JOptionPane.showMessageDialog(null, "Successfully Connected");
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "There was problem connecting to server !");
			System.out.println(e.getMessage());
		}	
	}
	public void close(){
		String hostname = "";
		if(GloBalClientID != ""){
			hostname = GloBalClientID;
		}else{
		    try {
		    	InetAddress addr = null;
				addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			    
		}
		OuterDataObject objj = new OuterDataObject(hostname, false, true);
		try {
			oos.writeObject(objj);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		setConnected(false);	//should send last message so others can update user list
								// and remove self from shared array list of handlers
		try{
			oos.close();
			JOptionPane.showMessageDialog(null, "Disconnected from server !");
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "Error disconnecting from server !");
			System.out.println(e.getMessage());
		}
	}
	public void load(){
		new Thread(this).start();
	}
	@SuppressWarnings("rawtypes")
	public void run(){
		try{
			ois = new ObjectInputStream(s.getInputStream());
			for(;;){
				Object o = receiveMessage();
				Font font = cp.ta.getFont();
				cp.ta.setFont(new Font(font.getFontName(), font.getStyle(), 22));
				if(o != null){
					if(o instanceof OuterDataObject){
						//first check if load or save is the responce by server
						String serverResponseTypeCheck = ((OuterDataObject) o).getRequestType();
						if(serverResponseTypeCheck.equals("load") || serverResponseTypeCheck == "load"){
							String msgHistory = ((OuterDataObject) o).getMessage();
							msgHistory = msgHistory.replace("~", "\n");
							cp.ta.setText(cp.ta.getText() + "\n-------Chat History ------\n" + msgHistory + "-------------------------------\n");
						}else{
							if(((OuterDataObject) o).JoinedStatus() && !((OuterDataObject) o).ServerUserListReply()){
								cp.appendMessage(((OuterDataObject) o).getClientID()+" Just Joined Chat, Say Hi!");
							}else if(((OuterDataObject) o).LeftStatus() && !((OuterDataObject) o).ServerUserListReply()){
								cp.appendMessage(((OuterDataObject) o).getClientID()+" Left Chat !");
							}else if(((OuterDataObject) o).ServerUserListReply()){
								ArrayList<String> tmp = ((OuterDataObject) o).userNames;
								for (String s : tmp){
									cp.appendMessage(s+" is Online");
								}
							}else{
								String msgToShow = ((OuterDataObject) o).getMessage();
								msgToShow = msgToShow.replace(":)", "☺");
								msgToShow = msgToShow.replace("(:", "☺");
								msgToShow = msgToShow.replace(":(", "☹");
								msgToShow = msgToShow.replace("):", "☹");
								msgToShow = msgToShow.replace("<3", "❤");
								if(msgToShow.contains("(Private Message)")){
									msgToShow = msgToShow.replace("(Private Message)","");
									cp.appendMessage("(Private Message) from " + ((OuterDataObject) o).getClientID()+" : "+msgToShow);
								}else{
									cp.appendMessage(((OuterDataObject) o).getClientID()+" says: "+msgToShow);
								}
								
							}	
						}						
					}else if(o instanceof LineMessage){
						LineMessage lm = (LineMessage)o;
						ArrayList<Line> linelist = (ArrayList)lm.getMessage();
						dp.linelist = linelist;
						dp.repaint();
					}else if(o instanceof UserMessage){
					
					}
				}else{
					break;
				}
			}
		}catch(FileNotFoundException e){
			System.out.println(e.getMessage());
		}catch(IOException e){
			System.out.println("IO Exception: " + e.getMessage());
		}finally{
			try{
				ois.close();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
	public void sendMessage(Object o){
		if(isConnected()){
			if(o instanceof LineMessage){ 
				System.out.println("LineMessage written to stream");
				try{
					oos.writeObject(o);
				}catch(IOException e){
					System.out.println(e.getMessage());
				}
			}else{
				OuterDataObject dobj = (OuterDataObject) o;
				try{
					oos.writeObject(dobj);
				}catch(IOException e){
					System.out.println(e.getMessage());
				}
			}
		}
	}
	public Object receiveMessage(){
	
		Object obj = null;
		try{
			obj = ois.readObject();
		}catch(IOException e){
			System.out.println("End of stream.");
		}catch(ClassNotFoundException e){
			System.out.println(e.getMessage());
		}
		return obj;
	}
	
	public void clearChat(){
		cp.ta.setText("");
	}
	
	public void clearDraw(){
		dp.linelist = new ArrayList<Line>();
		dp.repaint();
	}
	
	public void setClientID(String ClientName, FrameDemo container){
		if(ClientName != ""){
			GloBalClientID = ClientName;
			container.setTitle("Chat System: " + ClientName);
		}
	}
	
    private static void createAndShowGUI() {
        @SuppressWarnings("unused")
		FrameDemo frame = new FrameDemo("Chat System");   
    }

    void saveChat(){
    	String username;
    	String chatHistory;
    	
    	username = conp.F1.getText();
    	chatHistory = cp.ta.getText();
    	OuterDataObject obj = new OuterDataObject(username,chatHistory,"save");
    	sendMessage(obj);
    	JOptionPane.showMessageDialog(null, "Chat history saved on server !");
    }
    
    void loadChat(){
    	String username = conp.F1.getText();
    	OuterDataObject obj = new OuterDataObject(username,"","load");
    	sendMessage(obj);
    }
    
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}