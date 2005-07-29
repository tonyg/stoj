import java.awt.*;
import javax.swing.*;

public interface IEdge {
    public INode getTail();
    public INode getHead();
    public double getLength();
    public double getWeighting();
    public void fixup();
}
