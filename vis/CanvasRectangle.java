import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class CanvasRectangle extends AbstractCanvasNode {
    public CanvasRectangle(ICanvasItemController controller, Color color,
			   double x1, double y1, double x2, double y2)
    {
	super(controller, color, x1, y1, x2, y2);
    }

    protected void updateShape() {
	this.body = new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
    }
}