import java.io.Serializable;
import java.util.ArrayList;

public class OuterDataObject extends DataObject implements Serializable {
	String ClientId;
	boolean leftStatus = false;
	boolean joinedStatus = false;
	ArrayList<String> userNames;
	boolean ServerUserListReply = false;
	String username;
	String requestType = "";
	
	OuterDataObject(){}
	OuterDataObject(String id, String message){
		this.ClientId = id;
		this.setMessage(message);
	}
	OuterDataObject(String id, Boolean JoinedStatus, Boolean EndingMessage){
		this.ClientId = id;
		if(JoinedStatus && !EndingMessage){
			leftStatus = false;
			joinedStatus = true;
		}else if(!JoinedStatus && EndingMessage) {
			leftStatus = true;
			joinedStatus = false;
		}
	}
	OuterDataObject(ArrayList<String> a, boolean reply){
		this.userNames = a;
		this.ServerUserListReply = reply;
	}
	
	OuterDataObject(String username, String history, String requesttype){
		this.ClientId = username;
		this.setMessage(history);
		this.requestType = requesttype;
	}
	String getRequestType(){
		return requestType;
	}
	boolean ServerUserListReply(){
		return ServerUserListReply;
	}
	String getClientID(){
		return this.ClientId;
	}
	boolean LeftStatus(){
		return leftStatus;
	}
	boolean JoinedStatus(){
		return joinedStatus;
	}
}