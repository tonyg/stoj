import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class BufferedCanvas extends JComponent {
    private Color bgColor;
    private ArrayList layerNames;
    private HashMap layers;
    private ICanvasItem activeItem;

    public BufferedCanvas(Color bgColor) {
	this.bgColor = bgColor;
	this.layerNames = new ArrayList();
	this.layers = new HashMap();
	setDoubleBuffered(true);
	activeItem = null;

	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    for (Iterator i = layerNames.iterator(); i.hasNext();) {
			String layerName = (String) i.next();
			ArrayList layer = (ArrayList) layers.get(layerName);
			for (int j = 0; j < layer.size(); j++) {
			    ICanvasItem item = (ICanvasItem) layer.get(j);
			    if (item.contains(e.getX(), e.getY())) {
				activeItem = item;
				ICanvasItemController controller = activeItem.getController();
				if (controller != null) {
				    controller.grab(true);
				}
				break;
			    }
			}
		    }
		}

		public void mouseReleased(MouseEvent e) {
		    if (activeItem != null) {
			ICanvasItemController controller = activeItem.getController();
			if (controller != null) {
			    controller.grab(false);
			}
			activeItem = null;
		    }
		}
	    });

	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {
		    if (activeItem != null) {
			ICanvasItemController controller = activeItem.getController();
			if (controller != null) {
			    controller.dragTo(e.getX(), e.getY());
			}
		    }
		}
	    });
    }

    public void addLayer(String layerName) {
	layerNames.add(layerName);
	layers.put(layerName, new ArrayList());
    }

    public void addItem(String layerName, ICanvasItem item) {
	ArrayList layer = (ArrayList) layers.get(layerName);
	layer.add(item);
    }

    public void removeItem(ICanvasItem item) {
	for (Iterator layers = this.layers.values().iterator(); layers.hasNext();) {
	    ArrayList layer = (ArrayList) layers.next();
	    int i = layer.indexOf(item);
	    if (i != -1) {
		layer.remove(i);
		return;
	    }
	}
    }

    public static int frameCount = 0;
    public static long elapsedMs;

    public void paint(Graphics _g) {
	Graphics2D g = (Graphics2D) _g;
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			   RenderingHints.VALUE_ANTIALIAS_ON);

	g.setColor(this.bgColor);
	g.fillRect(0, 0, this.getWidth(), this.getHeight());

	for (Iterator i = layerNames.iterator(); i.hasNext();) {
	    String layerName = (String) i.next();
	    ArrayList layer = (ArrayList) layers.get(layerName);
	    for (Iterator j = layer.iterator(); j.hasNext();) {
		ICanvasItem item = (ICanvasItem) j.next();
		item.paint(g);
	    }
	}

	g.setColor(Color.white);
	g.drawString(Integer.toString(++frameCount), 10, 20);
	if (elapsedMs > 0) {
	    g.drawString(Double.toString((double) frameCount / elapsedMs * 1000.0), 10, 32);
	}
    }
}
