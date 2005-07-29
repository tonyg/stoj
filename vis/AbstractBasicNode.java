import java.awt.*;
import javax.swing.*;
import java.util.*;

public abstract class AbstractBasicNode implements INode, ICanvasItemController {
    private ArrayList outboundEdges;
    private IEdge[] edgeCache;
    private boolean pinned;
    private boolean grabbed;
    protected AbstractCanvasNode figure;
    protected String name;
    private double x, y;
    private double dx, dy;

    public AbstractBasicNode(BufferedCanvas canvas, String name) {
	init(canvas, name, false,
	     Math.random() * canvas.getWidth(),
	     Math.random() * canvas.getHeight());
    }

    public AbstractBasicNode(BufferedCanvas canvas, String name, boolean isPinned,
			     double initx, double inity)
    {
	init(canvas, name, isPinned, initx, inity);
    }

    public void grab(boolean grabbed) {
	this.grabbed = grabbed;
    }

    public void dragTo(double x, double y) {
	this.x = x; this.y = y;
	this.dx = this.dy = 0;
	updateFigure(x, y);
    }

    private void init(BufferedCanvas canvas, String name, boolean isPinned,
		      double initx, double inity)
    {
	this.name = name;
	outboundEdges = new ArrayList();
	edgeCache = null;
	pinned = isPinned;
	grabbed = false;
	x = initx; y = inity;
	dx = dy = 0;
	setupFigure(canvas, name, x, y);
	canvas.addItem("node", figure);
    }

    public String toString() {
	return "AbstractBasicNode(" + this.name + ")";
    }

    protected int getHalfwidth() { return 24; }
    protected Color getTextColor() { return Color.white; }
    protected Color getNodeColor() { return Color.green; }
    protected boolean wantText() { return true; }

    protected abstract AbstractCanvasNode buildFigure(double x1, double y1, double x2, double y2);

    protected void setupFigure(BufferedCanvas canvas, String name, double x, double y) {
	figure = buildFigure(x - getHalfwidth(), y - getHalfwidth(),
			     x + getHalfwidth(), y + getHalfwidth());
	if (wantText()) {
	    figure.setText(name);
	    figure.setTextColor(Color.yellow);
	}
    }

    protected void updateFigure(double x, double y) {
	figure.setLocation(x - getHalfwidth(), y - getHalfwidth(),
			   x + getHalfwidth(), y + getHalfwidth());
    }

    public IEdge findEdge(INode other) {
	for (Iterator i = outboundEdges.iterator(); i.hasNext();) {
	    IEdge edge = (IEdge) i.next();
	    if (edge.getHead() == other)
		return edge;
	}
	return null;
    }

    public void addEdge(IEdge edge) {
	if (edge.getTail() != this) {
	    throw new IllegalArgumentException("AbstractBasicNode.addEdge: tail must be this node");
	}
	outboundEdges.add(edge);
	edgeCache = null;
    }

    public void removeEdge(IEdge edge) {
	int i = outboundEdges.indexOf(edge);
	if (i != -1) {
	    outboundEdges.remove(i);
	}
	edgeCache = null;
    }

    public IEdge[] getOutboundEdges() {
	if (edgeCache == null) {
	    edgeCache = new IEdge[outboundEdges.size()];
	    for (int i = 0; i < edgeCache.length; i++) {
		edgeCache[i] = (IEdge) outboundEdges.get(i);
	    }
	}
	return edgeCache;
    }

    public boolean getPinned() { return pinned || grabbed; }
    public void setPinned(boolean p) { pinned = p; }

    public double getMass() {
	return 1.0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public void setDx(double v) { dx = v; }
    public void setDy(double v) { dy = v; }

    public void translate() {
	x += dx; y += dy;
	updateFigure(x, y);
    }

    public void fixupEdges() {
	for (Iterator i = outboundEdges.iterator(); i.hasNext();) {
	    IEdge edge = (IEdge) i.next();
	    edge.fixup();
	}
    }
}
