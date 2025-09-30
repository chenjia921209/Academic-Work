package edu.uwm.cs351;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.uwm.cs351.util.FIFOWorklist;
import edu.uwm.cs351.util.LIFOWorklist;
import edu.uwm.cs351.util.Worklist;

/**
 * Render a file of hex tiles on the screen, and then
 * perform a search on it. <br/>
 * Usage: &lt;filename&gt; [<code>FIFO</code>|<code>LIFO</code>] &lt;start&gt; &lt;end&gt; &lt;terrain&gt; ...<br/>
 * The "start" and "end" are three element hex coordinates, e.g., <code>&lt2,1,1&gt;</code>.
 * The terrains listed at the end are the ones that the path may traverse, e.g., <code>LAND DESERT</code>.
 */
public class HexPathFinder extends JFrame {
	/**
	 * Eclipse wants this
	 */
	private static final long serialVersionUID = 1L;

	public static void main(final String[] args) {
		final HexBoard board = new HexBoard();
		List<Terrain> passable = new ArrayList<>();
		if (args.length < 4) {
			System.out.println("Set Run>Run Configurations>Arguments>Program Arguments to have at least four arguments:");
			System.out.println("\ta filename (file of hextiles)");
			System.out.println("\tone of FIFO or LIFO");
			System.out.println("\ta starting hex coordinate");
			System.out.println("\tan ending hex coordinate");
			System.out.println("\tany terrain that is passable");
			System.exit(1);
		}
		Worklist<HexBoard.HexPiece> worklist;
		HexCoordinate start, end;
		try {
			readSeq(board, new BufferedReader(new FileReader(args[0])));
			if (args[1].equals("FIFO")) worklist = new FIFOWorklist<>();
			else if (args[1].equals("LIFO")) worklist = new LIFOWorklist<>();
			else {
				System.out.println("Unknown worklist type: " + args[1]);
				System.exit(1);
				return;
			}
			start = HexCoordinate.fromString(args[2]);
			end = HexCoordinate.fromString(args[3]);
			for (int i=4; i < args.length; ++i) {
				passable.add(Terrain.valueOf(args[i]));
			}
		} catch (IOException|FormatException|IllegalArgumentException e) {
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}
		final Search search = new Search(new TerrainSet(passable.toArray(new Terrain[0])), worklist);
		final HexPath path = search.find(start, end, board);
		if (path == null) {
			System.out.println("No path found.");
		}
		final String title = args[1] + " search from " + start + " to " + end;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				HexPathFinder x = new HexPathFinder(title,board,search,path);
				x.setSize(500, 300);
				x.setVisible(true);
				x.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			}
		});
	}
	
	private static void readSeq(HexBoard b, BufferedReader r) throws IOException {
		String input;
		while ((input = r.readLine()) != null) {
			try {
				HexTile tile = HexTile.fromString(input);
				b.add(new HexBoard.HexPiece(tile.getTerrain(), tile.getLocation()));
			} catch (FormatException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("serial")
	public HexPathFinder(final String title, final HexBoard b, final Search search, final HexPath path) {
		super(title);
		this.setContentPane(new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				for (HexBoard.HexPiece p : b) {
					p.asTile().draw(g);
				}
				if (path != null) {
					g.setColor(Color.MAGENTA);
					HexPathOperations.draw(path, g, HexTile.WIDTH);
				}
				else search.markVisited(g);
			}
		});
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
