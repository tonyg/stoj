import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class CanvasEllipse extends AbstractCanvasNode {
    public CanvasEllipse(ICanvasItemController controller, Color color,
			 double x1, double y1, double x2, double y2)
    {
	super(controller, color, x1, y1, x2, y2);
    }

    protected void updateShape() {
	this.body = new Ellipse2D.Double(x1, y1, x2 - x1, y2 - y1);
    }
}