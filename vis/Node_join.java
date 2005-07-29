import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Node_join extends BasicRectangle {
    public Node_join(BufferedCanvas c, String n) { super(c, n); }
    protected int getHalfwidth() { return 12; }
    protected Color getNodeColor() { return Color.blue; }
}
