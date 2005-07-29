import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Node_new_channel extends BasicRectangle {
    public Node_new_channel(BufferedCanvas c, String n) { super(c, n); }
    protected Color getNodeColor() { return new Color(128, 0, 0); }
}
