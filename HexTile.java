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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true; // unnecessary optimization
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		HexTile other = (HexTile) obj;
		return location.equals(other.location) && 
				terrain.equals(other.terrain);
	}

	@Override
	public int hashCode() {
		int result = terrain.hashCode() +
				Terrain.values().length * location.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return terrain.toString() + location.toString();
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

	/**
	 * Return HexTile whose string is the given string.
	 * @param s hex tile string of form TERRAIN<a,b,c>.  Must not be null
	 * @return hex tile whose string is this method's parameter.
	 * @throws FormatException if the string format is bad.
	 * @throws IllegalArgumentException if the terrain part is wrong
	 */
	public static HexTile fromString(String s) throws FormatException {
		   if (s == null) throw new IllegalArgumentException("Input string cannot be null");

		    //s = s.trim(); // 移除前後空白，避免輸入不乾淨的問題

		    // 找到 < 和 > 的位置
		    int startIndex = s.indexOf('<');
		    int endIndex = s.length() - 1;
		    
		   // int endIndex = s.indexOf('>');


		    // 確保格式正確
		    if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex - 1) {
		        throw new FormatException("Invalid format: Missing or misplaced < >");
		    }

		    // 擷取地形部分 (TERRAIN)
		    String terrainString = s.substring(0, startIndex);
		    
		    // 擷取座標部分 (<a,b,c>)，並去掉 <>
		    String coordString = s.substring(startIndex, endIndex + 1);

		    // 轉換 Terrain（確保是有效的枚舉）
		    Terrain terrain;
		    try {
		        terrain = Terrain.valueOf(terrainString); 
		    } catch (IllegalArgumentException e) {	
		        throw new IllegalArgumentException("Invalid terrain type: " + terrainString);
		    }

		    // 轉換 HexCoordinate
		    HexCoordinate location = HexCoordinate.fromString(coordString);

		    // 回傳新的 HexTile 物件
		    return new HexTile(terrain, location);
		}

}
