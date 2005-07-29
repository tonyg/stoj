import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Node_out extends BasicEllipse {
    public Node_out(BufferedCanvas c, String n) { super(c, n); }
    protected boolean wantText() { return true; }
    protected Color getNodeColor() { return Color.green; }
}
