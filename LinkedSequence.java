package edu.uwm.cs351;

import java.util.function.Consumer;

// This is a Homework Assignment for CS 351 at UWM

/**
 * A cyclic doubly-linked list implementation of the textbook Sequence ADT.
 * @param E element type of the sequence
 */
public class LinkedSequence<E> implements Cloneable // TODO: implements what?
{
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	/**
	 * Used to report an error found when checking the invariant.
	 * By providing a string, this will help debugging the class if the invariant should fail.
	 * @param error string to print to report the exact error found
	 * @return false always
	 */
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	// TODO: Declare the private static generic Node class with fields data, prev and next.
	// The class should be private, static and generic.
	// Please use a different name for its generic type parameter, e.g. class Node<T>
	// It should have a constructor or two (at least the default constructor) but no methods.
	// The no-argument constructor can construct a dummy node if you would like.
	// The fields of Node should have "default" access (neither public, nor private)
	// Remember the dummy node should have a type-cast reference to itself for its data
	// So we should have dummy.data == dummy.
	// You are encouraged to @SuppressWarnings("unchecked") on the default constructor.
	@SuppressWarnings("unchecked")
	private static class Node<T> {
		T data;
		Node<T> prev, next;
		// Default constructor，建立一個虛擬節點（dummy node）
		Node() {
			this.data = (T) this; // 讓虛擬節點的 data 指向自身
			this.prev = this;
			this.next = this;
		}
		// General constructor，建立普通節點
		Node(T data, Node<T> prev, Node<T> next) {
			this.data = data;
			this.prev = prev;
			this.next = next;
		}
	}

	// TODO: Declare the private fields (See homework assignment)
	private Node<E> dummy;
	private int count;
	private Node<E> cursor;
	private LinkedSequence(boolean ignored) {} // DO NOT CHANGE THIS

	// TODO: Declare "wellFormed" and check the data structure
	// You should be able to figure out what checks to make.
	// We have internal tests to help you have confidence that you
	// are checking the right things.
	// Don't use unchecked casts to check dummy data; just use ==
	private boolean wellFormed() {
		if (dummy == null) return report("Dummy node is null");
		if (dummy.data != dummy) return report("Dummy node data does not reference itself");

		//如果 dummy.prev 或 dummy.next 任何一個是 null，則回報 "Dummy node is not properly linked"，表示鏈表沒有正確連接
		if (dummy.prev == null || dummy.next == null) return report("Dummy node is not properly linked");
		// 確保 dummy.prev.next == dummy 和 dummy.next.prev == dummy
		if (dummy.prev.next != dummy) return report("Dummy node's prev.next is incorrect");
		if (dummy.next.prev != dummy) return report("Dummy node's next.prev is incorrect");

		int actualCount = 0; //計算實際結點數量
		Node<E> fast = dummy, slow = dummy;
		Node<E> current = dummy.next; //用來遍歷整個e鏈表

		while (current != dummy) { //遍歷所有非 dummy 的節點
			if (current == null) return report("Null node found in list"); //確保鏈表內不會有 null 節點，如果 current 為 null，表示鏈表不完整

			if(current==current.data) return report("only dummy can have the same data");

			// 加強鏈接檢查
			if (current.prev.next != current || current.next.prev != current) {
				return report("Irregular link detected at node: " + current);
			}
			actualCount++;
			current = current.next;
			// Detect cycles using Floyd's cycle detection (tortoise and hare)
			slow = slow.next;
			fast = (fast.next != dummy && fast.next.next != dummy) ? fast.next.next : dummy;
			if (fast == slow && fast != dummy) {
				return report("Cycle detected in list");
			}
		}
		if (actualCount != count) {
			return report("Count mismatch: expected " + count + " but found " + actualCount);
			//count 應該等於 actualCount（真正的節點數
		}
		if (cursor != dummy) { //確保 cursor 真的在鏈表內
			Node<E> checkCursor = dummy.next;
			boolean found = false;
			while (checkCursor != dummy) {
				if (checkCursor == cursor) {
					found = true;
					break;
				}
				checkCursor = checkCursor.next;
			}
			if (!found) {
				return report("Cursor is not in the list");
			}
		}
		return true;
	}

	/**
	 * Create an empty sequence
	 */
	public LinkedSequence() {
		//TODO: implement this
		dummy = new Node<>();
		count = 0;
		cursor = dummy;
		assert wellFormed() : "invariant failed in constructor";
	}

	// TODO: Put all methods of the class here.
	// TODO: You should @SuppressWarnings("unchecked") on clone,
	// but no other methods should need it.
	public int size( )
	{
		assert wellFormed() : "invariant failed at start of size";
		// TODO: Implement this code.
		return count;
	}
	public void start( )
	{
		assert wellFormed() : "invariant failed at start of start";
		// TODO: Implement this code.
		cursor = dummy.next;
		assert wellFormed() : "invariant failed at end of start";
	}
	public boolean isCurrent( )
	{
		assert wellFormed() : "invariant failed at start of isCurrent";
		// TODO: Implement this code.
		return cursor != dummy;
	}
	//
	//	public boolean hasNext() {
	//		if(cursor.next!=null) return true;
	//		return false;
	//	}

	public E getCurrent( )
	{
		assert wellFormed() : "invariant failed at start of getCurrent";
		if (!isCurrent()) throw new IllegalStateException("No current element to return.");
		// 回傳當前的元素
		return (E) cursor.data;
	}
	public void advance( )
	{
		assert wellFormed() : "invariant failed at start of advance";
		// TODO: Implement this code.
		// 檢查是否有當前元素
		if (!isCurrent()) throw new IllegalStateException("No current element to advance");
		cursor = cursor.next;
		assert wellFormed() : "invariant failed at end of advance";
	}
	public void addBefore(E element)
	{
		assert wellFormed() : "invariant failed at start of addBefore";
		if(!isCurrent()) {
			Node<E> newNode = new Node<>((E) element, cursor, cursor.next);
			cursor.next.prev = newNode;
			cursor.next = newNode;
			cursor = newNode;
		}else {
			Node<E> newNode = new Node<>((E) element, cursor.prev, cursor);
			cursor.prev.next = newNode;
			cursor.prev = newNode;
			cursor = newNode;
		}
		count++;
		assert wellFormed() : "invariant failed at end of addBefore";
	}
	public void addAfter(E element)
	{
		assert wellFormed() : "invariant failed at start of addAfter";
		if(!isCurrent()) {
			Node<E> newNode = new Node<>((E) element, cursor.prev, cursor);
			cursor.prev.next = newNode;
			cursor.prev = newNode;
			cursor = newNode;
		}else {
			Node<E> newNode = new Node<>((E) element, cursor, cursor.next);
			cursor.next.prev = newNode;
			cursor.next = newNode;
			cursor = newNode;
		}
		count++;
		assert wellFormed() : "invariant failed at end of addAfter";
	}
	public void removeCurrent( )
	{
		assert wellFormed() : "invariant failed at start of removeCurrent";
		if (!isCurrent()) throw new IllegalStateException("No current element to remove");
		cursor.prev.next = cursor.next;
		cursor.next.prev = cursor.prev;
		cursor = cursor.next;
		count--;
		assert wellFormed() : "invariant failed at end of removeCurrent";
	}
	public void addAll(LinkedSequence<E> s)
	{
		assert wellFormed() : "invariant failed at start of addAll";
		if (s == null) throw new NullPointerException("Cannot add from a null sequence");
		LinkedSequence<E> sCopy=s.clone();
		dummy.prev.next=sCopy.dummy.next;
		sCopy.dummy.next.prev= dummy.prev;
		sCopy.dummy.prev.next=dummy;
		dummy.prev=sCopy.dummy.prev;
		count=count+sCopy.count;

		assert wellFormed() : "invariant failed at end of addAll";
	}   
	public LinkedSequence<E> clone( )
	{  // Clone a LinkedSeq object.
		assert wellFormed() : "invariant failed at start of clone";
		try {
			LinkedSequence<E> copy = (LinkedSequence<E>) super.clone();
			copy.dummy = new Node<>();
			copy.count = 0;
			copy.cursor = copy.dummy;
			Node<E> current = dummy.next;
			while (current != dummy) {
				copy.addAfter(current.data);

				current = current.next;
			}
			
			current=dummy.next;
			Node<E> copyCurrent = copy.dummy.next;
			if(cursor==dummy) {
				copy.cursor=copy.dummy;
			}else {
				while(current != dummy) {
					if(current==cursor) {
						copy.cursor = copyCurrent;
					}
					copyCurrent=copyCurrent.next;
					current = current.next;
				}
				
			}
			assert copy.wellFormed() : "invariant failed at end of clone";
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}


	// do not change this class -- it's used for internal testing:
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
		 * A debugging node class for use in testing only, not in client code.
		 * @param E data type
		 */
		public static class Node<E> extends LinkedSequence.Node<E> {
			public Node(E d) {
				super();
				this.data = d;
				this.prev = null;
				this.next = null;
			}
		}

		/**
		 * Make a debugging node with the given data element
		 * @param d data from the node
		 * @return new debugging node
		 */
		public <E> Node<E> makeNode(E d) {
			return new Node<>(d);
		}

		/**
		 * Return a debugging node whose data refers to itself
		 * @return new debugging node
		 */
		@SuppressWarnings("unchecked")
		public <E> Node<E> makeSelfRef() {
			Node<E> result = new Node<>(null);
			result.data = (E)result;
			return result;
		}

		/**
		 * Change the data field of a debugging node
		 * @param n node to change
		 * @param x new value of the data
		 */
		@SuppressWarnings("unchecked")
		public <E> void assignData(Node<E> n, Object x) {
			n.data = (E)x;
		}

		/**
		 * Link the nodes in the forward direction.
		 * @param first node to point forward to the first of the rest
		 * @param rest remaining nodes to be linked
		 */
		public <E> void linkForward(Node<E> first, @SuppressWarnings("unchecked") Node<E>... rest) {
			Node<E> last = first;
			for (Node<E> n : rest) {
				last.next = n;
				last = n;
			}
		}

		/**
		 * Link the nodes in the reverse direction.
		 * @param first node to point the first of the rest nodes back to
		 * @param rest remaining node to link
		 */
		public <E> void linkBackward(Node<E> first, @SuppressWarnings("unchecked") Node<E>... rest) {
			Node<E> last = first;
			for (Node<E> n : rest) {
				n.prev = last;
				last = n;
			}
		}

		/**
		 * Create an instance of LinkedCollection so that we can test the invariant checker.
		 * @param d dummy node
		 * @param s count
		 * @param c cursor
		 * @return instance of LinkedCollection that has not been checked.
		 */
		public <E> LinkedSequence<E> newInstance(Node<E> d, int s, Node<E> c) {
			LinkedSequence<E> result = new LinkedSequence<>(false);
			result.dummy = d;
			result.count = s;
			result.cursor = c;
			return result;
		}

		/**
		 * Check a linked collection's data structure
		 * @param lc linked collection to check, must not be null
		 * @return whether the data structure is deemed OK
		 */
		public <E> boolean wellFormed(LinkedSequence<E> lc) {
			return lc.wellFormed();
		}
	}
}
