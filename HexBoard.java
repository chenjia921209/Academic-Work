package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import edu.uwm.cs351.HexBoard.HexPiece;
import edu.uwm.cs351.util.Primes;

public class HexBoard extends AbstractCollection<HexBoard.HexPiece> {

	@Override
	public Iterator<HexPiece> iterator() {
		// TODO Auto-generated method stub
		return new MyIterator();
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * A class of mutable hex tiles that have object identity,
	 * and are linked with other pieces. 
	 */
	public static class HexPiece {
		final HexCoordinate location;
		Terrain terrain;		
		HexPiece[] neighbors = new HexPiece[HexDirection.values().length];
		HexPiece nextInChain; // new to Homework #11

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
		 * Get the tile in the given direction on the board, if any
		 * @param d direction to look, must not be null
		 * @return piece in that direction, possibly null
		 */
		public HexPiece move(HexDirection d) {
			return neighbors[d.ordinal()];
		}

		@Override // implementation
		public String toString() {
			return terrain + "" + location + super.toString();
		}
	}


	private static final int INITIAL_CAPACITY = 7; // initial and minimum capacity

	/// The data structure: three fields.  DO not add any more

	private HexPiece[] table;
	private int size;
	private int version;


	/** Return the location within the hash table
	 * where the element is, or would be inserted (to add it)
	 * @param t coordinate to look for, must not be null
	 * @return location where piece with this coordinate is or would go
	 */
	private int locate(HexCoordinate t) { //helper method
		// 取得 hashCode，並對 table.length 取模，避免負數
		int h = t.hashCode();
		int i = h % table.length;
		if (i < 0) {
			i = i + table.length; // 保證索引不為負數
		}
		return i;
	}

	/**
	 * Return the piece at the given hex coordinate, if any.
	 * @param h hex coordinate
	 * @return piece at that coordinate, or null if none
	 */
	private HexPiece findPiece(HexCoordinate h) {
		// TODO find a hex piece at the given coordinate.

		for (HexPiece target : table) {
			HexPiece p = target;
			if (p == null) continue;

			//記錄已經走訪過的 HexPiece，避免在「鏈結」中重複走訪 因為不是每條鏈都一定是循環的
			java.util.HashSet<HexPiece> visited = new java.util.HashSet<>();
			//table.length
			while (p != null && visited.add(p)) {
				if (p.location.equals(h)) return p;
				p = p.nextInChain;
			}
		}
		return null;
	}

	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private boolean wellFormed() {
		/* Invariant: <ul>
		 * <li>The table array is not null</li>
		 * <li>The length of the array is a prime number and at least the initial; capacity</li>
		 * <li>Every chain is cyclic  to the start (use modified T&H)</li>
		 * <li>Every hex piece in a chain is in the correct chain.</li>
		 * <li>The total number of pieces in the chains matches 'size'</li>
		 * <li>The number of pieces is always less than the size of the table.</li>
		 * <li> None of the pieces have null terrains></li>
		 * <li> No two of the pieces have the same hex coordinate </li>
		 * <li> For every piece: 
		 *    a neighbor in a certain direction is in the table iff it is in the neighbor array for that direction.
		 *    </li>
		 */
		// TODO (You may wish to copy some code from the solution to Homework #5)
		if (table == null) return report("table is null");// 1. The table array is not null
		// 2. The length of the array is a prime number and at least the initial capacity
		if (table.length < INITIAL_CAPACITY || !Primes.isPrime(table.length)) return report("table length is is a prime number and at least the initial capacity");
		// 3. Every chain is cyclic to the start (modified Tortoise and Hare)
		int total = 0;
		java.util.HashSet<HexCoordinate> seen = new java.util.HashSet<>();

		for (int i = 0; i < table.length; ++i) {
			HexPiece start = table[i];
			if (start == null) continue;

			// Use Tortoise and Hare to detect cycle
			HexPiece slow = start;
			HexPiece fast = start.nextInChain;
			while (fast != null && fast.nextInChain != null) {
				if (slow == fast) break;
				slow = slow.nextInChain;
				fast = fast.nextInChain.nextInChain;
			}

			if (fast == null || fast.nextInChain == null) return report("Chain not cyclic");

			// 4. Every hex piece is in the correct chain (i.e., it hashes to this slot) 
			// 5. Count the number of pieces
			for (HexPiece p = start;; p = p.nextInChain) {
				if (p == null) return report("Unexpected null in chain");

				if (p.terrain == null) return report("Null terrain in piece");

				// Check coordinates unique
				if (!seen.add(p.location)) return report("the coordinate is not the only " + p.location);

				// Hash index check
				int correctIndex = (p.location.hashCode()) % table.length;
				if(correctIndex<0) {
					correctIndex=table.length+correctIndex;	
				}
				if (correctIndex != i) return report("Piece with location " + p.location + " is in wrong chain");

				// 9. Neighbor consistency
				for(HexDirection direction: HexDirection.values()) { //copy from HW5
					HexPiece neighbor = p.neighbors[direction.ordinal()];
					HexCoordinate expected= direction.move(p.location);
					HexPiece current = findPiece(expected);
					if(current!=neighbor) {
						return report("the tile is not the same as neighbor");
					}
					if(neighbor != null && p != neighbor.neighbors[direction.reverse().ordinal()]) {
						return report("the neighbor is not point back to the current tail");
					}

				}
				total++;
				if (p.nextInChain == start) break;
			}
		}

		// 6. size field should match the number of counted pieces
		if (total != size) return report("size field does not match actual number of pieces: " + total + " vs " + size);

		// 7. The number of pieces is less than the table length
		if (size >= table.length) return report("Too many elements in table: size >= table.length");

		return true;
	}


	private HexBoard(boolean ignored) { } // do not change this constructor

	public HexBoard() {
		table = new HexPiece[INITIAL_CAPACITY];
		size = 0;
		version = 0;
		assert wellFormed() : "invariant not estabished in constructor";
	}

	private void connect(HexPiece p) {
		HexCoordinate h = p.getLocation();
		for (HexDirection d : HexDirection.values()) {
			HexCoordinate h2 = d.move(h);
			HexPiece p2 = findPiece(h2);
			if (p2 != null) {
				p.neighbors[d.ordinal()] = p2;
				p2.neighbors[d.reverse().ordinal()] = p;
			}
		}
	}

	private void disconnect(HexPiece p) {
		for (HexDirection d : HexDirection.values()) {
			HexPiece p2 = p.neighbors[d.ordinal()];
			if (p2 != null) {
				p2.neighbors[d.reverse().ordinal()] = null;
				p.neighbors[d.ordinal()] = null;
			}
		}
	}

	// TODO: overrides (required/implementation/efficiency)
	@Override
	public boolean add(HexPiece e) {
		assert wellFormed() : "at the beginning of add";

		if (e.nextInChain != null) { //testL0,L1 不允許將已經加入過某個 HexBoard 的 HexPiece 再次加入到同一個或其他 HexBoard
			throw new IllegalArgumentException("HexPiece is already part of a HexBoard");
		}

		// 檢查該位置是否已經有 HexPiece
		HexPiece checkingPiece = findPiece(e.getLocation());
		if (checkingPiece != null) {
			// 如果已經存在，更新該 HexPiece 的 terrain
			checkingPiece.terrain = e.getTerrain();
			return false;  // 返回 false，表示沒有新增元素，只是更新了已存在的元素
		}
		// 如果該位置沒有已存在的 HexPiece，則像原來一樣插入新元素
		int index = locate(e.getLocation());
		HexPiece head = table[index];

		if (head == null) {
			// 第一個元素 -> 使其形成一個循環，指向自己
			e.nextInChain = e;
			table[index] = e;
		} else {
			// 插入到鏈表中，保持循環
			e.nextInChain = head.nextInChain;
			head.nextInChain = e;
		}
		size++;

		if (size >= table.length) {
			// 計算新的容量
			int newCapacity = Primes.nextTwinPrime(table.length * 2);


			// 創建新數組並將表格容量加倍
			HexPiece[] newTable = new HexPiece[newCapacity];

			table = rehash(newTable);
		}
		version++;
		connect(e);  // 設置該 HexPiece 的 neighbors

		assert wellFormed() : "at the end of add";
		return true;
	}


	//helper method
	private HexPiece[] rehash(HexPiece[] newTable) {
		for (HexPiece head : table) {
			if (head != null) {
				HexPiece current = head;
				do {
					HexPiece next = current.nextInChain;  // 儲存下一個節點，避免搬家後指標混亂
					// 重新計算 index
					int correctIndex = current.location.hashCode() % newTable.length;
					if (correctIndex < 0) correctIndex += newTable.length;

					// 插入到新 table 的對應 bucket（維持循環鏈結結構）
					if (newTable[correctIndex] == null) {
						current.nextInChain = current;
						newTable[correctIndex] = current;
					} else {
						current.nextInChain = newTable[correctIndex].nextInChain;
						newTable[correctIndex].nextInChain = current;
					}
					current = next;
				} while (current != head);
			}
		}
		return newTable;
	}

	/**
	 * Return the piece at this hex coordinate (if it exists)
	 * @param h hex coordinate to look for, may be null
	 * @return hex piece at this location, or null if no such piece
	 */
	public HexPiece get(HexCoordinate h) {
		return findPiece(h);
	}

	private class MyIterator implements Iterator<HexPiece> {

		private int count; 
		private HexPiece current; 
		private HexPiece start; // the beginning of the current chain
		private int colVersion; 
		private HexPiece lastReturned;
		private int lastIndex = -1;
		private boolean removeOK = false;

		public MyIterator() {
			colVersion = version;
			count = -1;
			advance(); // move to the first real piece
		}
		private void checkVersion() {
			if (version != colVersion) throw new ConcurrentModificationException();
		}

		private void advance() {
			checkVersion();
			if (current != null && current.nextInChain != start) {
				current = current.nextInChain;
				return;
			}
			// Move to next non-null bucket
			current = null;
			start = null;
			count++;
			while (count < table.length) {
				if (table[count] != null) {
					current = table[count];
					start = current;
					return;
				}
				count++;
			}
		}

		@Override
		public boolean hasNext() {
			checkVersion();
			return current != null;
		}

		@Override
		public HexPiece next() {
			if (!hasNext()) throw new NoSuchElementException();
			lastReturned = current;
			lastIndex = count;
			removeOK = true;
			advance();
			if(lastReturned == current) {
				advance();
				if(lastReturned == current) {
					current=null;
				}
			}
			return lastReturned;
		}
		@Override
		public void remove() {
			checkVersion();
			if (!removeOK) throw new IllegalStateException();
			removeOK = false;

			disconnect(lastReturned); // 斷開 neighbors

			HexPiece head = table[lastIndex]; // 取得該元素所在 bucket 的頭節點

			if (head == lastReturned && head.nextInChain == head) {
				// 只有一個節點
				head.nextInChain=null;
				table[lastIndex] = null;
			} else {
				HexPiece prev = head;
				while (prev.nextInChain != lastReturned) {
					prev = prev.nextInChain;
				}
				prev.nextInChain = lastReturned.nextInChain;
				if (head == lastReturned) {
					table[lastIndex] = lastReturned.nextInChain; // 如果刪除的是頭節點，要更新 bucket 的頭
					table[lastIndex].nextInChain = table[lastIndex].nextInChain.nextInChain;
					lastReturned.nextInChain=null;
				}
			}
			if (current == lastReturned) { // 如果 current 剛好是被刪除的節點，重設 current & start (test89)
				current = null;
				start = null;
			}
			lastReturned.nextInChain = null;
			size--;
			version++;
			colVersion = version;
			lastReturned = null;
			
		}
	}
	//TODO: For nested MyIterator class:
	// You can choose your data structure.
	// Our solution keeps "current" and "next"
	// No need to write a data structure invariant for the iterator.


	/**
	 * Used for testing the invariant.  Do not change this code.
	 */
	public static class Spy {
		public static class MyHexPiece extends HexPiece {
			/**
			 * Create a debugging hex piece with the given parts
			 * @param t terrain, may be null
			 * @param h location, may NOT be null
			 */
			public MyHexPiece(Terrain t, HexCoordinate h) {
				super(Terrain.INACCESSIBLE, h);
				terrain = t;
			}

			/**
			 * Set the neighbor element
			 * @param d direction, must not be null
			 * @param p piece to use, may be null
			 */
			public void setNeighbor(HexDirection d, HexPiece p) {
				this.neighbors[d.ordinal()] = p;
			}

			/**
			 * Change the nextInChain field to the given value
			 * @param p what should be next in chain, may be null
			 */
			public void setNext(HexPiece p) {
				this.nextInChain = p;
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
		 * @param a array of hex pieces
		 * @param s purported size
		 * @return a new instance with the given data structure
		 */
		public HexBoard newInstance(HexPiece[] a, int s) {
			HexBoard result = new HexBoard(false);
			result.table = a;
			result.size = s;
			result.version = s * 117 + 43;
			return result;
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
