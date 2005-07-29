import java.awt.*;
import javax.swing.*;

public interface ICanvasItemController {
    public void grab(boolean grabbed);
    public void dragTo(double x, double y);
}
