package edu.uwm.cs351.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

public class TreeMap<K,V>  extends AbstractMap<K,V> {

	// Here is the data structure to use.

	private static class Node<K,V> extends DefaultEntry<K,V> {
		Node<K,V> left, right;
		Node(K k, V v) {
			super(k,v);
			left = right = null;
		}
	}

	private Comparator<K> comparator;
	private Node<K,V> root;
	private int numItems = 0;
	private int version = 0;


	/// Invariant checks:
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	private static boolean report(String s) {
		reporter.accept("Invariant error: " + s);
		return false;
	}

	/**
	 * Return true if keys in this subtree are never null and are correctly sorted
	 * and are all in the range between the lower and upper (both exclusive).
	 * If either bound is null, then that means that there is no limit at this side.
	 * @param node root of subtree to examine
	 * @param lower value that all nodes must be greater than.  If null, then
	 * there is no lower bound.
	 * @param upper value that all nodes must be less than. If null,
	 * then there is no upper bound.
	 * @return true if the subtree is fine.  If false is returned, a problem
	 * should have already been reported.
	 */
	private boolean checkInRange(Node<K,V> node, K lower, K upper) {
		if (node == null) return true;
		if (node.key == null) return report("Found null data in tree");
		if (lower != null && comparator.compare(lower, node.key) >= 0) return report("Found out of order data: " + node.key + " not coming after lower " + lower);
		if (upper != null && comparator.compare(node.key,upper) >= 0) return report("Found out of order data: " + node.key + " not coming before upper " + upper);
		return checkInRange(node.left,lower,node.key) && 
				checkInRange(node.right,node.key,upper);
	}

	/**
	 * Return the number of nodes in the subtree rooted at n.
	 * This operation counts nodes; it does not use {@link #numItems}.
	 * @param n reference to subtree in which to count nodes.
	 * @return number of nodes in subtree.
	 */
	private int count(Node<K,V> n) {
		if (n == null) return 0;
		return count(n.left)+ 1 + count(n.right); 
	}

	/**
	 * Check the invariant, printing a message if not satisfied.
	 * @return whether invariant is correct
	 */
	private boolean wellFormed() {
		if (comparator == null) return report("null comparator");
		if (!checkInRange(root,null,null)) return false;
		int count = count(root);
		if (count != numItems) return report("count wrong: " + numItems + ", should be " + count);
		return true;
	}


	/// constructors

	private TreeMap(boolean ignored) {} // do not change this.

	/**
	 * Create an empty tree map, assuming that the type is comparable.
	 * If it is not comparable, then errors will happen as pairs
	 * are added to the tree.
	 */
	public TreeMap() {
		comparator = new Comparator<K>() {
			@SuppressWarnings({ "unchecked" })
			public int compare(K arg0, K arg1) {
				return ((Comparable<K>)arg0).compareTo(arg1);
			}
		};
		assert wellFormed() : "invariant broken after constructor()";
	}

	/**
	 * Create an empty tree map using the given comparator.
	 * @param c comparator to order elements, must not be null
	 * @throws IllegalArgumentException if the comparator is null
	 */
	public TreeMap(Comparator<K> c) {
		if (c == null) throw new IllegalArgumentException("comparator must not be null");
		comparator = c;
		assert wellFormed() : "invariant broken after constructor(Comparator)";
	}

	// The following is a useful private method to check that the
	// given object is a key in the tree.  If it is not the correct type
	// (or if the tree is empty) this method returns null.
	@SuppressWarnings("unchecked")
	private K asKey(Object x) {
		if (root == null || x == null) return null;
		try {
			comparator.compare(root.key,(K)x);
			comparator.compare((K)x,root.key);
			return (K)x;
		} catch (ClassCastException ex) {
			return null;
		}
	}

	private Node<K,V> getNode(Object o) {
		K x = asKey(o);
		if (x == null) return null;
		Node<K,V> r = root;
		while (r != null) {
			int c = comparator.compare(x,r.key);
			if (c == 0) return r;
			if (c < 0) r = r.left;
			else r = r.right;
		}
		return null;
	}

	@Override // efficiency (and make independent of iterators)
	public boolean containsKey(Object o) {
		assert wellFormed() : "invariant broken at start of contains";
		return getNode(o) != null;
	}

	@Override // efficiency (and make independent of iterators)
	public V get(Object o) {
		assert wellFormed() : "invariant broken at start of get";
		Node<K,V> n = getNode(o);
		if (n == null) return null;
		return n.value;
	}



	/// mutation:

	/**
	 * Add a binding to the map.
	 * @param key to add, must not be null
	 * @throws NullPointerException if the key is null.
	 */
	@Override // implementation
	public V put(K key, V value) throws IllegalArgumentException {
		assert wellFormed() : "invariant broken at beginning of put";
		if (key == null) throw new NullPointerException("Cannot use null as a key");
		V res = do_put(root,key,value,null,false);
		assert wellFormed() : "invariant broken at end of put";
		return res;
	}

	private void connect(Node<K,V> parent, boolean isr, Node<K,V> child) {
		if (parent == null) root = child;
		else if (isr) parent.right = child;
		else parent.left = child;
	}

	private V do_put(Node<K,V> n, K key, V value, Node<K,V> parent, boolean isr) {
		if (n == null) {
			++numItems;
			n = new Node<K,V>(key,value);
			connect(parent,isr,n);
			++version;
			return null;
		}
		int c = comparator.compare(key, n.key);
		if (c == 0) {
			V res = n.value;
			n.value = value;
			return res;
		}
		if (c < 0) {
			return do_put(n.left,key,value,n,false);
		} else {
			return do_put(n.right,key,value,n,true);
		}
	}

	@Override // implementation
	public V remove(Object o) {
		assert wellFormed() : "invariant broken at beginning of remove";
		V result;
		K x = asKey(o);		
		if (x == null) result = null;
		else result = do_remove(root,x,null,false);
		assert wellFormed() : "invariant broken at end of remove";
		return result;
	}

	private V do_remove(Node<K,V> n, K key, Node<K,V> parent, boolean isr) {
		if (n == null)  return null;
		int c = comparator.compare(key, n.key);
		if (c == 0) {
			return do_remove_here(n, parent, isr);
		} else if (c < 0) {
			return do_remove(n.left,key,n, false);
		} else {
			return do_remove(n.right,key,n, true);
		}
	}

	private V do_remove_here(Node<K, V> n, Node<K, V> parent, boolean isr) {
		++version;
		if (n.left == null) {
			//System.out.println("Case A: " + n);
			connect(parent,isr,n.right);
			-- numItems;
			return n.value;
		}
		Node<K,V> t = n.left;
		if (t.right == null) {
			//System.out.println("Case B: " + n);
			connect(t,true,n.right);
			connect(parent, isr, t);
			--numItems;
			return n.value;
		} else {
			//System.out.println("Case C: " + n);
			V saved = n.value;
			Node<K,V> prev = null;
			while (t.right != null) {
				prev = t;
				t = t.right;
			}
			n.key = t.key;
			n.value = do_remove_here(t,prev,true);
			return saved;
		}
	}

	private volatile Set<Entry<K,V>> entrySet;

	@Override // required
	public Set<Entry<K, V>> entrySet() {
		assert wellFormed() : "invariant broken at beginning of entrySet";
		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}

	/**
	 * The "backing" set for this map.
	 * In other words, this set doesn't have its own data structure:
	 * it uses the data structure of the map.
	 */
	private class EntrySet extends AbstractSet<Entry<K,V>> {

		@Override // required
		public int size() {
			assert wellFormed() : "invariant broken in size()";
			return numItems;
		}

		@Override // required
		public Iterator<Entry<K, V>> iterator() {
			assert wellFormed() : "invariant broken in iterator()";
			return new MyIterator();
		}

		// You don't need to override "add".  Why not?

		@Override // efficiency
		public boolean contains(Object o) {
			assert wellFormed() : "Invariant broken at start of EntrySet.contains";
			if (!(o instanceof Entry<?,?>)) return false;
			Entry<?,?> e = (Entry<?,?>)o;
			Node<K,V> node = getNode(e.getKey());
			if (node == null) return false;
			return node.equals(o);
		}

		@Override // efficiency
		public boolean remove(Object x) {
			assert wellFormed() : "Invariant broken at start of EntrySet.remove";
			if (!contains(x)) return false;
			TreeMap.this.remove(((Entry<?,?>)x).getKey());
			// the following check is redundant because TreeMap.remove already called it: 
			assert wellFormed() : "Invariant broken at end of EntrySet.remove";
			return true;
		}

		@Override // efficiency, also to make independent of "remove"
		public void clear() {
			assert wellFormed() : "invariant broken at beginning of clear";
			if (root == null) return;
			root = null;
			numItems = 0;
			++version;
			assert wellFormed() : "invariant broken at end of clear";
		}
	}


	/**
	 * Iterator over the map.
	 * We use a stack of nodes.
	 */
	private class MyIterator implements Iterator<Entry<K,V>> {
		final Stack<Node<K,V>> pending = new Stack<Node<K,V>>();
		Node<K,V> current = null;
		int myVersion = version;

		boolean wellFormed() {
			if (!TreeMap.this.wellFormed()) return false;
			if (version != myVersion) return true;
			if (pending == null) return report("pending is null");
			Node<K,V> node = root;
			Node<K,V> pre = null;
			for (Node<K,V> n : pending) {
				if (n == null) return report("null in stack");
				while (n != node) {
					if (node == null) return report("unexpected node in stack: " + n);
					pre = node;
					node = node.right;
				}
				node = node.left;
			}
			if (current != null) {
				if (node == null) {
					if (current != pre) return report("current is " + current + ", but expected " + pre);
				} else {
					while (node.right != null) node = node.right;
					if (current != node) return report("current is " + current + ", not " + node);
				}
			}
			return true;
		}

		MyIterator() {
			Node<K,V> n = root;
			while (n != null) {
				pending.push(n);
				n = n.left;
			}
			assert wellFormed() : "invariant broken after iterator constructor";
		}

		public void checkVersion() {
			if (version != myVersion) throw new ConcurrentModificationException("stale iterator");
		}

		@Override // required
		public boolean hasNext() {
			assert wellFormed() : "invariant broken before hasNext()";
			checkVersion();
			return !pending.isEmpty();
		}

		@Override // required
		public Entry<K, V> next() {
			assert wellFormed() : "invariant broken at start of next()";
			if (!hasNext()) throw new NoSuchElementException("at end of map");
			current = pending.pop();
			for (Node<K,V> n = current.right; n != null; n = n.left) {
				pending.push(n);
			}
			assert wellFormed() : "invariant broken at end of next()";
			return current; 
		}

		@Override // implementation
		public void remove() {
			assert wellFormed() : "invariant broken at start of iterator.remove()";
			checkVersion();
			if (current == null) throw new IllegalStateException("cannot remove now");
			TreeMap.this.remove(current.key);
			current = null;
			myVersion = ++version;
			assert wellFormed() : "invariant broken at end of iterator.remove()";
		}

	}


	/// MergeSort Assignment

	// In order to handle adding large numbers of entries
	// at once, we will use a system of converting trees to
	// sorted cyclic doubly linked lists, sorting unsorted lists, and 
	// merging sorted lists.  We will use the existing Nodes and interpret
	// "left" as "prev" and "right" as "next".

	/**
	 * Return true if the given head is either null (empty) or
	 * a cyclic DLL list.  No null values are permitted as keys.
	 * If there is a problem, it should be reported.
	 * @param head start of supposed cyclic DLL, or null
	 * @return true if it is indeed a well-formed cyclic DLL without null keys.
	 */
	private boolean wellFormed(Node<K,V> head) {
		// Hint; This is (supposed to be) a DLL, so no need to use Tortoise & Hare
		if (head == null) return true; //空列表

		Node<K,V> current = head; //從頭開始追蹤
		do {
			if (current.key == null) return report("Node with null key found in DLL");
			Node<K,V> next = current.right;
			Node<K,V> prev = current.left;
			if (next == null) return report("node " + current + " right is null");
			if (prev == null) return report("node " + current + " left is null");
			if (next.left != current) return report("node " + current + " right.left mismatch (was " + next.left + ")");
			if (prev.right != current) return report("node " + current + " left.right mismatch (was " + prev.right + ")");
			current = next;
		} while (current != head);

		return true;
	}

	/**
	 * Return true if the given head of a cyclic DLL is strictly sorted
	 * in ascending order according to the comparator (that is, without duplicates).
	 * The list is assumed to be well formed.  This method
	 * works as an invariant check: it calls {@link #report(String)} if problems are found.
	 * @param head start of a cyclic DLL list (possibly null)
	 * @return whether the list is sorted.  If false is returned, a problem will have been reported.
	 */
	private boolean isSorted(Node<K,V> head) {
		if (!wellFormed(head)) throw new IllegalArgumentException("not a wellFormed cyclic DLL");
		if (comparator == null) return report("Comparator is null");
		if (head == null) return true; // 空串列視為已排序

		Node<K,V> current = head;
		do {
			Node<K,V> next = current.right;
			if (next == head) break; // 到尾端（下一個是頭）就結束

			if (comparator.compare(current.key, next.key) >= 0)
				return report("List is not strictly ascending: " + current.key + " >= " + next.key);// 檢查是否嚴格遞增（不允許重複）
			current = next;
		} while (current != head);

		return true;
	}

	/**
	 * Count the length of a cyclic DLL.
	 * The list is unchanged.
	 * @param l a possibly empty cyclic DLL.
	 * @return number of nodes in the cycle
	 */
	private int length(Node<K,V> l) {
		assert wellFormed(l);
		int count = 0;
		if (l == null) return 0; 
		Node<K,V> current = l;

		do {
			count++;  // 增加節點計數
			current = current.right;  // 移動到下一個節點
		} while (current != l);  // 當回到起始節點時停止

		return count;  // 返回鏈表的長度
	}

	/**
	 * Add a node to the end of a possibly empty DLL cyclic list.
	 * @param l possibly empty cyclic DLL to add to
	 * @param n node to add (not null), must not be in the list already
	 * @return list with new element added to end
	 */
	private Node<K,V> add(Node<K,V> l, Node<K,V> n) {
		assert wellFormed(l);
		if (l == null) {
			// 唯一節點：自我指向
			n.left = n.right = n;
			return n;
		}
		Node<K,V> tail = l.left; //l.left 是尾端節點
		tail.right = n;
		n.left = tail;
		n.right = l;
		l.left = n;

		assert wellFormed(l);
		return l;
	}

	/**
	 * Add the second list to the end of the first list.
	 * @param l1 possibly empty cyclic DLL to append to
	 * @param l2 possibly empty cyclic DLL to append to first list
	 * @return appended list
	 */
	private Node<K,V> append(Node<K,V> l1, Node<K,V> l2) {
		assert wellFormed(l1) && wellFormed(l2);
		if (l1 == null) return l2;
		if (l2 == null) return l1;

		// 非空串列合併
		Node<K,V> tail1 = l1.left; // l1 尾端
		Node<K,V> tail2 = l2.left; // l2 尾端

		// 串接：l1 的尾巴接 l2 的頭
		tail1.right = l2;
		l2.left = tail1;

		// 串接：l2 的尾巴接回 l1 的頭
		tail2.right = l1;
		l1.left = tail2;

		assert wellFormed(l1);
		return l1;
	}

	/**
	 * Convert a subtree rooted at r into a sorted cyclic DLL ith all the same nodes.
	 * All the old nodes are re-purposed as DLL nodes.
	 * @param r subtree, may be null
	 * @return resulting sorted cyclic DLL, may be null
	 */
	private Node<K,V> toList(Node<K,V> r) {
		assert checkInRange(r, null, null);
		Node<K,V> result = r;
		if (r == null) return null;

		// 遞迴處理左子樹與右子樹
		Node<K,V> leftList = toList(r.left);
		Node<K,V> rightList = toList(r.right);

		// 把當前節點轉成循環 DLL（單一節點也要自己指向自己）
		r.left = r.right = r;

		//		// 使用已經寫好的 helper 方法組裝
		result = append(leftList, r);
		result = append(result, rightList);   // 再接右邊
		assert wellFormed(result) && isSorted(result);
		return result;
	}

	/**
	 * Split a cyclic DLL into two segments; 
	 * the first n elements remaining in the original
	 * list and the remaining elements in a possibly empty new cyclic DLL
	 * that is returned
	 * @param l non-empty cyclic list
	 * @param n positive length of elements to keep, must be less or equal to the length of n
	 * @return possibly empty list of remaining elements
	 */
	private Node<K,V> split(Node<K,V> l, int n) {
		assert wellFormed(l) && length(l) >= n;
		if (n <= 0) throw new IllegalArgumentException("split needs to take a positive number");
		Node<K,V> result = l;
		for (int i = 1; i < n; i++) {
			result = result.right;
		}

		// result 是第 n 個節點
		Node<K,V> newHead = result.right;

		// 如果切完後沒有剩下（也就是 newHead == l），代表整個循環都被切掉
		if (newHead == l) {
			return null;
		}

		// 拆開循環：讓 result 成為新尾端，l 成為新頭，newHead 成為剩下鏈的頭
		Node<K,V> leftTail = result;
		Node<K,V> rightHead = newHead;
		Node<K,V> rightTail = l.left;
		// 拆開兩邊的循環串列
		leftTail.right = l;
		l.left = leftTail;

		rightTail.right = rightHead;
		rightHead.left = rightTail;

		assert wellFormed(l) && wellFormed(result) && length(l) == n;
		return newHead;
	}

	private Node<K,V> merge(Node<K,V> l1, Node<K,V> l2) {
		assert wellFormed(l1) && isSorted(l1);
		assert wellFormed(l2) && isSorted(l2);
		if (l1 == null) return l2;
		if (l2 == null) return l1;
		Node<K,V> result = null;

		while (l1 != null && l2 != null) {
			int cmp = comparator.compare(l1.key, l2.key);
			if (cmp < 0) {
				Node<K,V> one = l1;
				l1 = split(l1,1); //spit(從node拿,size＝一個)
				result = add(result,one);
			} else if (cmp == 0) {
				Node<K,V> two = l2;
				l2=split(l2,1);
				result = add(result, two); // 保留 l2，丟棄 l1
				l1 = split(l1,1);
			} else {
				Node<K,V> two = l2;
				l2 = split(two, 1);
				result = add(result, two);
			}
		}
		// 如果 l1 還有剩，把它剩下的節點加入
		if(l1 != null) {
			result = append(result, l1);
		}
		if (l2 != null) {
			result = append(result, l2);
		}

		assert wellFormed(result) && isSorted(result);
		return result;
	}

	/**
	 * Sort the given cyclic DLL list and return the result.
	 * @param l a possibly empty cyclic DLL
	 * @param size length of this list.
	 * @return sorted cyclic list
	 */
	private Node<K,V> sort(Node<K,V> l, int size) {
		assert wellFormed(l) && length(l) == size;
		Node<K,V> result = l;

		if (size <= 1) {
			// 當鏈表長度為 0 或 1 時，視為已排序，直接回傳
			return l;
		}

		// 步驟一：找到中間節點，將鏈表一分為二
		int mid = size / 2;
		Node<K,V> midNode = l;
		for (int i = 0; i < mid; ++i) {
			midNode = midNode.right; // 向右走 mid 步找到中點
		}

		// 儲存左右兩段的尾節點，用來斷開循環結構
		Node<K,V> leftTail = midNode.left;
		Node<K,V> rightTail = l.left;

		// 將原本的循環結構斷開，使兩半變成線性鏈表
		leftTail.right = l;
		l.left = leftTail;

		rightTail.right = midNode;
		midNode.left = rightTail;

		// 遞迴排序左右兩半
		Node<K,V> leftSorted = sort(l, mid);
		Node<K,V> rightSorted = sort(midNode, size - mid);

		// 合併兩個排序好的線性雙向鏈表，回傳合併後的循環鏈表
		result = merge(leftSorted, rightSorted);

		assert wellFormed(result) && isSorted(result);
		return result;
	}

	/**
	 * Convert a sorted cyclic DLL to a balanced BST
	 * using the existing nodes.
	 * @param l possibly empty cyclic DLL
	 * @param size number of elements in the cyclic DLL
	 * @return well formed tree of all nodes formerly in cyclic DLL.
	 */
	private Node<K,V> toTree(Node<K,V> l, int size) {
		assert wellFormed(l) && isSorted(l) && length(l) == size;
		Node<K,V> r = l;
		if (size == 0) return null;
		if(size == 1) {
			l.right = null;
			l.left = null;
			return l;
		}
		int mid = size / 2;
		Node<K,V> root = split(l,mid);
		Node<K,V> leftList = l;
		Node<K,V> rightList = split(root,1);
		r = root;

		root.left = toTree(leftList, mid);
		root.right = toTree(rightList, size - mid - 1);

		assert checkInRange(r,null,null) && count(r) == size;
		return r;
	}

	@Override // efficiency
	public void putAll(Map<? extends K, ? extends V> m) {
		assert wellFormed() : "invariant broken before putAll";
		if (m.size() <= this.size()) {
			// System.out.println("(using default implementation)");
			super.putAll(m); // more efficient to simply put one by one
			return; 
		}
		// TODO
		// 1. Create a cyclic DLL of entries from m
	    Node<K, V> cyclicList = null;
	    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
	        Node<K, V> newNode = new Node<>(entry.getKey(), entry.getValue());
	        cyclicList = add(cyclicList, newNode);
	    }
		// 2. Sort the new list
	    cyclicList = sort(cyclicList,length(cyclicList));
		// 3. Convert existing tree to a (sorted) list
	    Node<K,V> treeList = toList(root);	
		// 4. Merge the two lists.
	    Node<K,V> merged = merge(treeList, cyclicList);
	    int mergedSize = length(merged); 
	    numItems = mergedSize;
		// 5. Convert the result back to a tree
	   root =toTree(merged,mergedSize);
		// 6. increment version (our iterator cannot cope with possibly new nodes
		//    even if the size didn't change.)
	    version++;
		assert wellFormed() : "invariant broken in putAll";
	}

	/**
	 * Used for testing the invariant.  Do not change this code.
	 */
	public static class Spy<K,V> {
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
		public static class Node<K,V> extends TreeMap.Node<K,V> {
			// Even if Eclipse suggests it: do not add any fields to this class!
			/**
			 * Create a node with null key and null value fields.
			 * @param k key 
			 * @param v value
			 */
			public Node(K k, V v) {
				this(k,v,null, null);
			}
			/**
			 * Create a node with the given values
			 * @param l left for new node, may be null
			 * @param r right for new node, may be null
			 */
			public Node(K k, V v, Node<K,V> l, Node<K,V> r) {
				super(k,v);
				this.left=l;
				this.right=r;
			}

			/**
			 * Change a node by setting the "left" field.
			 * @param n new left field, may be null.
			 */
			public void setLeft(Node<K,V> n) {
				this.left = n;
			}

			/**
			 * Change a node by setting the "right" field.
			 * @param n new right field, may be null.
			 */
			public void setRight(Node<K,V> n) {
				this.right = n;
			}
			/**
			 * Return the left field.
			 * @return the left field, may be null.
			 */
			public Node<K,V> getLeft() {
				return (Node<K,V>)this.left;
			}

			/**
			 * Return the right field.
			 * @return the right field, may be null.
			 */
			public Node<K,V>  getRight() {
				return (Node<K,V>)this.right;
			}

			/**
			 * sets the key to new key k
			 * @paramskk field k to set for current node.
			 */
			public void setKey(K k) {
				this.key = k;
			}

			/**
			 * sets the value to new value v
			 * @param v value to set for current node.
			 * @return the old value for the node.
			 */
			public V setValue(V v) {
				V oldv = this.value;
				this.value = v;
				return oldv;
			}
		}

		/**
		 * @param root of a tree map
		 * @param c the comparator
		 * @param numItems the size
		 * @param version the version
		 * @return the new instance with input variables
		 */
		public TreeMap<K,V> newInstance(Node<K,V> root, Comparator<K> c, int numItems, int version){
			TreeMap<K,V> result = new TreeMap<>(false);
			result.root=root;
			result.comparator = c;
			result.numItems = numItems;
			result.version = version;
			return result;
		}

		/**
		 * Return true if the given head is either null (empty) or
		 * a cyclic DLL list.  No null values are permitted as keys.
		 * If there is a problem, it should be reported.
		 * @param tm instance of to use, must not be null
		 * @param head start of supposed cyclic DLL, or null
		 * @return true if it is indeed a well-formed cyclic DLL without null keys.
		 */
		public boolean wellFormed(TreeMap<K, V> tm,Node<K,V> head) {
			return tm.wellFormed(head);
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param tm instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(TreeMap<K, V> tm) {
			return tm.wellFormed();
		}

		/**
		 * Return true if the given head of a cyclic DLL is strictly sorted
		 * in ascending order according to the comparator (that is, without duplicates).
		 * The list is assumed to be well formed.  This method
		 * works as an invariant check: it calls doReport with if problems are found.
		 * @param tm instance of to use, must not be null
		 * @param head start of a cyclic DLL list (possibly null)
		 * @return whether the list is sorted.  If false is returned, a problem will have been reported.
		 */
		public boolean isSorted(TreeMap<K, V> tm,Node<K,V> head) {
			return tm.isSorted(head);
		}

		/**
		 * Setter function for numItems
		 * @param tm instance of to use, must not be null
		 * @param numItems the size to set
		 */
		public void setNumItems(TreeMap<K, V> tm,int numItems) {
			tm.numItems = numItems;
		}

		/**
		 * setter function to set root
		 * @param tm instance of to use, must not be null
		 * @param root a new root node
		 */
		public void setRoot(TreeMap<K, V> tm,Node<K,V> root) {
			tm.root = root;
		}

		/**
		 * getter function for root node
		 * @param tm instance of to use, must not be null
		 * @return the root of tm
		 */
		public Node<K,V> getRoot(TreeMap<K, V> tm) {
			return (Node<K,V>)tm.root;
		}

		/**
		 * setter function for comparator
		 * @param tm instance of to use, must not be null
		 * @param c a value to set for comparator
		 */
		public void setComparator(TreeMap<K, V> tm,Comparator<K> c) {
			tm.comparator = c;
		}
		/**
		 * Sort the given cyclic DLL list and return the result.
		 * @param tm instance of to use, must not be null
		 * @param l a possibly empty cyclic DLL
		 * @param size length of this list.
		 * @return sorted cyclic list
		 */
		public Node<K,V> sort(TreeMap<K, V> tm,Node<K,V> l, int size) {
			return (Node<K,V>)tm.sort(l, size);
		}

		/**
		 * Convert a sorted cyclic DLL to a balanced BST
		 * using the existing nodes.
		 * @param tm instance of to use, must not be null
		 * @param l possibly empty cyclic DLL
		 * @param size number of elements in the cyclic DLL
		 * @return well formed tree of all nodes formerly in cyclic DLL.
		 */
		public Node<K,V> toTree(TreeMap<K, V> tm,Node<K,V> l, int size) {
			return (Node<K,V>)tm.toTree(l, size);
		}

		/**
		 * Split a cyclic DLL into two segments; 
		 * the first n elements remaining in the original
		 * list and the remaining elements in a possibly empty new cyclic DLL
		 * that is returned
		 * @param tm instance of to use, must not be null
		 * @param l non-empty cyclic list
		 * @param n positive length of elements to keep, must be less or equal to the length of n
		 * @return possibly empty list of remaining elements
		 */
		public Node<K,V> split(TreeMap<K, V> tm,Node<K,V> l, int n) {
			return (Node<K,V>)tm.split(l, n);
		}

		/**
		 * Return true if keys in this subtree are never null and are correctly sorted
		 * and are all in the range between the lower and upper (both exclusive).
		 * If either bound is null, then that means that there is no limit at this side.
		 * @param tm instance of to use, must not be null
		 * @param node root of subtree to examine
		 * @param lower value that all nodes must be greater than.  If null, then
		 * there is no lower bound.
		 * @param upper value that all nodes must be less than. If null,
		 * then there is no upper bound.
		 * @return true if the subtree is fine.  If false is returned, a problem
		 * should have already been reported.
		 */
		public boolean checkInRange(TreeMap<K, V> tm,Node<K,V> node, K lower, K upper) {
			return tm.checkInRange(node, lower, upper);
		}

		/**
		 * Convert a subtree rooted at r into a sorted cyclic DLL ith all the same nodes.
		 * All the old nodes are re-purposed as DLL nodes.
		 * @param tm instance of to use, must not be null
		 * @param r subtree, may be null
		 * @return resulting sorted cyclic DLL, may be null
		 */
		public Node<K,V> toList(TreeMap<K, V> tm,Node<K,V> r) {
			return (Node<K,V>)tm.toList(r);
		}

		/**
		 * Add the second list to the end of the first list.
		 * @param tm instance of to use, must not be null
		 * @param l1 possibly empty cyclic DLL to append to
		 * @param l2 possibly empty cyclic DLL to append to first list
		 * @return appended list
		 */
		public Node<K,V> append(TreeMap<K, V> tm,Node<K,V> l1, Node<K,V> l2) {
			return (Node<K,V>)tm.append(l1, l2);
		}

		/**
		 * Count the length of a cyclic DLL.
		 * The list is unchanged.
		 * @param tm instance of to use, must not be null
		 * @param l a possibly empty cyclic DLL.
		 * @return number of nodes in the cycle
		 */
		public int length(TreeMap<K, V> tm,Node<K,V> l) {
			return tm.length(l);
		}

		/**
		 * Merge l1 and l2 and return the result
		 * @param tm instance of to use, must not be null
		 * @param l1 and l2 cyclic DLLs to merge.
		 * @return result of merging two cyclic DLL
		 */
		public Node<K,V> merge(TreeMap<K, V> tm,Node<K,V> l1, Node<K,V> l2) {
			return (Node<K,V>)tm.merge(l1, l2);
		}

		/**
		 * Add a node to the end of a possibly empty DLL cyclic list.
		 * @param tm instance of to use, must not be null
		 * @param l possibly empty cyclic DLL to add to
		 * @param n node to add (not null), must not be in the list already
		 * @return list with new element added to end
		 */
		public Node<K,V> add(TreeMap<K, V> tm,Node<K,V> l, Node<K,V> n) {
			return (Node<K,V>)tm.add(l, n);
		}


	}
}
