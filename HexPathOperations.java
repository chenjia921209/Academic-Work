package edu.uwm.cs351;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;

/**
 * A utility class with operations on HexPaths
 * that can be performed efficiently
 * without access to the data structure.
 */
public class HexPathOperations {

	/**
	 * Concatenate the two paths, putting the result in the first path.
	 * The two paths must be connected: the first must end where the second starts
	 * @param p1 first path, will be added to
	 * @param p2 second path, will be unaffected
	 * @throws IllegalArgumentException if the two paths are not connected
	 */
	public static void add(HexPath p1, HexPath p2) {
		if (p1 == null || p2 == null) {
			throw new NullPointerException("HexPath arguments cannot be null");
		}
		if (!p1.getEnd().equals(p2.getStart())) {
			throw new IllegalArgumentException("Paths are not connected: p1 must end where p2 starts.");
		}
		for (HexDirection direction : p2.getDirections()) {//遍歷整個p2把它加到p1中
			p1.add(direction);
		}
	}

	/**
	 * Clean up a path by removing unnecessary passing over
	 * of locations.  In particular, if the path doubles back on itself,
	 * then the pair are removed.  And if a path takes a sharp corner,
	 * two directions can be replaced with one.
	 * @param p path to cleanup (will be modified), must not be null
	 */
	public static void cleanup(HexPath p) {
		HexPath newPath = new HexPath(p.getStart());
		
		for(int count = p.length(); count>0; count--) {
			newPath.add(p.removeLast());
		}
		while(newPath.length()>1) {
			HexDirection dir = newPath.removeLast();
			HexDirection lastDir = newPath.removeLast(); 
			
			HexDirection directionToAdd = null;
			boolean cleanPathFound = false;
			if (lastDir.clockwise(2).equals(dir)) {//-120
				newPath.add(lastDir.clockwise(1));
				cleanPathFound=true;
			}
			else if (!cleanPathFound && lastDir.clockwise(4).equals(dir)) {//+120
				newPath.add(lastDir.clockwise(5));
				cleanPathFound=true;
			}
			else if (!cleanPathFound && !lastDir.clockwise(3).equals(dir)) {//+-180
				newPath.add(lastDir);
				directionToAdd=dir;
			}
			if(directionToAdd!=null) {
				p.add(directionToAdd);
			}else if(!p.isEmpty()) {
				newPath.add(p.removeLast());
			}	
		}
		if(newPath.length()==1) {
			p.add(newPath.removeLast());
		}
		
	}


	/**
	 * Draw a path in a graphics context using the given width.
	 * @param path path to draw, must not be null
	 * @param g graphics context, must not be null
	 * @param width width of hexagons, must be positive
	 */
	public static void draw(HexPath path, Graphics g, int width) {
		HexCoordinate prev = null;
		int x1 = 0;
		int y1 = 0;
		for (HexCoordinate h : path) {
			Point p = h.toPoint(width);
			int x2 = p.x;
			int y2 = p.y;
			if (prev == null) {
				prev = h;
			} else {
				g.drawLine(x1, y1, x2, y2);
			}
			x1 = x2;
			y1 = y2;
		}
	}

	/**
	 * Create a random path.
	 * @param start starting coordinate, must not be null
	 * @param length how long to create a path
	 * @param r random generator, must not be null
	 * @return
	 */
	public static HexPath makeRandom(HexCoordinate start, int length, Random r) {
		HexDirection[] dirs = HexDirection.values();
		HexPath p = new HexPath(start);
		for (int i=0; i < length; ++i) {
			p.add(dirs[r.nextInt(dirs.length)]);
		}
		return p;
	}

	/**
	 * Return the reverse path and clear this path.
	 * @param p path to reverse (will be cleared)
	 * @return path that traces in the opposite direction.
	 */
	public static HexPath reverse(HexPath p) {

		HexPath newReversedPath = new HexPath(p.getEnd()); // 以原路徑的終點作為新的起點

		while (!p.isEmpty()) { // 逐步取出方向，反向加入新路徑
			newReversedPath.add(p.removeLast().reverse());
		}
		return newReversedPath;
	}
}
