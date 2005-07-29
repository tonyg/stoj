import java.awt.*;
import javax.swing.*;
import java.util.*;

public class BasicEllipse extends AbstractBasicNode {
    public BasicEllipse(BufferedCanvas canvas, String name) { super (canvas, name); }

    protected int getHalfwidth() { return 4; }
    protected boolean wantText() { return false; }

    protected AbstractCanvasNode buildFigure(double x1, double y1, double x2, double y2) {
	return new CanvasEllipse(this, getNodeColor(), x1, y1, x2, y2);
    }
}
