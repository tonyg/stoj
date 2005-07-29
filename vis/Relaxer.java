import java.util.*;
import javax.swing.*;

public class Relaxer implements Runnable {
    private static final long INTERVAL_MS = 1000 / 10;
    private static final double FRICTION = 0.2;

    private ArrayList nodes;
    private JComponent container;

    public boolean keepRunning = true;

    public Relaxer(JComponent container) {
	this.nodes = new ArrayList();
	this.container = container;
    }

    private double getWidth() {
	return container.getWidth();
    }

    private double getHeight() {
	return container.getHeight();
    }

    public synchronized void addNode(INode node) {
	nodes.add(node);
    }

    public synchronized void removeNode(INode node) {
	int i = nodes.indexOf(node);
	if (i != -1) {
	    nodes.remove(i);
	}
    }

    public void run() {
	long startTime = new Date().getTime();
        BufferedCanvas.frameCount = 0;
        BufferedCanvas.elapsedMs = 0;
        keepRunning = true;
        while (keepRunning) {
	    relax();
	    container.repaint();
	    long now = new Date().getTime();
	    long delta = now - startTime;
	    BufferedCanvas.elapsedMs = delta;
	    long fraction = delta % INTERVAL_MS;
	    try {
		Thread.sleep(fraction);
	    } catch (InterruptedException ie) {}
	}
    }

    public synchronized void relax() {
	double friction = Math.exp(Math.log(FRICTION) * INTERVAL_MS/1000.0);
	for (Iterator i = nodes.iterator(); i.hasNext();) {
	    INode node = (INode) i.next();
	    node.setDx(node.getDx() * friction);
	    node.setDy(node.getDy() * friction);
	}

	double lx, ly;

	for (int i = 0; i < nodes.size(); i++) {
	    INode node = (INode) nodes.get(i);
	    IEdge[] outEdges = node.getOutboundEdges();
	    for (int j = 0; j < outEdges.length; j++) {
		IEdge edge = outEdges[j];
		INode other = edge.getHead();

		lx = other.getX() - node.getX();
		ly = other.getY() - node.getY();
		double l = Math.sqrt(lx*lx + ly*ly);
		double f = (l - edge.getLength()) * edge.getWeighting();
		if (Math.abs(f) >= 0.00001) {
		    if (Math.abs(l) >= 0.00001) {
			lx /= l;
			ly /= l;
		    } else {
			lx = Math.random() - 0.5;
			ly = Math.random() - 0.5;
		    }

		    double interval = INTERVAL_MS / 1000.0;
		    double flx = interval * f * lx;
		    double fly = interval * f * ly;
		    node.setDx(node.getDx() + flx / node.getMass());
		    node.setDy(node.getDy() + fly / node.getMass());
		    other.setDx(other.getDx() - flx / other.getMass());
		    other.setDy(other.getDy() - fly / other.getMass());
		}
	    }

	    for (int j = i+1; j < nodes.size(); j++) {
		INode other = (INode) nodes.get(j);
		lx = other.getX() - node.getX();
		ly = other.getY() - node.getY();
		double l2 = (lx*lx + ly*ly);
		if (l2 > 0 && l2 < 10000) {
		    double l = Math.sqrt(l2);
		    node.setDx(node.getDx() - lx / l);
		    node.setDy(node.getDy() - ly / l);
		    other.setDx(other.getDx() + lx / l);
		    other.setDy(other.getDy() + ly / l);
		}
	    }
	}

	for (Iterator i = nodes.iterator(); i.hasNext();) {
	    INode node = (INode) i.next();
	    if (!node.getPinned()) {
		double nx = node.getX() + node.getDx();
		double ny = node.getY() + node.getDy();
		if (nx < 0) { node.setDx(node.getDx() - nx); }
		if (ny < 0) { node.setDy(node.getDy() - ny); }
		if (nx > getWidth()) { node.setDx(node.getDx() - (nx - getWidth())); }
		if (ny > getHeight()) { node.setDy(node.getDy() - (ny - getHeight())); }
		node.translate();
	    }
	}

	for (Iterator i = nodes.iterator(); i.hasNext();) {
	    INode node = (INode) i.next();
	    node.fixupEdges();
	}
    }
}
