package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class HexBoard extends AbstractCollection<HexTile> {
	/** Comparator for hex coordinates, each row before the next */

	public static final Comparator<HexCoordinate> hexComparator =new Comparator<HexCoordinate>() { // TODO
		public int compare(HexCoordinate h1, HexCoordinate h2) {
			if (h1.b() != h2.b()) {
				return Integer.compare(h1.b(), h2.b()); // 先比較 b 坐標
			}
			return Integer.compare(h1.a(), h2.a()); // 如果 b 相同，則比較 a 坐標
		}		
	};

	/**
	 * A class of mutable hex tiles that have object identity,
	 * and are linked with other pieces. 
	 */
	public static class HexPiece {
		HexCoordinate location;
		Terrain terrain;		
		HexPiece[] neighbors = new HexPiece[HexDirection.values().length];

		/**
		 * Create a piece with the given aspects.
		 * @param t terrain, must not be null
		 * @param h location, must not be null
		 */
		public HexPiece(Terrain t, HexCoordinate h) {
			if (t == null || h == null) throw new NullPointerException("terrain or location cannot be null");
			this.terrain = t;
			this.location = h;
		}

		/**
		 * Return location of piece.
		 * @return location
		 */
		public HexCoordinate getLocation() {
			return location;
		}

		/**
		 * Return current terrain (can change)
		 * @return terrain
		 */
		public Terrain getTerrain() {
			return terrain;
		}

		/**
		 * Return an immutable tile for this piece.
		 * @return an immutable hex tile
		 */
		public HexTile asTile() {
			return new HexTile(terrain, location);
		}

		/**
		 * Get the tile in the given direction on teh board, if any
		 * @param d direction to look, must not be null
		 * @return piece in that direction, possibly null
		 */
		public HexPiece move(HexDirection d) {
			return neighbors[d.ordinal()];
		}

		// TODO: optional: write a toString method -- for debugging only
	}


	/// The data structure: two fields.  DO not add any more

	private List<HexPiece> pieces = new ArrayList<>();
	private int version;


	/** Return the location within the pieces list
	 * where the element ir, or would be inserted (to add it)
	 * @param t coordinate to look for
	 * @return location where piece with this coordinate is or would go
	 */
	private int locate(HexCoordinate t) {

		int left = 0, right = pieces.size() - 1;

		while (left <= right) {
			int mid = left + (right - left) / 2;
			int cmp = hexComparator.compare(t,pieces.get(mid).location);

			if (cmp == 0) {
				return mid; // 找到了座標，回傳索引
			} else if (cmp < 0) {
				right = mid - 1; // `t` 在右半邊
			} else {
				left = mid + 1; // `t` 在左半邊
			}
		}

		return left; // 回傳適合插入的位置

	}

	// TODO: We have a helper method that uses "locate" to find a HexPiece with the 
	// given coordinate if it exists.  Otherwise it return snull

	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private boolean wellFormed() {
		/* Invariant:
		 * 1. None of the pieces have null coordinates or terrains
		 * 2. All the pieces are in order, without duplicate locations
		 * 3. For every piece: 
		 *    a neighbor in a certain direction is in the list iff it is in the neighbor array for that direction.
		 */
		// TODO
		//1.& 3.
		for(HexPiece piece: pieces) {
			if(piece.location==null||piece.terrain==null) {
				return report("Coordinate can not be null");
			}
			for(HexDirection direction: HexDirection.values()) { //through 
				HexCoordinate expected= direction.move(piece.location);
				HexPiece current = get(expected);
				HexPiece neighbor = piece.neighbors[direction.ordinal()];

				if(current!=neighbor) {
					return report("the tile is not the same as neighbor");
				}
				if(neighbor != null && piece != neighbor.neighbors[direction.reverse().ordinal()]) {
					return report("the neighbor is not point back to the current tail");
				}
			}
			//2.
			for(int i=0; i<pieces.size()-1;i++) {
				if(hexComparator.compare(pieces.get(i).location, pieces.get(i+1).location)>=0) {
					return report("pieces are not in order");
				}
			}
		}
		return true;
	}

	private HexBoard(boolean ignored) { } // do not change this constructor

	public HexBoard() {
		// TODO 
		pieces = new ArrayList<>();
		version =0;
		assert wellFormed() : "invariant not estabished in constructor";
	}

	// TODO: We have helper methods to connect and disconnect pieces from the board
	private void disconnect(HexPiece piece) {
		for(HexDirection direction : HexDirection.values()) {
			if(piece.neighbors[direction.ordinal()] != null) {
				piece.neighbors[direction.ordinal()].neighbors[direction.reverse().ordinal()]= null;
			}
			piece.neighbors[direction.ordinal()]=null;
		}
	}

	private void connect(HexPiece piece) {
		for(HexDirection direction : HexDirection.values()) {
			HexPiece neighbor = get(direction.move(piece.location));
			if(neighbor!=null) {
				piece.neighbors[direction.ordinal()]=neighbor;
				neighbor.neighbors[direction.reverse().ordinal()] = piece;
			}
		}
	}

	// TODO: overrides (required/implementation/efficiency)

	@Override
	public boolean add(HexTile e) {
		// TODO Auto-generated method stub
		assert wellFormed() : "Invarient broken at the start of add";
		if(e==null) {
			throw new NullPointerException("e is null");
		}
		HexCoordinate location = e.getLocation();
		int index = locate(location);
		boolean added = false;
		if(index< pieces.size() && hexComparator.compare(pieces.get(index).location, location)==0) {
			HexPiece piece = pieces.get(index); 
			if(piece.terrain!= e.getTerrain()) {
				piece.terrain= e.getTerrain();
			}
		}else {
			HexPiece newHexPiece = new HexPiece(e.getTerrain(),location);
			pieces.add(index, newHexPiece);
			connect(newHexPiece);
			version++;
			added=true;
		}
		assert wellFormed():"Invarient broken at the end of add";
		return added;
	}

	@Override
	public boolean contains(Object o) {
		assert wellFormed():"Invariant broken at the start of contains";
		if(!(o instanceof HexTile)) {
			return false;
		}
		HexTile tile = (HexTile) o;
		HexPiece piece = get(tile.getLocation());
		return piece!=null && piece.asTile().equals(tile);
	}

	@Override
	public boolean remove(Object o) {
		assert wellFormed():"Invariant broken at the start of remove";
		if(!(o instanceof HexTile)) {
			return false;
		}
		HexTile tile = (HexTile) o;
		int index = locate(tile.getLocation());
		if (index<pieces.size()) {
			HexPiece piece = pieces.get(index);
			if(hexComparator.compare(piece.location, tile.getLocation())==0 && piece.asTile().equals(tile)) {
				disconnect(pieces.get(index));
				pieces.remove(index);
				version++;
				assert wellFormed():"Incariant broken at the end of remove";
				return true;

			}
		}

		return false;
	}

	@Override
	public void clear() {
		assert wellFormed():"Invariant broken at the start of clear";
		if(pieces.isEmpty()) return;
		version++;
		for(HexPiece piece : pieces) {
			disconnect(piece);
		}
		pieces.clear();
		assert wellFormed():"Invariant broken at the end of clear";
	}

	@Override
	public Iterator<HexTile> iterator() {
		return new MyIterator();
	}

	@Override
	public int size() {
		return pieces.size(); 
	}

	/**
	 * Return the piece at this hex coordinate (if it exists)
	 * @param t hex coordinate to look for
	 * @return hex piece at this location, or null if no such piece
	 */
	public HexPiece get(HexCoordinate h) {
		if(h!=null) {
			int index=locate(h);
			if(index<pieces.size() && hexComparator.compare(pieces.get(index).location, h)==0) {
				return pieces.get(index);
			}
		}
		return null; // TODO
	}

	//TODO: Declare a nested MyIterator class
	// 1. Wrap (have a field for) an iterator on the pieces list
	// 2. Use the wrapped iterator to do the regular iterator work, 
	//    but do the fail-fast work here.  (Don't rely on the wrapped iterator.)
	// 3. Keep track of the current element so that it can be disconnected if 
	//    a remove was successful
	// 4. check the outer invariant, but no need to declare own invariant

	private class MyIterator implements Iterator<HexTile>{
		int colVersion;
		int index;
		int current=-1;

		public MyIterator(){
			colVersion = version;
			index=0;
			assert HexBoard.this.wellFormed();
		}
		@Override
		public boolean hasNext() {
			assert HexBoard.this.wellFormed();
			if (colVersion != version) throw new ConcurrentModificationException("versions don't match");
			return index<pieces.size();
		}
		@Override
		public HexTile next() {
			assert HexBoard.this.wellFormed();
			if(!hasNext()) {
				throw new NoSuchElementException("no next element");
			}
			current = index;
			HexPiece piece = pieces.get(index++);
			assert HexBoard.this.wellFormed();
			return piece.asTile();
		}

		@Override
		public void remove() {
			assert HexBoard.this.wellFormed();
			if(colVersion!=version) {
				throw new ConcurrentModificationException("version don't match");
			}
			if (current == -1) {
				throw new IllegalStateException("need to call next first");
			}

			HexPiece pieceToRemove = pieces.get(current);
			disconnect(pieceToRemove);
			pieces.remove(current);

			if(current< index) {
				index--;
			}

			colVersion= ++version;
			current=-1;
			assert HexBoard.this.wellFormed();	
		}
	}

	/**
	 * Used for testing the invariant.  Do not change this code.
	 */
	public static class Spy {
		public static class MyHexPiece extends HexPiece {
			/**
			 * Create a debugging hex piece with the given parts
			 * @param t terrain, may be null
			 * @param h location, may be null
			 */
			public MyHexPiece(Terrain t, HexCoordinate h) {
				super(Terrain.INACCESSIBLE, new HexCoordinate(0,0));
				terrain = t;
				location = h;
			}

			/**
			 * Set the neighbor element
			 * @param d direction, must not be null
			 * @param p piece to use, may be null
			 */
			public void setNeighbor(HexDirection d, HexPiece p) {
				this.neighbors[d.ordinal()] = p;
			}
		}

		/**
		 * Return the sink for invariant error messages
		 * @return current reporter
		 */
		public Consumer<String> getReporter() {
			return reporter;
		}

		/**
		 * Change the sink for invariant error messages.
		 * @param r where to send invariant error messages.
		 */
		public void setReporter(Consumer<String> r) {
			reporter = r;
		}

		/**
		 * Create a debugging instance of the main class
		 * with a particular data structure.
		 * @param a list of hex pieces
		 * @return a new instance with the given data structure
		 */
		public HexBoard newInstance(List<HexPiece> a) {
			HexBoard result = new HexBoard(false);
			result.pieces = a;
			return result;
		}

		/**
		 * Test the locate routine
		 * @param b board to use, must not be null
		 * @param h coordinate to look for
		 * @return integer location
		 */
		public int locate(HexBoard b, HexCoordinate h) {
			return b.locate(h);
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param bs instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(HexBoard bs) {
			return bs.wellFormed();
		}
	}

}
