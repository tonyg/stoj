import java.awt.*;
import javax.swing.*;

public class BasicEdge implements IEdge {
    private INode tail, head;
    private EdgeMetrics metrics;
    private CanvasArrow figure;

    public BasicEdge(BufferedCanvas canvas, INode tail, INode head, EdgeMetrics m) {
	this.tail = tail;
	this.head = head;
	this.metrics = m;
	this.figure = new CanvasArrow(metrics.getColor(), 0, 0, 0, 0);
	canvas.addItem("edge", this.figure);
    }

    public INode getTail() { return tail; }
    public INode getHead() { return head; }
    public double getLength() { return metrics.getLength(); }
    public double getWeighting() { return metrics.getWeighting(); }

    public void fixup() {
	figure.setColor(metrics.getColor());
	figure.setLocation(tail.getX(), tail.getY(), head.getX(), head.getY());
    }
}
