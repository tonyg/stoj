import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class CanvasArrow extends AbstractItem implements ICanvasItem {
    private static final double FLATNESS = 2.0;
    private static final double ARROWLENGTH = 8;
    private static final double ARROWWIDTH = 3;

    private Shape curve, head;

    public CanvasArrow(Color color, double x1, double y1, double x2, double y2) {
	super(color, x1, y1, x2, y2);
    }

    public ICanvasItemController getController() {
	return null;
    }

    protected void updateShape() {
	double dx = (x2 - x1) / 2.0;
	double dy = (y2 - y1) / 2.0;

	double cx = x1 + dx - dy/FLATNESS;
	double cy = y1 + dy + dx/FLATNESS;
	this.curve = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);

	//double cx = x1;
	//double cy = y1;
	//this.curve = new Line2D.Double(x1, y1, x2, y2);

	double basex = cx + dy/FLATNESS/2;
	double basey = cy - dx/FLATNESS/2;

	dx = (x2 - x1);
	dy = (y2 - y1);
	double hypot = Math.sqrt(dx*dx + dy*dy);
	if (Math.abs(hypot) > 0.1) {
	    dx /= hypot;
	    dy /= hypot;
	} else {
	    dx = 1;
	    dy = 0;
	}

	Polygon p = new Polygon();
	p.addPoint((int) (basex - dx*ARROWLENGTH + dy*ARROWWIDTH),
		   (int) (basey - dy*ARROWLENGTH - dx*ARROWWIDTH));
	p.addPoint((int) basex, (int) basey);
	p.addPoint((int) (basex - dx*ARROWLENGTH - dy*ARROWWIDTH),
		   (int) (basey - dy*ARROWLENGTH + dx*ARROWWIDTH));
	this.head = p;
    }

    public boolean contains(double x, double y) {
	return false;
    }

    public void paint(Graphics2D g) {
	g.setColor(this.color);
	g.draw(this.curve);
	g.fill(this.head);
    }
}