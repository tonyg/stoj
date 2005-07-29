import java.awt.*;
import javax.swing.*;
import java.util.*;

public class BasicRectangle extends AbstractBasicNode {
    public BasicRectangle(BufferedCanvas canvas, String name) { super (canvas, name); }

    protected AbstractCanvasNode buildFigure(double x1, double y1, double x2, double y2) {
	return new CanvasRectangle(this, getNodeColor(), x1, y1, x2, y2);
    }
}
