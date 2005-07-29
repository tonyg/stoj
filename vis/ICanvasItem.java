import java.awt.*;
import javax.swing.*;

public interface ICanvasItem {
    public ICanvasItemController getController();
    public boolean contains(double x, double y);
    public String getToolTip();
    public void paint(Graphics2D g);
}
