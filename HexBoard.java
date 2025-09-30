package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * An implementation of the HexBoard ADT using 
 * a binary search tree implementation.
 * A hex board is a collection of hex tiles except that there can 
 * never be two tiles at the same location. 
 */
public class HexBoard extends AbstractCollection<HexTile> implements Cloneable {

	private static int compare(HexCoordinate h1, HexCoordinate h2) {
		if (h1.b() == h2.b()) {
			return h1.a() - h2.a();
		}
		return h1.b() - h2.b();
	}

	private static class Node {
		HexCoordinate loc;
		Terrain terrain;
		Node left, right;
		Node(HexCoordinate l, Terrain t) { loc = l; terrain = t; }
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
	public Terrain terrainAt(HexCoordinate l) {
		assert wellFormed() : "in terrainAt";
		for (Node p = root; p != null; ) {
			int c = compare(l,p.loc);
			if (c == 0) return p.terrain;
			if (c < 0) p = p.left;
			else p = p.right;
		}
		return null;
	}

	@Override // required
	public Iterator<HexTile> iterator() {
		assert wellFormed() : "in iterator";
		return new MyIterator();
	}

	@Override // required
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
		if (size > 0) {
			root = null;
			size = 0;
			++version;
		}
	}

	// TODO: method(s) for remove.
	public boolean remove(Object o) {
		assert wellFormed() : "before remove()";

		if(!(o instanceof HexTile)) return false;

		HexTile hexTile = (HexTile) o;
		Terrain t =terrainAt(hexTile.getLocation());
		if(t==null || !t.equals(hexTile.getTerrain())) {
			return false;
		}
		root = removeNode(root,hexTile.getLocation());
		size--;
		// 更新版本號
		++version;

		assert wellFormed() : "after remove()";
		return true;  // 成功移除
	}
	private Node removeNode(Node p, HexCoordinate loc) {
		if (p == null) return null; 

		int c = compare(loc, p.loc);  // 比較 loc 與當前節點位置

		if (c < 0) {
			p.left = removeNode(p.left, loc);
		} else if (c > 0) {
			p.right = removeNode(p.right, loc);
		} else {
			//if left=null,then return right
			if (p.left == null) return p.right;
			if (p.right == null) return p.left;
			//if both have left and right
			Node n= p.right;
			while(n.left!=null) {
				n=n.left;
			}
			p.loc = n.loc;
			p.terrain = n.terrain;
			// 移除右子樹中的最小節點
			p.right = removeNode(p.right, n.loc);
		}
		return p;
	}

	// TODO: Method(s) for clone.
	@Override
	public HexBoard clone() {
		try {
			HexBoard copy = (HexBoard) super.clone(); //super.clone() 進行淺複製
			copy.root = copyTree(this.root);
			copy.version = 0;
			assert copy.wellFormed();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("clone not supported");
		}
	}

	private Node copyTree(Node p) {
		if (p == null) return null;
		Node n = new Node(p.loc, p.terrain); // // 創建一個新的 Node，並遞歸複製左右子樹
		n.left = copyTree(p.left);
		n.right = copyTree(p.right);
		return n;
	}



	private class MyIterator implements Iterator<HexTile> {
		// new data structure for iterator:
		private Stack<Node> pending = new Stack<>();
		private HexTile current; // if can be removed
		private int myVersion = version;

		private boolean wellFormed() {
			// TODO:
			// 1. Check the outer invariant (see new syntax in homework description)
			if (!HexBoard.this.wellFormed()) return false;
			// 2. If we are stale, don't check anything else, pretend no problems
			if (myVersion != version) return true;
			// 3. If current isn't null, there should be a node for it in the tree.
			if (current != null) {
				Terrain found = terrainAt(current.getLocation());
				if(found == null || !found.equals(current.getTerrain())) {
					return report("Node is not in the tree");
				}
			}
			// 4. If current isn't null, the next node after it should be top of the stack
			if (current!=null && !pending.isEmpty()) {
				Node top = findNode(root,current);
				Node successor = findSuccessor(root,top);
				if(successor !=pending.peek()) {
					return report("the next node after it should be top of the stack");

				}
			}
			// 5. If the stack isn't empty, then it should have all greater ancestors of top of stack and nothing else.

			if (pending == null) {
			    return report("stack is empty");
			}

			Stack<Node> clone = (Stack<Node>) pending.clone();
			if (clone.isEmpty()) {
			    return true;
			}

			Node top = clone.pop();

			// Ensure the top node is part of the tree
			if (!isInTree(root, top)) {
			    return report("Node in stack is not in the tree");
			}

			Stack<Node> expected = new Stack<>();
			Node current = root;

			// Traverse the tree to find the expected ancestor nodes of the top node
			while (current != null) {
			    int comparison = compare(top.loc, current.loc);

			    if (comparison < 0) {
			        expected.push(current);
			        current = current.left;
			    } else if (comparison > 0) {
			        current = current.right;
			    } else {
			        break;
			    }
			}

			// Compare the nodes in the clone stack with the expected ancestor nodes
			while (!clone.isEmpty() && !expected.isEmpty()) {
			    Node actualNode = clone.pop();
			    Node expectedNode = expected.pop();

			    if (actualNode != expectedNode) {
			        return report("Stack contains incorrect ancestor");
			    }
			}

			// If there are extra or missing ancestors in the stack, report it
			if (!clone.isEmpty() || !expected.isEmpty()) {
			    return report("Stack has extra or missing ancestors");
			}

			return true;
		}

		private MyIterator(boolean ignored) {} // do not change, and do not use in your code

		// TODO: any helper method(s) (see homework description)
		private Node findNode(Node root, HexTile current) {
			if(root ==null) return null;

			int cmp = compare(current.getLocation(), root.loc);
			if(cmp==0 && root.terrain.equals(current.getTerrain())) return root;
			if(cmp<0) return findNode(root.left, current);
			return findNode(root.right, current);
		}

		private Node findSuccessor(Node root, Node node) {
			if(node.right!=null) {
				Node target = node.right;
				while(target.left !=null) {
					target = target.left;
				}
				return target;
			}
			Node succ =null;
			while(root!=null) {
				int cmp = compare(node.loc, root.loc);
				if(cmp<0) {
					succ = root;
					root = root.left;
				}else if(cmp>0) {
					root = root.right;
				}
				else {
					break;
				}
			}
			return succ;
		}
		private boolean isInTree(Node root, Node target) {
			if(root ==null) return false;
			if(root.equals(target)) return true;
			return isInTree(root.left, target) || isInTree(root.right, target);
		}

		private MyIterator() {
			// TODO
			assert wellFormed();
			myVersion = HexBoard.this.version;
			Node n = root;
			while (n != null) {
				pending.push(n);
				n = n.left;
			}
			assert wellFormed():"at the end of MyIterator";

		}

		@Override // required
		public boolean hasNext() {
			if (myVersion != version) {
				throw new ConcurrentModificationException();
			}
			return !pending.isEmpty(); // TODO
		}

		@Override // required
		public HexTile next() {
			if (!hasNext()) throw new NoSuchElementException();

			Node n = pending.pop(); // get the next node
			current = new HexTile(n.terrain, n.loc); // convert node to HexTile

			Node right = n.right;
			while (right != null) {
				pending.push(right);
				right = right.left;
			}
			assert wellFormed();
			return current; // TODO: find next entry and generate hex tile on demand
		}

		@Override // implementation
		public void remove() {
			//			throw new UnsupportedOperationException("no removal yet"); 
			assert wellFormed():"at the beginning of remove";
			if (myVersion != version) throw new ConcurrentModificationException();
			if (current == null) throw new IllegalStateException();
			HexBoard.this.remove(current);
			version++;
			myVersion = version;
			HexCoordinate currentLocation = current.getLocation();
			current=null;

			pending.clear();
			Node n = root;
			while (n != null) {
				int comp = compare(currentLocation, n.loc);
				if (comp < 0) {
					pending.push(n);
					n = n.left;
				} else {
					n = n.right;
				} 
			}

			assert wellFormed() : "after remove()";
			// TODO
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
		 * @return a new instance of a BallSeq with the given data structure
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
		public Iterator<HexTile> newIterator(HexBoard base, Stack<Node> p, HexTile c, int v) {
			HexBoard.MyIterator result = base.new MyIterator(false);
			result.pending = (Stack<HexBoard.Node>)(Stack<?>)p;
			result.current = c;
			result.myVersion = v;
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
		 * Checking if a testing iterator has the correct form,
		 * according to its "wellFormed" method.
		 * @param it testing iterator, must not be null and must have been created by {@link #newIterator}
		 * @return whether the data structure meets the requirements
		 */
		public boolean wellFormed(Iterator<HexTile> it) {
			MyIterator myIt = (MyIterator)it;
			return myIt.wellFormed();
		}

	}
}