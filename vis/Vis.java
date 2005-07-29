import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class Vis extends JApplet {
    private static class Command {
	public String command;
	public String category;
	public String subject;
	public String argument;
    };

    private static void splitLine(String line, Command c) {
	int old = 0;
	int i = -1;

	old = i+1; i = line.indexOf(' ', old); c.command = line.substring(old, i);
	old = i+1; i = line.indexOf(' ', old); c.category = line.substring(old, i);
	old = i+1; i = line.indexOf(' ', old); c.subject = line.substring(old, i);
	c.argument = line.substring(i+1);
    }

    private static void readInput(BufferedCanvas canvas, Relaxer relaxer,
				  Map edgeMetrics, String filename)
	throws FileNotFoundException, IOException,
	       ClassNotFoundException, NoSuchMethodException, InstantiationException,
	       IllegalAccessException, InvocationTargetException
    {
	LineNumberReader in;
	if (filename != null) {
	    in = new LineNumberReader(new FileReader(filename));
	} else {
	    in = new LineNumberReader(new InputStreamReader(System.in));
	}
	readInput(canvas, relaxer, edgeMetrics, in);
    }

    private static void readInput(BufferedCanvas canvas, Relaxer relaxer,
				  Map edgeMetrics, LineNumberReader in)
	throws IOException,
	       ClassNotFoundException, NoSuchMethodException, InstantiationException,
	       IllegalAccessException, InvocationTargetException
    {
	String line;
	Command c = new Command();

	HashMap nodes = new HashMap();

	while ((line = in.readLine()) != null) {
	    splitLine(line, c);
	    if ("node".equals(c.command)) {
		Class cl = Class.forName("Node_" + c.category);
		Constructor con = cl.getConstructor(new Class[] { BufferedCanvas.class,
								  String.class });
		INode n = (INode) con.newInstance(new Object[] { canvas,
								 c.argument });
		relaxer.addNode(n);
		nodes.put(c.subject, n);
	    } else if ("edge".equals(c.command)) {
		EdgeMetrics m = (EdgeMetrics) edgeMetrics.get(c.category);
		AbstractBasicNode tail = (AbstractBasicNode) nodes.get(c.subject);
		INode head = (INode) nodes.get(c.argument);
		IEdge e = new BasicEdge(canvas, tail, head, m);
		tail.addEdge(e);
	    }
	}
    }

    private static HashMap getEdgeMetrics() {
	final double DEFAULT_LENGTH = 100;
	HashMap metrics = new HashMap();
	metrics.put("ch", new EdgeMetrics(new Color(32, 64, 0), DEFAULT_LENGTH, 0));
	metrics.put("k", new EdgeMetrics(Color.blue, DEFAULT_LENGTH, 0.05));
	metrics.put("rec_k", new EdgeMetrics(Color.blue, DEFAULT_LENGTH/8, 0.25));
	metrics.put("new_k", new EdgeMetrics(new Color(0, 32, 64), DEFAULT_LENGTH, 0.0125));
	metrics.put("subject", new EdgeMetrics(Color.red, DEFAULT_LENGTH, 0.8));
	metrics.put("out_subject", new EdgeMetrics(Color.red, DEFAULT_LENGTH/2, 0.8));
	metrics.put("join_in", new EdgeMetrics(Color.red, DEFAULT_LENGTH/2, 0.8));
	metrics.put("object", new EdgeMetrics(Color.green, DEFAULT_LENGTH, 0.8));
	return metrics;
    }

    public static void main(String[] args) {
	JFrame f = new JFrame("StojVis");
	BufferedCanvas c = new BufferedCanvas(Color.black);
	c.addLayer("edge");
	c.addLayer("node");
	Relaxer r = new Relaxer(c);

	f.setSize(1000, 700);
	f.getContentPane().add(c);
	f.show();

	HashMap metrics = getEdgeMetrics();

	try {
	    if (args.length > 0) {
		readInput(c, r, metrics, args[0]);
	    } else {
		readInput(c, r, metrics, (String) null);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	new Thread(r).start();
    }

    private Relaxer relaxer = null;
    private Thread relaxerThread = null;

    public void init() {
	BufferedCanvas c = new BufferedCanvas(Color.black);
	c.addLayer("edge");
	c.addLayer("node");
	relaxer = new Relaxer(c);

	getContentPane().add(c);

	HashMap metrics = getEdgeMetrics();
	String graph = java.net.URLDecoder.decode(getParameter("graph"));
	try {
	    readInput(c, relaxer, metrics, new LineNumberReader(new StringReader(graph)));
	} catch (Exception e) { throw new RuntimeException(e); }
    }

    public void start() {
	if (relaxerThread == null) {
	    relaxerThread = new Thread(relaxer);
	}
	relaxerThread.start();
    }

    public void stop() {
	if (relaxerThread != null) {
	    relaxer.keepRunning = false;
	    relaxerThread = null;
	}
    }
}
