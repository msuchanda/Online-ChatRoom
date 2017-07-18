import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.print.attribute.AttributeSet;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class ChatPanel extends JPanel{
	JTextField tf;
	JTextPane ta;
	JTextField tHidden;
	FrameDemo container;
	public ChatPanel(){
	}
	public ChatPanel(FrameDemo container){
		this.container = container;
		setLayout(new BorderLayout());
		tf = new JTextField();
		ta = new JTextPane();
		tHidden = new JTextField();
		tHidden.setText("dsdfsdf");
		tf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				String text = tf.getText();
				tf.setText("");
				String hostname = "";
				if(container.GloBalClientID != ""){
					hostname = container.GloBalClientID;
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
			    OuterDataObject dobj = new OuterDataObject(hostname,text);
				container.sendMessage(dobj);
			}
		});
		ta.setEditable(false);
		add(tf, BorderLayout.SOUTH);
		add(ta, BorderLayout.CENTER);	
		ta.setMargin(new Insets( 2, 2, 10, 2 ));
		tf.setMargin(new Insets( 2, 2, 10, 2 ));
	}
	public void appendMessage(String m){
		ta.setText(ta.getText()+ m + "\n");
	}
	

	
	
	public Dimension getPreferredSize(){
		Dimension screenSize = container.getSize();
		return new Dimension(((screenSize.width)/2),100);
	}
	public Dimension getMinimumSize(){
		return new Dimension(450,100);
	}
}