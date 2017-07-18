import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;

public class ControlPanel extends JPanel{
	JButton b1, b2, b3, b4, b5, b6,b7,b8,b9,b10, b11;
	JTextField F1;
	FrameDemo container;
	public ControlPanel(FrameDemo fd){
		container = fd;
		b1 = new JButton("Connect");
		b2 = new JButton("Disconnect");
		b4 = new JButton("Clear chat");
		b11 = new JButton("Clear drawing");
		b5 = new JButton("Send Drawing");
		b9 = new JButton("Save Drawing");
		b10 = new JButton("Load Drawing");
		b7 = new JButton("Save Chat");
		b8 = new JButton("Load Chat");
		b6 = new JButton("Set User Name");
		F1 = new JTextField("", 10);
		
		b1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.connect();
				b1.setEnabled(false);
				b2.setEnabled(true);
			}
		});
		
		b2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.close();
				F1.setEnabled(true);
				b6.setEnabled(true);
				b2.setEnabled(false);
				b1.setEnabled(true);
				container.GloBalClientID = "";
			}
		});
		
		b4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.clearChat();
			}
		});
		
		b11.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.clearDraw();
			}
		});
		
		b5.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				LineMessage lm = new LineMessage();
				lm.setMessage(container.dp.linelist);
				container.sendMessage(lm);
			}
		});
		
		b6.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.setClientID(F1.getText(), container);
				F1.setEnabled(false);
				b6.setEnabled(false);
			}
		});
		
		b7.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.saveChat();
			}
		});
		
		b8.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				container.loadChat();
			}
		});
		
		
		b9.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				LineMessage lm = new LineMessage();
				lm.setMessage(container.dp.linelist);
				lm.setSetType("saveDrawing");
				
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
		
				lm.setUsername(hostname);
				container.sendMessage(lm);
				JOptionPane.showMessageDialog(null, "Drawing is saved on server !");
			}
		});
		
		b10.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				LineMessage lm = new LineMessage();
				lm.setSetType("loadDrawing");
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
		
				lm.setUsername(hostname);
				container.sendMessage(lm);
			}
		});
		
		
		
		add(b1);
		add(b2);
		add(b4);
		add(b11);
		add(b5);
		add(b9);
		add(b10);
		add(b7);
		add(b8);
		add(b6);
		add(F1);
	}
}
