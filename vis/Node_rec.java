import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Node_rec extends BasicEllipse {
    public Node_rec(BufferedCanvas c, String n) { super(c, n); }
    protected Color getNodeColor() { return Color.red; }
}
