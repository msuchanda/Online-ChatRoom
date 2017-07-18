import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;


public class DrawPanel extends JPanel implements MouseMotionListener, MouseListener{
	ArrayList<Line> linelist;
	int lastX, lastY;
	FrameDemo container;
	
	public DrawPanel(){}
	public DrawPanel(FrameDemo container){
		setBackground(Color.WHITE);
		linelist = new ArrayList<Line>();
		addMouseMotionListener(this);
		addMouseListener(this);
		this.container = container;
	}
	public void mouseMoved(MouseEvent me){
	}
	public void mouseDragged(MouseEvent me){
		int endX = me.getX();
		int endY = me.getY();
		Line line = new Line(lastX, lastY, endX, endY);
		linelist.add(line);
		lastX = endX;
		lastY = endY;
		repaint();
	}
	public void mouseEntered(MouseEvent me){
		final int x = me.getX();
		final int y = me.getY();
		final Rectangle cellBounds = container.dp.getBounds();
		if(cellBounds != null/*cellBounds.contains(x, y)*/){
			container.dp.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}else{
			container.dp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	public void mouseExited(MouseEvent me){}
	public void mousePressed(MouseEvent me){
		lastX = me.getX();
		lastY = me.getY();
	}
	public void mouseReleased(MouseEvent me){}
	public void mouseClicked(MouseEvent me){}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Iterator<Line> it = linelist.iterator();
		while(it.hasNext()){
			Line current = it.next();
			g.drawLine(current.getStartX(), current.getStartY(),
					current.getEndX(), current.getEndY());
		}
		//LineMessage lm = new LineMessage();
		//lm.setMessage(linelist);
		//container.sendMessage(lm);
		g.drawString("Draw Here", (container.getWidth()/5), 20);
	}
	public Dimension getPreferredSize(){
		Dimension screenSize = container.getSize();
		return new Dimension(((screenSize.width)/2 - 50),100);
	}
	public Dimension getMinimumSize(){
		return new Dimension(450,100);
	}
}