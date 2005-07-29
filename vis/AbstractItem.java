import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public abstract class AbstractItem {
    protected Color color;
    protected String toolTip;
    protected double x1, y1, x2, y2;

    public AbstractItem(Color color, double x1, double y1, double x2, double y2) {
	this.color = color;
	this.toolTip = "";
	this.setLocation(x1, y1, x2, y2);
    }

    public Color getColor() { return color; }
    public void setColor(Color c) { color = c; }

    public String getToolTip() { return toolTip; }
    public void setToolTip(String t) { toolTip = t; }

    public double getX1() { return x1; }
    public double getX2() { return x2; }
    public double getY1() { return y1; }
    public double getY2() { return y2; }

    public void setX1(double v) { x1 = v; updateShape(); }
    public void setX2(double v) { x2 = v; updateShape(); }
    public void setY1(double v) { y1 = v; updateShape(); }
    public void setY2(double v) { y2 = v; updateShape(); }

    public void setPoint1(double x, double y) { x1 = x; y1 = y; updateShape(); }
    public void setPoint2(double x, double y) { x2 = x; y2 = y; updateShape(); }

    public void setLocation(double x1, double y1, double x2, double y2) {
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
	updateShape();
    }

    protected abstract void updateShape();
}