package edu.uwm.cs351;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * A hexagonal game tail with a particular coordinate and terrain.
 */
public class HexTile {

	public static final int WIDTH = 50; // this value could be changed
	
	private final Terrain terrain;
	private final HexCoordinate location;
	
	/**
	 * Create a hexagonal tile for the given terrain and location in hex coordinates.
	 * @param t terrain, must not be null
	 * @param loc location, must not be null
	 * @exception IllegalArgumentException if either argument is null
	 */
	public HexTile(Terrain t, HexCoordinate loc) {
		//we will use this kind of exception a lot
		if (t == null || loc == null) throw new IllegalArgumentException("neither terrain nor location may be null");
		terrain = t;
		location = loc;
	}
	
	@Override // implementation
	public String toString() {
		return terrain.toString() + location.toString();
	}
	
	@Override // implementation
	public boolean equals(Object x) {
		if (!(x instanceof HexTile)) return false;
		HexTile other = (HexTile)x;
		return location.equals(other.location) && terrain.equals(other.terrain);
	}
	
	@Override // implementation
	public int hashCode() {
		return location.hashCode() * Terrain.values().length + terrain.hashCode();
	}
	
	/**
	 * Return terrain of this tile.
	 * @return terrain of this tile
	 */
	public Terrain getTerrain() { return terrain; }
	
	/**
	 * Return location of this tile.
	 * @return location of this tile
	 */
	public HexCoordinate getLocation() { return location; }
	
	/**
	 * Render the tile in a graphics context.
	 * We fill the hexagon with the terrain suggested color and then
	 * outline the tile in black.
	 * @param g context to use.
	 */
	public void draw(Graphics g) {
		Polygon hexagon = location.toPolygon(WIDTH);
		g.setColor(terrain.getColor());
		g.fillPolygon(hexagon);
		g.setColor(Color.BLACK);
		g.drawPolygon(hexagon);
	}
}
