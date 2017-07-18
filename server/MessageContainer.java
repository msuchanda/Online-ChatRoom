import java.io.*;
import java.util.*;

abstract public class MessageContainer{

	abstract public void setMessage(Object message);
	
	abstract public Object getMessage();

}

class StringMessage extends MessageContainer implements Serializable{

	String message;

	public void setMessage(Object message){
		this.message = (String)message;
	}
	
	public Object getMessage(){
			return message;	
	}

}
class UserMessage extends MessageContainer implements Serializable{

	String message;

	public void setMessage(Object message){
		this.message = (String)message;
	}
	
	public Object getMessage(){
			return message;	
	}

}
