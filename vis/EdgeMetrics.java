import java.awt.*;
import javax.swing.*;

public class EdgeMetrics {
    private Color color;
    private double length;
    private double weighting;

    public EdgeMetrics(Color color, double length, double weighting) {
	this.color = color;
	this.length = length;
	this.weighting = weighting;
    }

    public Color getColor() { return color; }
    public double getLength() { return length; }
    public double getWeighting() { return weighting; }
}
