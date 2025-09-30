package edu.uwm.cs351;

public enum HexDirection {
	NORTHEAST(0,-1,"b"), EAST(1,0,"A"), SOUTHEAST(1,1,"D"),
	SOUTHWEST(0,1,"B"), WEST(-1,0,"a"), NORTHWEST(-1,-1,"d");

	private final int da, db;
	private final String nickName;

	private HexDirection(int da, int db, String nn) {
		this.da = da;
		this.db = db;
		nickName = nn;
	}

	/** Return the direction in the opposite direction 
	 * from this one.
	 * @return opposite direction, never null
	 */
	public HexDirection reverse() {
		return values()[(ordinal()+3)%6];
	}

	/**
	 * Get the hex coordinate in the indicated direction
	 * @param h input coordinate, must not be null
	 * @return output coordinate in that direction
	 */
	public HexCoordinate move(HexCoordinate h) {
		return new HexCoordinate(h.a() + da, h.b() + db);
	}

	/**
	 * Return a nickname: a, b, d, A, B, D
	 * indicating whether a/b is decreased (lowercase) and/or
	 * increased (uppercase) or both d/D.
	 */
	public String getNickName() {
		return nickName;
	}

	/**
	 * Return a hex direction given its nickname.
	 * @param s nickname: one of a, b, d, A, B, D
	 * @return hex direction for that nick name
	 * @throws IllegalArgumentException if the nickname does not match the six allowed
	 */
	public static HexDirection fromNickName(String s) {
		switch (s) {
		case "A": return EAST;
		case "D": return SOUTHEAST;
		case "B": return SOUTHWEST;
		case "a": return WEST;
		case "d": return NORTHWEST;
		case "b": return NORTHEAST;
		default:
			throw new IllegalArgumentException("No direction for '" + s + "'");
		}
	}
}
