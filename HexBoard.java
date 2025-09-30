package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * An implementation of the HexBoard ADT using 
 * a binary search tree implementation.
 * A hex board is a collection of hex tiles except that there can 
 * never be two tiles at the same location. 
 */
public class HexBoard extends AbstractCollection<HexTile> {

	private Node root;
	private int size;
	private int colVersion = 0;


	private static int compare(HexCoordinate h1, HexCoordinate h2) {
		if (h1.b() != h2.b()) {
			return Integer.compare(h1.b(), h2.b());
		}
		return Integer.compare(h1.a(), h2.a()); // TODO: return comparison value: row first and then left->right in row
	}

	private static class Node {
		HexCoordinate loc;
		Terrain terrain;
		Node left, right;
		Node(HexCoordinate l, Terrain t) { loc = l; terrain = t; }
	}

	// TODO: declare fields (see homework assignment)

	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	private static boolean report(String s) {
		reporter.accept("Invariant error: " + s);
		return false;
	}

	/**
	 * Return true if the nodes in this BST are properly
	 * ordered with respect to the {@link #compare(HexCoordinate, HexCoordinate)}
	 * method.  If a problem is found, it should be reported (once).
	 * @param r subtree to check (may be null)
	 * @param lo lower bound (if any)
	 * @param hi upper bound (if any)
	 * @return whether there are any problems in the tree.
	 */
	private static boolean isInProperOrder(Node r, HexCoordinate lo, HexCoordinate hi) {
		if (r == null) {
			return true;
		}
		if (lo != null && compare(r.loc, lo) <= 0) {
				return report("Node at " + r.loc + " is less than or equal to lower bound " + lo);
		}else if (hi != null && compare(r.loc, hi) >= 0) {
				return report("Node at " + r.loc + " is greater than or equal to upper bound " + hi);
		}

		return isInProperOrder(r.left, lo, r.loc) && isInProperOrder(r.right, r.loc, hi);
	}

	/**
	 * Return the count of the nodes in this subtree.
	 * @param p subtree to count nodes for (may be null)
	 * @return number of nodes in the subtree.
	 */
	private static int countNodes(Node p) {
		if (p == null) return 0;
		return 1 + countNodes(p.left) + countNodes(p.right);
	}

	private boolean wellFormed() {
		// Use helper methods to check the tree.
	    if (!isInProperOrder(root, null, null)) {
	        return false; 
	    }
	    int actualSize = countNodes(root);
	    if (size != actualSize) {
	        return report("Size mismatch: expected " + size + ", found " + actualSize);
	    }
	    return true;
	}

	/**
	 * Create an empty hex board.
	 */
	public HexBoard() {
		// TODO: initialize fields (if necessary)
		assert wellFormed() : "in constructor";
	}
	@Override
	public void clear() {
		assert wellFormed() : "before clear";
		if(size==0) return;
		root = null;
		size = 0;
		colVersion++;
		assert wellFormed() : "after clear";
	}
	

	@Override
	public boolean contains(Object o) {
		assert wellFormed(): "in contains";
		if(!(o instanceof HexTile )) return false;
		
		HexTile tile = (HexTile) o;
		Terrain t = terrainAt(tile.getLocation());
		if(t!=null && t.equals(tile.getTerrain())) {
			return true;
		}else {
			return false;
		}
	}

	/** Return the terrain at the given coordinate or null
	 * if nothing at this coordinate.
	 * @param c hex coordinate to look for (null OK but pointless)
	 * @return terrain at that coordinate, or null if nothing
	 */
	public Terrain terrainAt(HexCoordinate l) {
		assert wellFormed() : "in terrainAt";
		Node current = root;

		while (current != null) {
			int cmp = compare(l, current.loc);
			if (cmp == 0) {
				return current.terrain;
			}
			if (cmp < 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}
		return null; // TODO
	}

	@Override // required by Java
	public Iterator<HexTile> iterator() {
		assert wellFormed() : "in iterator";
		return new MyIterator();
	}

	@Override // required by Java
	public int size() {
		assert wellFormed() : "in size";
		return size; // TODO
	}

	// new methods (used by the iterator)
	public boolean add(HexTile tile) {
		assert wellFormed() : "in add";
		
		Node newNode = new Node(tile.getLocation(), tile.getTerrain());
		if (root == null) {
			root = newNode;
			size++;
			colVersion++; // 增加修改計數
			assert wellFormed() : "after add";
			return true;
		}
		Node current = root;
		Node parent = null;
		while (current != null) {
			int cmp = compare(tile.getLocation(), current.loc);
			parent = current;
			if (cmp < 0) {
				current = current.left;
			} else if (cmp>0){
				current = current.right;
			} else {
					if(tile.getTerrain()==current.terrain) {
						return false;
					}else {
						current.terrain = tile.getTerrain();
						colVersion++;
						return true;
					}
				}
			}
		if (compare(tile.getLocation(), parent.loc) < 0) {
			parent.left = newNode;
		} else {
			parent.right = newNode;
		}
		size++;
		colVersion++; // 增加修改計數
		assert wellFormed() : "after add";
		return true;
	}
	/**
	 * Return the "b" coordinate of the first tile in the
	 * hex board, or 0 if the board is empty.
	 * @return row of first tile or zero if none
	 */
	private int getFirstRow() {
		assert wellFormed() : "in getFirstRow()";
		if (root == null) return 0;
		int minRow = root.loc.b();
		Node current = root;
		while (current != null) {
			if (current.loc.b() < minRow) {
				minRow = current.loc.b();
			}
			current = current.left;
		}
		return minRow;
		// TODO
	}

	/**
	 * Return the "b" coordinate of the last tile in the
	 * hex board, or 0 if the board is empty.
	 * @return row of last tile, or zero if none
	 */
	private int getLastRow() {
		assert wellFormed() : "in getLastRow()";
		Node current = root;
		if (current == null) return 0;
		while (current.right != null) {
			current = current.right;
		}
		return current.loc.b();
		// TODO
	}

	/**
	 * Return the first (leftmost) hex tile in the given row, if any
	 * @param b row number (second [part of hex coordinate)
	 * @return hex tile with lowest a with this b location, 
	 * or null if no such hex tile.
	 */
	private HexTile getFirstInRow(int b) {
		assert wellFormed() : "in getFirstInRow()"; 
		Node current = root;
		Node InRow = null;
		if(current==null) return null;
		while(current != null) {
			if(current.loc.b()==b) {
				InRow= current;
				current=current.left;
			}else if(current.loc.b()<b) {
				current=current.right;
			}else {
				current=current.left;
			}
		}
		if(InRow==null) return null;
		return new HexTile(InRow.terrain,InRow.loc); 
		// TODO
	}

	/**
	 * Return the first (rightmost) hex tile in the given row, if any
	 * @param b row number (second [part of hex coordinate)
	 * @return hex tile with highest a with this b location, 
	 * or null if no such hex tile.
	 */
	private HexTile getLastInRow(int b) {
		assert wellFormed() : "in getLastInRow()";
		Node current = root;
		Node LastInRow = null;
		if(current==null) return null;
		while(current != null) {
			if(current.loc.b()==b) {
				LastInRow= current;
				current=current.right;
			}else if(current.loc.b()>b) {
				current=current.left;
			}else {
				current=current.right;
			}
		}
		if(LastInRow==null) return null;
		return new HexTile(LastInRow.terrain,LastInRow.loc); 
		// TODO
	}

	// TODO: What else?

	private class MyIterator implements Iterator<HexTile> {
		// TODO: fields, constructor, any helper method(s) (see homework description)
		private int currentRow;
		private HexTile nextTile;
		private HexTile lastTile;
		private int version =colVersion;

		public MyIterator() {
			currentRow = getFirstRow();
			nextTile= getFirstInRow(currentRow);
			lastTile = getLastInRow(currentRow);
		}

		@Override // required by Java
		public boolean hasNext() {
			if(version != colVersion) throw new ConcurrentModificationException("version is not the same as colVersion");
			return nextTile != null ; // TODO
		}

		@Override // required by Java
		public HexTile next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			HexTile currentTile = nextTile;

			// 判斷是否要移動到下一行
			if (currentTile.getLocation().equals(lastTile.getLocation())) {
				do {
					currentRow++;
					nextTile = getFirstInRow(currentRow);
					lastTile = getLastInRow(currentRow);
				} while (nextTile == null && currentRow <= getLastRow()); // 避免跳過空行

			} else {
				nextTile = getNextInRow(currentTile.getLocation());
			}

			return currentTile;
		}

		// TODO: anything else?
		private HexTile getNextInRow(HexCoordinate currentCoord) {
			assert wellFormed() : "in getNextInRow()";

			Node current = root;
			HexTile nextInRow = null;

			while (current != null) {
				if (current.loc.b() == currentCoord.b() && current.loc.a() > currentCoord.a()) {
					if (nextInRow == null || compare(current.loc, nextInRow.getLocation()) < 0) {
						nextInRow = new HexTile(current.terrain, current.loc);
					}
				}
				// 確保完整遍歷
				if (compare(currentCoord, current.loc) < 0) {
					current = current.left;
				} else {
					current = current.right;
				}
			}
			return nextInRow;
		}
	}


	/**
	 * Used for testing the invariant.  Do not change this code.
	 */
	public static class Spy {
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
		 * A public version of the data structure's internal node class.
		 * This class is only used for testing.
		 */
		public static class Node extends HexBoard.Node {
			// Even if Eclipse suggests it: do not add any fields to this class!
			/**
			 * Create a node with null data and null next fields.
			 */
			public Node() {
				this(null, null,null, null);
			}
			/**
			 * Create a node with the given values
			 * @param location and terrain data for new node, may be null
			 * @param l left for new node, may be null
			 * @param r right for new node, may be null
			 */
			public Node(HexCoordinate loc,Terrain terrain, Node l, Node r) {
				super(null,null);
				this.loc = loc;
				this.terrain = terrain;
				this.left = l;
				this.right = r;
			}

			/**
			 * Change the data in the node.
			 * @param s new string to use
			 */
			public void setTile(HexCoordinate loc,Terrain terrain) {
				this.loc = loc;
				this.terrain = terrain;
			}

			/**
			 * Change a node by setting the "left" field.
			 * @param n new left field, may be null.
			 */
			public void setLeft(Node n) {
				this.left = n;
			}

			/**
			 * Change a node by setting the "right" field.
			 * @param n new right field, may be null.
			 */
			public void setRight(Node n) {
				this.right = n;
			}
		}

		/**
		 * Create a debugging instance of the ADT
		 * with a particular data structure.
		 * @param r root
		 * @param m size, many nodes
		 * @return a new instance of a BallSeq with the given data structure
		 */
		public HexBoard newInstance(Node r, int m) {
			HexBoard result = new HexBoard();
			result.root = r;
			result.size = m;
			return result;
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param lx instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(HexBoard lx) {
			return lx.wellFormed();
		}

		/**
		 * Return the count of the nodes in this subtree.
		 * @param p subtree to count nodes for (may be null)
		 * @return number of nodes in the subtree.
		 */
		public int countNodes(Node p) {
			return HexBoard.countNodes(p);	
		}

		/**
		 * Return the result of the helper method isInProperOrder
		 * @param n node to check for
		 * @param lo lower bound
		 * @param hi upper bound
		 * @return result of running isInProperOrder on a debugging instance of HexCoordinate
		 */
		public boolean isInProperOrder(Node n, HexCoordinate lo, HexCoordinate hi) {
			HexBoard lx = new HexBoard();
			lx.root = null;
			lx.size = -1;
			return HexBoard.isInProperOrder(n,lo,hi);
		}
		/**
		 * Compare two hex Coordinates
		 * @param HexCoord H1 and HexCoord H2
		 * @return Integer 0 if equal, neg if H1<H2 and pos if H1>H2
		 */
		public int compare(HexCoordinate h1, HexCoordinate h2) {
			return HexBoard.compare(h1, h2);
		}

		/**
		 * Return the "b" coordinate of the first tile in the
		 * hex board, or 0 if the board is empty.
		 * @return row of first tile or zero if none
		 */
		public int getFirstRow(HexBoard lx) {
			return lx.getFirstRow();

		}
		/**
		 * Return the "b" coordinate of the last tile in the
		 * hex board, or 0 if the board is empty.
		 * @return row of last tile, or zero if none
		 */
		public int getLastRow(HexBoard lx) {
			return lx.getLastRow();
		}
		/**
		 * Return the first (leftmost) hex tile in the given row, if any
		 * @param b row number (second [part of hex coordinate)
		 * @return hex tile with lowest a with this b location, 
		 * or null if no such hex tile.
		 */
		public HexTile getFirstInRow(HexBoard lx, int b) {
			return lx.getFirstInRow(b);

		}
		/**
		 * Return the first (rightmost) hex tile in the given row, if any
		 * @param b row number (second [part of hex coordinate)
		 * @return hex tile with highest a with this b location, 
		 * or null if no such hex tile.
		 */
		public HexTile getLastInRow(HexBoard lx, int b) {
			return lx.getLastInRow(b);

		}


	}
}
