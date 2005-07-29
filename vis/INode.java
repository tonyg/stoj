import java.awt.*;
import javax.swing.*;

public interface INode {
    public IEdge[] getOutboundEdges();
    public boolean getPinned();
    public double getMass();
    public double getX();
    public double getY();
    public double getDx();
    public double getDy();
    public void setDx(double v);
    public void setDy(double v);
    public void translate();
    public void fixupEdges();
}
