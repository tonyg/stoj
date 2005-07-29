import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public abstract class AbstractCanvasNode extends AbstractItem implements ICanvasItem {
    private Color textColor;
    private String text;
    protected Shape body;
    private ICanvasItemController controller;

    public AbstractCanvasNode(ICanvasItemController controller, Color color,
			      double x1, double y1, double x2, double y2)
    {
	super(color, x1, y1, x2, y2);
	this.textColor = Color.white;
	this.text = "";
	this.controller = controller;
    }

    public ICanvasItemController getController() {
	return controller;
    }

    public Color getTextColor() { return textColor; }
    public void setTextColor(Color c) { textColor = c; }

    public String getText() { return text; }
    public void setText(String t) { text = t; }

    protected abstract void updateShape();

    public boolean contains(double x, double y) {
	return this.body.contains(x, y);
    }

    public void paint(Graphics2D g) {
	g.setColor(this.color);
	g.fill(this.body);

	FontMetrics fm = g.getFontMetrics();
	Rectangle2D b = fm.getStringBounds(text, g);
	g.setColor(this.textColor);
	g.drawString(text,
		     (int) (x1 + (x2 - x1)/2 - b.getWidth()/2),
		     (int) (y1 + (y2 - y1)/2 + b.getHeight()/2));
    }
}