package edu.uwm.cs351;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import edu.uwm.cs351.util.AbstractEntry;

/**
 * An implementation of the HexBoard ADT using 
 * a binary search tree implementation.
 * A hex board is a collection of hex tiles except that there can 
 * never be two tiles at the same location. 
 */
public class HexBoard extends AbstractSet<HexTile> implements Cloneable {

	private static int compare(HexCoordinate h1, HexCoordinate h2) {
		if (h1.b() == h2.b()) {
			return h1.a() - h2.a();
		}
		return h1.b() - h2.b();
	}

	private static class Node extends AbstractEntry<HexCoordinate, Terrain>
	{
		HexCoordinate loc;
		Terrain terrain;
		Node left, right;
		Node(HexCoordinate l, Terrain t) { loc = l; terrain = t; }
		@Override
		public HexCoordinate getKey() {
			return loc;
		}
		@Override
		public Terrain getValue() {
			return terrain;
		}
		@Override
		public Terrain setValue(Terrain value) {
			if (value == null) {
				throw new NullPointerException();
			}
			Terrain old = terrain;
			terrain = value;
			return old;
		}
	}

	private Node root;
	private int size;
	private int version;

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
		if (r == null) return true;
		if (r.loc == null) return report("null location in tree");
		if (r.terrain == null) return report("null terrain for " + r.loc);
		if (lo != null && compare(lo,r.loc) >= 0) return report("out of order " + r.loc + " <= " + lo);
		if (hi != null && compare(hi,r.loc) <= 0) return report("out of order " + r.loc + " >= " + hi);
		return isInProperOrder(r.left,lo,r.loc) && isInProperOrder(r.right,r.loc,hi);
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
		if (!isInProperOrder(root,null,null)) return false;
		int count = countNodes(root);
		if (size != count) return report("size " + size + " wrong, should be " + count);
		return true;
	}

	/**
	 * Create an empty hex board.
	 */
	public HexBoard() {
		root = null;
		size = 0;
		assert wellFormed() : "in constructor";
	}

	/** Return the terrain at the given coordinate or null
	 * if nothing at this coordinate.
	 * @param c hex coordinate to look for (null OK but pointless)
	 * @return terrain at that coordinate, or null if nothing
	 */
	public Terrain terrainAt(HexCoordinate c) {
		assert wellFormed() : "in terrainAt";
		if (c == null) return null;
		for (Node p = root; p != null; ) {
			int cmp = compare(c,p.loc);
			if (cmp == 0) return p.terrain;
			if (cmp < 0) p = p.left;
			else p = p.right;
		}
		return null;
	}

	@Override // required by Java
	public Iterator<HexTile> iterator() {
		assert wellFormed() : "in iterator";
		return new MyIterator();
	}
	@Override // required by Java
	public int size() {
		assert wellFormed() : "in size";	
		return size;
	}

	@Override // efficiency
	public boolean contains(Object o) {
		assert wellFormed() : "in contains()";
		if (o instanceof HexTile) {
			HexTile h = (HexTile)o;
			return terrainAt(h.getLocation()) == h.getTerrain();
		}
		return false;
	}

	@Override // implementation
	public boolean add(HexTile e) {
		assert wellFormed() : "in add()";
		Node lag = null;
		Node p = root;
		int c = 0;
		while (p != null) {
			c = compare(e.getLocation(),p.loc);
			if (c == 0) break;
			lag = p;
			if (c < 0) p = p.left;
			else p = p.right;
		}
		if (p != null) { // found it!
			if (p.terrain == e.getTerrain()) return false;
			p.terrain = e.getTerrain();
			// size doesn't increase...
		} else {
			p = new Node(e.getLocation(),e.getTerrain());
			++size;
			if (lag == null) root = p;
			else if (c < 0) lag.left = p;
			else lag.right = p;
		}
		++version;
		assert wellFormed() : "after add()";
		return true;
	}

	@Override // efficiency
	public void clear() {
		assert wellFormed() : "invariant broken before clear()";
		if (size > 0) {
			root = null;
			size = 0;
			++version;
		}
		assert wellFormed() : "invariant broken by clear()";
	}

	private Node doRemove(Node r, HexTile ht) {
		int c = compare(ht.getLocation(),r.loc);
		if (c == 0) {
			if (r.left == null) return r.right;
			if (r.right == null) return r.left;
			Node sub = r.left;
			while (sub.right != null) {
				sub = sub.right;
			}
			r.loc = sub.loc;
			r.terrain = sub.terrain;
			r.left = doRemove(r.left, new HexTile(r.terrain,r.loc));
		} else if (c < 0) {
			r.left = doRemove(r.left, ht);
		} else {
			r.right = doRemove(r.right, ht);
		}
		return r;
	}

	@Override // for efficiency and because the iterator uses this, for implementation
	public boolean remove(Object x) {
		assert wellFormed() : "invariant broken before remove()";
		if (!(x instanceof HexTile)) return false;
		HexTile ht = (HexTile)x;
		if (!contains(ht)) return false;
		root = doRemove(root,ht);
		--size;
		++version;
		assert wellFormed() : "invariant broken after remove";
		return true;
	}

	private Node doClone(Node r) {
		if (r == null) return null;
		Node c = new Node(r.loc, r.terrain);
		c.left = doClone(r.left);
		c.right = doClone(r.right);
		return c;
	}

	@Override // decorate
	public HexBoard clone() {
		assert wellFormed() : "invariant failed at start of clone";
		HexBoard result;

		try
		{
			result = (HexBoard) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}

		result.root = doClone(root);
		assert wellFormed() : "invariant failed at end of clone";
		assert result.wellFormed() : "invariant failed for clone";

		return result;
	}

	
	
	private class MyIterator implements Iterator<HexTile>{

		private EntrySetIterator entryIt ;
		private boolean hasRow;
		private int row;

		public MyIterator(int row) {
			entryIt = new EntrySetIterator(row);
			this.row = row;
			this.hasRow=true;
		}

		public MyIterator() {
			entryIt = new EntrySetIterator();
			this.hasRow = false;
		}

		@Override
		public boolean hasNext() {
			if(!hasRow) {
				return entryIt.hasNext();
			}else {
				return entryIt.hasNext(row);
			}
		}

		@Override
		public HexTile next() {
			if(!hasNext()) {
				throw new NoSuchElementException("No element in this row");
			}
			Entry<HexCoordinate, Terrain> entry = entryIt.next();
			HexTile hexTile = new HexTile(entry.getValue(), entry.getKey());
			return hexTile;
		}

		@Override
		public void remove() {
			entryIt.remove();
		}

	}
	

	/**
	 * Return a set backed by this hex board that
	 * has all the tiles in the given row.
	 * The result is <i>backed</i> by this hex board;
	 * changes to either are reflected in the other.
	 * @param r row number.
	 * @return set of hextiles with the given row
	 */

	public Set<HexTile> row(int r) {
		assert wellFormed():"at the begin of row";
		return new Row(r); // TODO
	}

	/**
	 * Return a view of this hex board as a map from hex coordinates to terrain.
	 * It is as efficient as the hex board itself.
	 * @return
	 */
	public Map<HexCoordinate,Terrain> asMap() {
		return new MyMap(); 
	}



	// TODO: add nested classes to implement map, entry set, and row.
	private class Row extends AbstractSet<HexTile> {
		private final int row;

		private Row(int row) {
			this.row = row;
		}

		@Override
		public boolean add(HexTile h) {
			assert wellFormed():"at the beginning of add";
			// row 不對，不能加
			if (h.getLocation().b() != row) throw new IllegalArgumentException("HexTile is not belong to row" + row);
			if (h == null || h.getLocation().b() != row) throw new NullPointerException("h is null");
			return HexBoard.this.add(h); // 加到整個 HexBoard 裡
		}

		@Override
		public Iterator<HexTile> iterator() {
			assert wellFormed():"at the beginning of iterator()";
			return new MyIterator(row);
		}

		@Override
		public int size() {
			assert wellFormed():"at the beginning of size";
			int count = 0;
			Iterator<HexTile> it =iterator();
			while(it.hasNext()) {
				it.next();
				count++;
			}
			assert wellFormed():"at the end of the size";
			return count;
		}

		@Override //efficiency
		public boolean contains(Object o) { //檢查一個物件 o 是否在某個集合裡面
			if (!(o instanceof HexTile)) return false; 
				HexTile h = (HexTile) o; //強制轉型為 HexTile
				if(h.getLocation().b() == row) {
				return HexBoard.this.contains(h);
				}
			return false;
		}

		@Override //efficiency
		public boolean remove(Object o) {
			if (o instanceof HexTile) {
				HexTile h = (HexTile) o;
				if (h.getLocation().b() == row) return HexBoard.this.remove(h);
			}
			return false;
		}
		
		@Override
		public boolean isEmpty() {
			assert wellFormed() : "in isEmpty() of EntrySet";
		    return !iterator().hasNext();
		}
	}


	// The map and entry set classes must not have any fields!
	// The row class may only have a "final" field (for the row number).
	// Assuming you can use a separate constructor for MyIterator,
	// this class can be used for row iterators too.

	// 巢狀在 HexBoard 裡
	private class EntrySet extends AbstractSet<Map.Entry<HexCoordinate, Terrain>> {

		@Override 
		public Iterator<Map.Entry<HexCoordinate, Terrain>> iterator() {
			assert wellFormed():("in iterator() of EntrySet");
			return new EntrySetIterator();
		}

		@Override 
		public int size() {
			assert wellFormed():("in size of EntrySet");
			return HexBoard.this.size;
		}
		@Override //efficiency
		public boolean contains(Object o) {
			if(!(o instanceof Map.Entry)) return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>) o;
			if(!(e.getKey() instanceof HexCoordinate) || !(e.getValue()
					instanceof Terrain)) return false; //檢查 key 是不是 HexCoordinate，value 是不是 Terrain
			HexCoordinate key = (HexCoordinate) e.getKey();
			Terrain actual = terrainAt(key);
			return actual != null && actual.equals(e.getValue()); //compare value
		}
		@Override //efficiency
		public boolean remove(Object o) {
			if(!(o instanceof Map.Entry)) return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>) o;
			if(!(e.getKey() instanceof HexCoordinate) || !(e.getValue()
					instanceof Terrain)) return false;
			HexCoordinate key = (HexCoordinate) e.getKey();
			Terrain t = (Terrain)e.getValue();
			return HexBoard.this.remove(new HexTile(t, key));
		}

	}
	private class MyMap extends AbstractMap<HexCoordinate, Terrain> {

		@Override
		public Set<Map.Entry<HexCoordinate, Terrain>> entrySet() {
			return new EntrySet();
		}

		@Override
		public Terrain put(HexCoordinate key, Terrain value) {
			assert wellFormed() : "invariant brfore put()";
			if (key == null || value == null) throw new NullPointerException();
			Terrain addOne = get(key);
			add(new HexTile(value, key));
			assert wellFormed() : "invariant after put()";
			return addOne;
		}
		@Override //efficiency
		public Terrain get(Object key) {
			assert wellFormed() : "at the beginning get() of MyMap";
			if (!(key instanceof HexCoordinate)) return null;
			return terrainAt((HexCoordinate) key);
		}
		@Override //efficiency
		public boolean containsKey(Object key) {
			assert wellFormed() : "at the beginning containsKey() of map";
			if (!(key instanceof HexCoordinate)) return false;
			return terrainAt((HexCoordinate) key) != null;
		}
		@Override //efficiency
		public Terrain remove(Object key) {
			assert wellFormed() : "at the beginning of MyMap";
			if (!(key instanceof HexCoordinate)) return null;
			HexCoordinate k = (HexCoordinate) key;
			Terrain t = terrainAt(k);
			if(t!= null)
				HexBoard.this.remove(new HexTile(t, k));
			return t;
		}
	}

	private class EntrySetIterator implements Iterator<Entry<HexCoordinate,Terrain>> {
		// Separate this into two classes:
		// One an entry set iterator, and the other a wrapper

		// around it that returns hex tiles up to a particular row number.
		private Stack<Node> pending = new Stack<>();
		private HexTile current; // if can be removed
		private int myVersion = version;
		private int rowFilter = 0; // -1 means no row filtering

		private boolean wellFormed() {
			if (!HexBoard.this.wellFormed()) return false;
			if (version == myVersion) {
				if (pending == null) return report("null pending");
				@SuppressWarnings("unchecked")
				Stack<Node> clone = (Stack<Node>) pending.clone();
				Node p = null;
				if (current != null) {
					boolean found = false;
					for (Node r=root; r != null; ) {
						if (compare(current.getLocation(),r.loc) < 0) {
							p = r; // remember GT ancestor
							r = r.left;
						} else {
							if (r.loc.equals(current.getLocation())) found = true; // changed from HW #9
							r = r.right;
						}
					}
					if (!found) return report("didn't find current in tree.");
					if (p == null) {
						if (!clone.isEmpty()) return report("stack isn't empty, but hextile is last");
					} else {
						if (clone.isEmpty()) return report("stack is empty, but hextile is not last");
						if (clone.peek() != p) return report("top of stack is not next node from current");
					}
				}
				p = null;
				while (!clone.isEmpty()) {
					Node q = clone.pop();
					if (q == null) return report("Found null on stack");
					Node r = q.left;
					while (r != p && r != null) {
						r = r.right;
					}
					if (r != p) return report("Found bad node " + q + " on stack");
					p = q;
				}				
				if (p != null) {
					Node r = root;
					while (r != p && r != null) {
						r = r.right;
					}
					if (r != p) return report("Bottom node on stack not a right descendant of root: " + p);
				}
			}
			return true;
		}


		private void pushNodes(Node p) {
			while (p != null) {
				pending.push(p);
				p = p.left;
			}
		}

		private void checkVersion() {
			if (version != myVersion) throw new ConcurrentModificationException("stale");
		}

		public EntrySetIterator(boolean ignore) {}

		// Default constructor - no row filtering


		// Row-specific constructor
		EntrySetIterator(int row) {
			rowFilter = row;
			// Find nodes that could contain entries in this row
			Node n = root;
			while (n != null) {
				if (row <= n.loc.b()) {
					pending.push(n);
					n = n.left;
				} else {
					n = n.right;
				}
			}
			this.myVersion = version;
			assert wellFormed() : "invariant failed at end of EntrySetIterator(row)";
		}


		private EntrySetIterator() {
			pushNodes(root);
			assert wellFormed();
		}

		@Override // required by Java
		public boolean hasNext() {
			assert wellFormed() : "in hasNext";
			checkVersion();
			return !pending.isEmpty();
		}

		public boolean hasNext(int row) {
			assert wellFormed() : "in hasNext(row)";
			checkVersion();
			while (!pending.isEmpty()) {
				Node p = pending.peek();
				if (p.loc.b() == row) return true;
				if (p.loc.b() < row) {
					pending.pop();
					pushNodes(p.right);
				} else {
					return false;
				}
			}
			return false;
		}


		@Override // required by Java
		public Node next() {
			assert wellFormed() : "in next";
			if (!hasNext()) throw new NoSuchElementException("no more");
			Node p = pending.pop();
			pushNodes(p.right);
			current = new HexTile(p.terrain,p.loc);
			assert wellFormed() : "invariant broken at end of next()";
			return p;
		}

		@Override // implementation
		public void remove() {
			assert wellFormed() : "invariant broken at start of remove()";
			checkVersion();
			if (current == null) throw new IllegalStateException("nothing to remove");
			if(!HexBoard.this.remove(current)){
				if(terrainAt(current.getLocation())==null) {
					throw new RuntimeException("someting went wrong!");
				}
				//原本 remove(current) 失敗，是因為 current 裡的 terrain 是舊的，跟現在樹裡那塊 tile 的 terrain 已經不一樣了（reference 變了），所以 contains() 判斷失敗，導致 remove() 沒真的刪。
				HexTile actual = new HexTile(terrainAt(current.getLocation()), current.getLocation());
				//修正方法就是重新拿出「樹裡真實的 terrain」，重新構造 HexTile 再拿去刪，就不會錯了 
				HexBoard.this.remove(actual);
			}
			myVersion = version;
			current = null;
			assert wellFormed() : "invariant broken at end of remove()";
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
				this(null, null, null, null);
			}

			/**
			 * Create a node with the given values
			 * @param loc location of new node, may be null
			 * @param terrain data for new node, may be null
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
			 * @param loc location 
			 * @param terrain terrain
			 */
			public void setTile(HexCoordinate loc,Terrain terrain) {
				this.loc = loc;
				this.terrain = terrain;
			}

			/**
			 * Return the terrain of the testing node
			 * @return its terrain
			 */
			public Terrain getTerrain() {
				return terrain;
			}

			/**
			 * Get left child of testing node.
			 * @return left child
			 */
			public Node getLeft() {
				return (Node)left;
			}

			/**
			 * Get right child of testing node.
			 * @return right child
			 */
			public Node getRight() {
				return (Node)right;
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
		 * @param v version
		 * @return a new instance of a ADT with the given data structure
		 */
		public HexBoard newInstance(Node r, int m, int v) {
			HexBoard result = new HexBoard();
			result.root = r;
			result.size = m;
			result.version = v;
			return result;
		}

		/**
		 * Create a testing iterator instance
		 * @param base the hex board
		 * @param p stack of pending nodes
		 * @param c value of "current"
		 * @param v value of "colVersion"
		 * @return new testing instance
		 */
		@SuppressWarnings("unchecked")
		public Iterator<Map.Entry<HexCoordinate,Terrain>> newIterator(HexBoard base, Stack<Node> p, HexTile c, int v) {
			HexBoard.EntrySetIterator result = base.new EntrySetIterator(false);
			result.pending = (Stack<HexBoard.Node>)(Stack<?>)p;
			result.current = c;
			result.myVersion = v;
			return result;
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param b instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(HexBoard b) {
			return b.wellFormed();
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
		 * @param h1 first to compare, must not be null
		 * @param h2 second to compare, must not be null
		 * @return Integer 0 if equal, neg if H1<H2 and pos if H1>H2
		 */
		public int compare(HexCoordinate h1, HexCoordinate h2) {
			return HexBoard.compare(h1, h2);
		}

		/**
		 * Checking if a testing iterator has the correct form,
		 * according to its "wellFormed" method.
		 * @param it testing iterator, must not be null and must have been created by {@link #newIterator}
		 * @return whether the data structure meets the requirements
		 */
		public boolean wellFormed(Iterator<Map.Entry<HexCoordinate, Terrain>> it) {
			EntrySetIterator myIt = (EntrySetIterator)it;
			return myIt.wellFormed();
		}

	}
}

