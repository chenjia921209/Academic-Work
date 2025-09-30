package edu.uwm.cs351;

import java.awt.Color;

/** A terrain class for a game board with hexagonal tiles.
 * The game determines the meaning of the various terrains.
 * We use light colors for rendering (except inaccessible) to help with contrast.
 */
public enum Terrain {
	INACCESSIBLE(Color.BLACK),
	// TODO: add other terrains
    WATER(Color.CYAN),
    LAND(Color.WHITE),
    FOREST(Color.GREEN),
    MOUNTAIN(Color.LIGHT_GRAY),
    CITY(Color.ORANGE),
    DESERT(Color.YELLOW);

	private final Color color;
	
	// TODO: define the (private) constructor.
	// There must not be a public constructor.  Why not?
	private Terrain(Color color) {
        this.color = color;
    }

	
	/**
	 * Return the suggested color to use for this terrain.
	 * Color is light to permit dark foregrounds to be used on figures on tiles.
	 * @return color associated with this terrain.
	 */
	public Color getColor() {
		return color;
	}
}
