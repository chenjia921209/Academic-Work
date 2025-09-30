package edu.uwm.cs351;

import java.awt.Point;
import java.awt.Polygon;

import javax.print.attribute.standard.MediaSize.Other;

/**
 * Coordinates on a hexagon-filled game board.
 * <dl>
 * <dt>a<dd> left to right (0 = left edge, moving left half a hex each line down)
 * <dt>b<dd> top to bottom (0 = top edge)
 * <dt>c<dd> left to right (0 = top edge, moving right half a hex each line down)
 * </dl>
 * The {@link #c()} coordinate is always the difference of the first two.
 */
public class HexCoordinate {
	private final int a, b, c;
	
	/**
	 * Create a hexagonal coordinate by specifying the first two coordinates
	 * and computing the third.
	 * @param a first coordinate
	 * @param b second coordinate
	 */
	public HexCoordinate(int a, int b) { //定義c怎麼來的
		// TODO: Very easy. See handout.
		this.a=a; //因為要指定這個a = a所以要用this
		this.b=b;
		this.c=a-b;
	}
	
	/**
	 * Create a hexagonal coordinate by specifying all three coordinates,
	 * which must be consistent.
	 * @param a
	 * @param b
	 * @param c
	 * @exception IllegalArgumentException if the coordinates are not consistent.
	 */
	public HexCoordinate(int a, int b, int c) throws IllegalArgumentException {//要檢查abc是否符合規則 a+b+c=0
		// TODO: Check consistency and then assign fields.
		if(a-b != c) {
			throw new IllegalArgumentException("Invalid coordinates: a + b + c must equal to 0.");
		}
		this.a = a;
	    this.b = b;
	    this.c = c;

	}
	
	/// three simple accessors
	
	/** Return the first coordinate (how far from left
	 * plus more every line).
	 * @return the first coordinate
	 */
	public int a() { return a; }
	
	/**
	 * Return the second coordinate (how far from top).
	 * @return the second coordinate
	 */
	public int b() { return b; }

	/**
	 * Return the third coordinate (how far from left
	 * minus more very line).
	 * @return the third coordinate
	 */
	public int c() { return c; }
	
	@Override
	public String toString() {
		return ("<"+a +"," +b + "," +c +">");
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof HexCoordinate)){ //如果他不屬於hexcoordinate
			return false;
		}
			HexCoordinate obj1 = (HexCoordinate) obj; //因為他不屬於coordinate所以需要創建一個新的obj1來儲存 (HexCoordiate)是用來表示他的型態
			return this.a == obj1.a &&  this.b == obj1.b &&  this.c == obj1.c ;
			
		}

	@Override
	public int hashCode() {
		int resulta = a;
		int resultb = 7* resulta +b;
		int resultc = 7* resultb +c;
		return resultc;
	}


	/// Overrides (make sure to declare @Override)
	// (No documentation comment is necessary,
	// but the reason for the override must be given.  Usually "implementation".)
	// no need to give a documentation comment if overridden documentation still is valid.
	// TODO: Override methods from Object that need new implementations
	
	
	/**
	 * Return the closest hex coordinate to this point.
	 * If two are equally close, either may be returned.
	 * @param p
	 * @param width width of grid (must NOT be negative or zero)
	 * @return closest hex coordinate
	 */
	public static HexCoordinate fromPoint(Point p, int width) {
		float height = width * HEIGHT_RATIO;
		float db = p.y/height;
		float da = (float)p.x/width + db/2.0f;
		float dc = da - db;
		
		int ac = (int)Math.floor((da+dc));
		int ab = (int)Math.floor((da+db));
		int bc = (int)Math.floor((db-dc));
		
		int a = (int)Math.ceil((ab+ac)/3.0);
		int b = (int)Math.ceil((ab+bc)/3.0);
		return new HexCoordinate(a,b);
	}


	/// Other accessors
	



	//define HEIGHT_RATIO is the ration of height to width
	public static final float HEIGHT_RATIO = (float) (Math.sqrt(3.0)/2.0); // height of a row, given width = 1.0
	private static final float THIRD = 1.0f/3.0f;
	private static final float TWOTHIRD = 2.0f/3.0f;
	
	/**
	 * Return center of hexagon as a point on the two-dimensional AWT plane.
	 * @param width width of hexagon
	 * @return Point in the center of the hexagon
	 */
	public Point toPoint(int width) {
		return toPoint(width, a, b);
	}
	
	/**
	 * A generalization of {@link #toPoint(int)} that takes two floats, to permit
	 * fractions into the coordinate space.
	 * @param width width of hexagon in grid
	 * @param a first coordinate
	 * @param b second coordinate
	 * @return [x,y] point for this location.
	 */
	private static Point toPoint(int width, float a, float b) {
		// TODO convert two two-dimensional coordinates with rounded values
		// Hint: Our code computes the height: the vertical skip between lines of hexagons
		// using the width and the height ratio.  
		// Then it uses the width and the height together with a and b to compute x and y coordinates.
		// Each unit of "a" moves the coordinate over by the width, and each unit of "b" down by the height.
		// But each unit of "b" also moves the coordinate *back* by half the width.
		double height = width * HEIGHT_RATIO;
		double x = width*(a-b/2);
		double y = height*b;
		
		int xCoordinate = (int) Math.round(x);
		int yCoordinate = (int) Math.round(y);
		//Point point = new Point(xCoordinate,yCoordinate);
		
		return new Point(xCoordinate,yCoordinate);
	}
	
	/**
	 * Create a polygon (for rendering in AWT) for the hexagon around this
	 * hex coordinate.  The hexagons so creates tile the plane.
	 * @param width width of hexagon in pixels
	 * @return polygon for hexagon
	 */
	public Polygon toPolygon(int width) {
		Polygon result = new Polygon();
		//THIRD = 1.0/3.0;
		//TWOTHIRD =2.0/3.0;
	
		Point p1 = new Point(toPoint(width,a-THIRD,b-TWOTHIRD));
		Point p2 = new Point(toPoint(width,a+THIRD,b-THIRD));
		Point p3 = new Point(toPoint(width,a+TWOTHIRD,b+THIRD));
		Point p4 = new Point(toPoint(width,a+THIRD,b+TWOTHIRD));
		Point p5 = new Point(toPoint(width,a-THIRD,b+THIRD));
		Point p6 = new Point(toPoint(width,a-TWOTHIRD,b-THIRD));
	
		result.addPoint(p1.x,p1.y);
		result.addPoint(p2.x,p2.y);
		result.addPoint(p3.x,p3.y);
		result.addPoint(p4.x,p4.y);
		result.addPoint(p5.x,p5.y);
		result.addPoint(p6.x,p6.y);
		
		// TODO: Add points for each of the six vertices of the hexagon.
		// Compute the a,b hex coordinates of each of the six points of the
		// hexagon around the center (which is at <a,b,c>).
		// Then for each one, use the private toPoint method to
		// create a point which can then be added to the polygon.
		// For example, the top vertex is at <a-1/3,b-2/3,c+1/3>
		// Use the THIRD and TWOTHIRD constants.
		return result;
		// You may want to look up Oracle's documentation on Polygon,
		// especially its addPoint method.
	}
	
	/**
	 * Return the number of steps to get from one hex to another.
	 * We can use the smallest distance traveling along just two of the coordinates.
	 * Thus we can add all the differences and remove the largest (not used).
	 * Alternately, we can return the <em>largest</em> of the differences directly,
	 * which (do the algebra!) is the same value.
	 * @param other
	 * @return number of steps from one hex to another
	 */
	 public int distance(HexCoordinate other) {
		int xDistance = Math.abs(a-other.a);
		int yDistance = Math.abs(b-other.b);
		int zDistance = Math.abs(c-other.c);
		int minPath = (int) Math.max(Math.max(xDistance, yDistance),zDistance);
		
		return minPath;
		// TODO: return the distance between this hexagon and the other.
		// See the handout for details.
		// You can use Math.max(x, y) and Math.abs(x)

		}
	}

