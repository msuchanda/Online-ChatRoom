import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class ThreadedObjectServer{  
	
	public static void main(String[] args ){  
		ArrayList<ThreadedObjectHandler> handlers = new ArrayList<ThreadedObjectHandler>();
		ArrayList<String> liveUsers = new ArrayList<String>();
		try{  
			ServerSocket s = new ServerSocket(3000);
			for (;;){
				Socket incoming = s.accept( );
				new Thread(new ThreadedObjectHandler(incoming, handlers, liveUsers)).start();
			}   
		}
		catch (Exception e){  
			System.out.println(e);
		} 
	} 
}
class ThreadedObjectHandler implements Runnable { 
	OuterDataObject myObject = null;
	LineMessage myLineObject = null;
	private Socket incoming;
	ArrayList<ThreadedObjectHandler> handlers;
	ObjectInputStream in;
	ObjectOutputStream out;
	ArrayList<String> liveUsers;
	public ThreadedObjectHandler(Socket incoming, ArrayList<ThreadedObjectHandler> handlers, ArrayList<String> liveUsers){ 
		this.incoming = incoming;
		this.handlers = handlers;
		handlers.add(this);
		this.liveUsers = liveUsers;
	}
	
	public synchronized void broadcast(OuterDataObject obj){
		Iterator<ThreadedObjectHandler> it = handlers.iterator();
		while(it.hasNext()){
			ThreadedObjectHandler current = it.next();
			try{
				current.out.writeObject(obj);
				current.out.reset();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
	
	public synchronized void broadcastLineMessage(LineMessage obj){
		Iterator<ThreadedObjectHandler> it = handlers.iterator();
		while(it.hasNext()){
			ThreadedObjectHandler current = it.next();
			try{
				current.out.writeObject(obj);
				current.out.reset();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
   
	public void run(){  
		try{ 	
			in = new ObjectInputStream(incoming.getInputStream());

			out = new ObjectOutputStream(incoming.getOutputStream());
			boolean first = false;
			for(;;){
				Object tmpObj = in.readObject();
				if(tmpObj instanceof OuterDataObject){ //if incoming is of text message
					myObject = (OuterDataObject)tmpObj;
					String initCheckCond = myObject.getRequestType();
					if(initCheckCond == "save" || initCheckCond.equals("save")){
						// save the strings with username in database
						Class.forName("com.mysql.jdbc.Driver");
						Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://sql.njit.edu:3306/sm824","sm824","LoCQ4JufP");
						Statement stmt = conn.createStatement();
						String messageHistory = myObject.getMessage();
						messageHistory = messageHistory.replace("\n", "~");
						ResultSet rs = stmt.executeQuery("select * from `chat` where `username` = '"+myObject.getClientID()+"'");
						if(rs.next()){
							stmt.executeUpdate("update `chat` set `history` = '"+ messageHistory+"' where `username` = '"+myObject.getClientID()+"'");
						}else{
							stmt.executeUpdate("insert into `chat` values ('"+myObject.getClientID()+"','"+ messageHistory+"')");		
						}
					}else if(initCheckCond == "load" || initCheckCond.equals("load")){
						//retrieve from DB and return to the client
						Class.forName("com.mysql.jdbc.Driver");
						Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://sql.njit.edu:3306/sm824","sm824","LoCQ4JufP");
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select `history` from `chat` where `username` = '"+myObject.getClientID()+"'");
						String history = "";
						while(rs.next()){
							history = rs.getString("history");
						}
						OuterDataObject obj = new OuterDataObject("", history, "load");
						this.out.writeObject(obj);
					}else{
						String clientName = myObject.getClientID();
						for (String s : liveUsers){
							if(s == clientName){
								first = true;
							}
						}				
						if(!first){
							/*  Send whos online list to just joined user   */
							OuterDataObject SendList = new OuterDataObject(liveUsers, true);
							this.out.writeObject(SendList);
							/*  BROADCAST others whos joined */
							OuterDataObject justJoinedMessage = new OuterDataObject(clientName, true, false);
							Iterator<ThreadedObjectHandler> it = handlers.iterator();
							while(it.hasNext()){
								ThreadedObjectHandler current = it.next();
								if(current != this){
									try{
										current.out.writeObject(justJoinedMessage);
										current.out.reset();
									}catch(IOException e){
										System.out.println(e.getMessage());
									}
								}
							}
							//add user to live user list
							liveUsers.add(clientName);
						}else{
							boolean privateMessage = false;
							//check if current message is because someone left chat 
							if(myObject.LeftStatus()){
								String clientIDtoRemove = myObject.getClientID();
								int i=0;
								for (String s : liveUsers){
									if(s == clientIDtoRemove){
										liveUsers.remove(i);
										break;
									}
									i++;
								}
								/*  BROADCAST others whos joined */
								OuterDataObject justJoinedMessage = new OuterDataObject(clientName, false, true);
								Iterator<ThreadedObjectHandler> it = handlers.iterator();
								while(it.hasNext()){
									ThreadedObjectHandler current = it.next();
									if(current != this){
										try{
											current.out.writeObject(justJoinedMessage);
											current.out.reset();
											privateMessage = true;
										}catch(IOException e){
											System.out.println(e.getMessage());
										}
									}
								}
							}
							
							
							//Now check for Private message 
							String msgg = myObject.getMessage();
							int i=0;
							
							for (String s : liveUsers){
								if(msgg.contains("@"+s)){
									myObject.setMessage(msgg.replace("@"+s,"(Private Message)"));
									privateMessage = true;
									break;
								}
								i++;
							}
							int j=0;
							Iterator<ThreadedObjectHandler> it = handlers.iterator();
							while(it.hasNext()){
								ThreadedObjectHandler current = it.next();
								if(j == i){
									try{
										
										current.out.writeObject(myObject);
										current.out.reset();
										this.out.writeObject(myObject);
										this.out.reset();
									}catch(IOException e){
										System.out.println(e.getMessage());
									}
								}
								j++;
							}
							
							//if this is private message then do not broadcast ! 
							if(privateMessage == false){
								broadcast(myObject);
							}
						}				
					}
				}else if(tmpObj instanceof LineMessage){
					myLineObject = (LineMessage)tmpObj;
					String checkFlag = myLineObject.getSetType();
					if(checkFlag == "saveDrawing" || checkFlag.equals("saveDrawing")){
						ArrayList<Line> LineMessage = (ArrayList)myLineObject.getMessage();
						String makeString = "";
						for (Line l: LineMessage) {
							makeString = makeString+"{"+l.getStartX()+"/"+l.getStartY()+"/"+l.getEndX()+"/"+l.getEndY()+"},";
						}
						// save the strings with username in database
						Class.forName("com.mysql.jdbc.Driver");
						Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://sql.njit.edu:3306/sm824","sm824","LoCQ4JufP");
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select * from `lineMessage` where `username` = '"+myLineObject.getUsername()+"'");
						if(rs.next()){
							stmt.executeUpdate("update `lineMessage` set `line` = '"+ makeString+"' where `username` = '"+myLineObject.getUsername()+"'");
						}else{
							stmt.executeUpdate("insert into `lineMessage` values ('"+myLineObject.getUsername()+"','"+ makeString+"')");		
						}
					}else if(checkFlag == "loadDrawing" || checkFlag.equals("loadDrawing")){
						//retrieve from DB and return to the client
						Class.forName("com.mysql.jdbc.Driver");
						Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://sql.njit.edu:3306/sm824","sm824","LoCQ4JufP");
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery("select `line` from `lineMessage` where `username` = '"+myLineObject.getUsername()+"'");
						String line = "";
						while(rs.next()){
							line = rs.getString("line");
						}
						String[] initSplit = line.split(",");
						ArrayList<Line> arrLineList = new ArrayList<Line>();
						for (int i=0; i< initSplit.length; i++) {
							String tmpstring = initSplit[i];
							tmpstring = tmpstring.replace("{", "");
							tmpstring = tmpstring.replace("}", "");
							String[] tmpintArr = tmpstring.split("/");
							arrLineList.add(new Line(Integer.parseInt(tmpintArr[0]), Integer.parseInt(tmpintArr[1]), Integer.parseInt(tmpintArr[2]), Integer.parseInt(tmpintArr[3])));
						}
						LineMessage lm = new LineMessage();
						lm.setMessage(arrLineList);
						this.out.writeObject(lm);
					}else{
						broadcastLineMessage(myLineObject);
					}					
				}
			}
		}catch (Exception e){  
			System.out.println(e);
		}finally{
			handlers.remove(this);
			try{
				in.close();
				out.close();
				incoming.close();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
}
