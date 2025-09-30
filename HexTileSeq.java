// This is an assignment for students to complete after reading Chapter 3 of
// "Data Structures and Other Objects Using Java" by Michael Main.

package edu.uwm.cs351;

import java.util.function.Consumer;

/******************************************************************************
 * This class is a homework assignment;
 * A HexTileSeq is a collection of HexTiles.
 * The sequence can have a special "current element," which is specified and 
 * accessed through four methods that are not available in the sequence class 
 * (start, getCurrent, advance and isCurrent).
 ******************************************************************************/
public class HexTileSeq implements Cloneable
{
	// Implementation of the HexTileSeq class:
	//   1. The number of elements in the sequence is in the instance variable 
	//      manyItems.  The elements may be HexTile objects or nulls.
	//   2. For any sequence, the elements of the
	//      sequence are stored in data[0] through data[manyItems-1], and we
	//      don't care what's in the rest of data.
	//   3. If there is a current element, then it lies in data[currentIndex];
	//      if there is no current element, then currentIndex equals manyItems. 

	// TODO: Declare the private static Node class.
	// It should have a constructor but no methods.
	// The fields of Node should have "default" access (neither public, nor private)
	private static class Node{
		HexTile data;
		Node next;
		Node(HexTile data, Node next){
			this.data=data;
			this.next=next;
		}
	}
	private Node head;    // 指向鏈表的開頭
	private int manyNodes ;     // 紀錄鏈表長度
	private Node precursor;
	// TODO: Declare the private fields needed for sequences
	// (in the textbook, page 226, five are recommended, 
	// but you should only define three.  See Activity.)



	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private boolean wellFormed() {
		// Invariant:
		// 1. list must not include a cycle.
		// 2. manyNodes is number of nodes in list
		// 3. precursor is either null or points to a node in the list.precursor 要嘛是 null，要嘛指向鏈表中的某個節點

		// Implementation:
		// Do multiple checks: each time returning false if a problem is found.

		// We do the first one for you:
		// check that list is not cyclic
		if (head != null) {
			// This check uses the "tortoise and hare" algorithm attributed to Floyd.
			Node fast = head.next;
			for (Node p = head; fast != null && fast.next != null; p = p.next) {
				if (p == fast) return report("list is cyclic!");
				fast = fast.next.next;
			}
		}

		int count = 0;
		for (Node p = head; p != null; p = p.next) {
			count++;
		}
		if (count != manyNodes) {
			return report("size does not match the actual number of nodes!");
		}

		// Implement remaining conditions.
		if (precursor != null) {
			boolean found = false;
			for (Node p = head; p != null; p = p.next) {
				if (p == precursor) {
					found = true;
					break;
				}
			}
			if (!found) {
				return report("cursor is not pointing to a valid node in the list!");
			}
		}
		// If no problems discovered, return true
		return true;
	}

	// This is only for testing the invariant.  Do not change!
	private HexTileSeq(boolean testInvariant) { }

	/**
	 * Initialize an empty sequence.
	 **/   
	public HexTileSeq( )
	{
		// TODO: Implement this code.
		head = null;     
		precursor=null;
		manyNodes = 0;
		assert wellFormed() : "Invariant false at end of constructor";
	}

	/**
	 * Determine the number of elements in this sequence.
	 * @return
	 *   the number of elements in this sequence
	 **/ 
	public int size( )
	{
		assert wellFormed() : "invariant failed at start of size";
		// TODO: Implement this code.
		// size() should not modify anything, so we omit testing the invariant here
		return manyNodes;
	}

	// TODO: implement model field(s)
	// Do Activity 4 and put in the (private!) getters for them
	// if they are needed.
	private Node getCursor() {
		return precursor == null ? head : precursor.next;
	}

	/**
	 * The first element (if any) of this sequence is now current.
	 * @param - none
	 * @postcondition
	 *   The front element of this sequence (if any) is now the current element (but 
	 *   if this sequence has no elements at all, then there is no current 
	 *   element).
	 **/ 
	public void start( )
	{
		assert wellFormed() : "invariant failed at start of start";
		// TODO: Implement this code.
		precursor = null;
		assert wellFormed() : "invariant failed at end of start";
	}

	/**
	 * Accessor method to determine whether this sequence has a specified 
	 * current element (a HexTile or null) that can be retrieved with the 
	 * getCurrent method. This depends on the status of the cursor.
	 * @param - none
	 * @return
	 *   true (there is a current element) or false (there is no current element at the moment)
	 **/
	public boolean isCurrent( )
	{
		assert wellFormed() : "invariant failed at start of isCurrent";
		// TODO: Implement this code.
		if(precursor==null && head!=null) return true;
		return precursor != null && precursor.next != null;
	}

	/**
	 * Accessor method to get the current element of this sequence. 
	 * @param - none
	 * @precondition
	 *   isCurrent() returns true.
	 * @return
	 *   the current element of this sequence, possibly null
	 * @exception IllegalStateException
	 *   Indicates that there is no current element, so 
	 *   getCurrent may not be called.
	 **/
	public HexTile getCurrent( )
	{
		assert wellFormed() : "invariant failed at start of getCurrent";
		// TODO: Implement this code.
		// 如果沒有當前元素（即 cursor 為 null），拋出異常
		if (!isCurrent()) {
			throw new IllegalStateException("No current element");
		}
		if(precursor==null && head!=null) {
			return head.data;
		}
		return precursor.next.data;// 返回 cursor 指向的當前元素

	}

	/**
	 * Move forward, so that the next element is now the current element in
	 * this sequence.
	 * @param - none
	 * @precondition
	 *   isCurrent() returns true. 
	 * @postcondition
	 *   If the current element was already the end element of this sequence 
	 *   (with nothing after it), then there is no longer any current element. 
	 *   Otherwise, the new current element is the element immediately after the 
	 *   original current element.
	 * @exception IllegalStateException
	 *   If there was no current element, so 
	 *   advance may not be called (the precondition was false).
	 **/
	public void advance( )
	{
		assert wellFormed() : "invariant failed at start of advance";
		// TODO: Implement this code.
		// 檢查是否有當前元素
		if (!isCurrent()) {
			throw new IllegalStateException("No current element to advance");
		}

		if (precursor != null) {
			precursor = precursor.next;
		} else {
			precursor=head;
		}

		assert wellFormed() : "invariant failed at end of advance";
	}

	/**
	 * Remove the current element from this sequence.
	 * @param - none
	 * @precondition
	 *   isCurrent() returns true.
	 * @postcondition
	 *   The current element has been removed from this sequence, and the 
	 *   following element (if there is one) is now the new current element. 
	 *   If there was no following element, then there is now no current 
	 *   element.
	 * @exception IllegalStateException
	 *   Indicates that there is no current element, so 
	 *   removeCurrent may not be called. 
	 **/
	public void removeCurrent( )
	{
		assert wellFormed() : "invariant failed at start of removeCurrent";
		// TODO: Implement this code.
		if(!isCurrent()) {
			throw new IllegalStateException("no current element");
		}
		//1.check precursor
		if(precursor==null) {
			head=getCursor().next;
		}else {
			precursor.next=getCursor().next;

		}
		// 更新 size
		manyNodes--;
		assert wellFormed() : "invariant failed at end of removeCurrent";
	}

	/**
	 * Add a new element to this sequence, before the current element. 
	 * @param element
	 *   the new element that is being added, it is allowed to be null
	 * @postcondition
	 *   A new copy of the element has been added to this sequence. If there was
	 *   a current element, then the new element is placed before the current
	 *   element. If there was no current element, then the new element is placed
	 *   at the start of the sequence. In all cases, the new element becomes the
	 *   new current element of this sequence. 
	 **/
	public void addBefore(HexTile element)
	{
		assert wellFormed() : "invariant failed at start of addBefore";
		// TODO: Implement this code.
		manyNodes++;
		Node newNode=null;
		// 創建新的節點，並插入到當前元素之前
		if(precursor != null) {

			newNode = new Node(element, precursor);
			if(precursor.next==null) {
				newNode.next=head;
				head = newNode;
				precursor=null;
			}else {
				newNode.next=precursor.next;
				precursor.next= newNode;
			}
		} else {
			newNode = new Node(element, head);
			head = newNode;
		}
		assert wellFormed() : "invariant failed at end of addBefore";
	}

	/**
	 * Add a new element to this sequence, after the current element. 
	 * @param element
	 *   the new element that is being added, may be null
	 * @postcondition
	 *   A new copy of the element has been added to this sequence. If there was
	 *   a current element, then the new element is placed after the current
	 *   element. If there was no current element, then the new element is placed
	 *   at the end of the sequence. In all cases, the new element becomes the
	 *   new current element of this sequence. 
	 **/
	public void addAfter(HexTile element)
	{
		assert wellFormed() : "invariant failed at start of addAfter";
		// TODO: Implement this code.  No loops are needed!
		//percursor.next.next= getCursor.next
		Node newNode=null;
		if(isCurrent()) {
			newNode= new Node(element,getCursor().next);
			getCursor().next=newNode;
			if(precursor==null) {
				precursor=head;
			}else {
				precursor=getCursor();
			}
		}else {	

			if(head==null) {
				newNode = new Node(element,null);
				head=newNode;
			}else {
				newNode = new Node(element,precursor.next);
				precursor.next = newNode;
			}
		}
		manyNodes++;

		assert wellFormed() : "invariant failed at end of addAfter";
	}


	/**
	 * Place the contents of another sequence at the end of this sequence.
	 * @param addend
	 *   a sequence whose contents will be placed at the end of this sequence
	 * @precondition
	 *   The parameter, addend, is not null. 
	 * @postcondition
	 *   The elements from addend have been placed at the end of 
	 *   this sequence. The current element of this sequence if any,
	 *   remains unchanged.   The addend is unchanged.
	 * @exception NullPointerException
	 *   Indicates that addend is null. 
	 **/
	public void addAll(HexTileSeq addend)
	{
		assert wellFormed() : "invariant failed at start of addAll";
		// TODO: Implement this code.
		if (addend == null) {
			throw new NullPointerException("The addend sequence is null.");
		}

		HexTileSeq addEndCopy =addend.clone();
		boolean current = isCurrent();
		Node newNode =null;
		if(precursor==null) {//代表整個是空的
			newNode=head;
		}else {//要把這個newNode設在最後這樣addall才會加在最後面
			newNode=precursor;
		}
		if(newNode==null) {//要接住361的
			head=addEndCopy.head;
		}else {
			while(newNode.next!=null) {
				newNode=newNode.next;
			}
			if(addEndCopy.head!=null) {
				newNode.next=addEndCopy.head;
			}
		}
		if(!current && head!=null) {
			if(precursor==null) {
				precursor=head;
			}
			while(precursor.next!=null) {
				precursor=precursor.next;
			}
		}

		// 只需要確保 size 增加了 addend 的元素數量
		this.manyNodes += addend.manyNodes;

		assert wellFormed() : "invariant failed at end of addAll";
	}   

	/**
	 * Generate a copy of this sequence.
	 * @param - none
	 * @return
	 *   The return value is a copy of this sequence. Subsequent changes to the
	 *   copy will not affect the original, nor vice versa.
	 **/ 
	public HexTileSeq clone( )
	{  // Clone a HexTileSeq object.
		assert wellFormed() : "invariant failed at start of clone";
		HexTileSeq result;

		try
		{
			result = (HexTileSeq) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}

		// TODO: Implemented by student.
		// Now do the hard work of cloning the list.
		// See pp. 193-197, 228
		// Setting precursor correctly is tricky.
		if(head!=null) {
			result.head= new Node((head.data),null);
			Node copy = result.head;
			if(precursor==head) {
				result.precursor=result.head;
			}
			for(Node temp =head.next;temp!=null;temp=temp.next) {
				copy.next = new Node((temp.data),null);
				Node clone= new Node(temp.data,null);
				copy.next= clone;
				copy=copy.next;
				if (temp==precursor) {
					result.precursor= copy;

				}

			}
		}
		assert wellFormed() : "invariant failed at end of clone";
		assert result.wellFormed() : "invariant failed for clone";

		return result;
	}


	/**
	 * Class for internal testing.  Do not modify.
	 * Do not use in client/application code
	 */
	public static class Spy {
		/**
		 * A public versio of the data structure's internal node class.
		 * This class is only used for testing.
		 */
		public static class Node extends HexTileSeq.Node {
			/**
			 * Create a node with null data and null next fields.
			 */
			public Node() {
				this(null, null);
			}
			/**
			 * Create a node with the given values
			 * @param p data for new node, may be null
			 * @param n next for new node, may be null
			 */
			public Node(HexTile p, Node n) {
				super(null,null);
				this.data = p;
				this.next = n;
			}

			/** Return the data of a spy node. */
			public HexTile getData() {
				return data;
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
		 * Create a node for testing.
		 * @param p tile, may be null
		 * @param n next node, may be null
		 * @return newly ceated test node
		 */
		public Node newNode(HexTile p, Node n) {
			return new Node(p, n);
		}

		/**
		 * Change a node's next field
		 * @param n1 node to change, must not be null
		 * @param n2 node to point to, may be null
		 */
		public void setNext(Node n1, Node n2) {
			n1.next = n2;
		}

		/**
		 * Create an instance of the ADT with give data structure.
		 * This should only be used for testing.
		 * @param h head of linked list
		 * @param s size
		 * @param x current x
		 * @param y current y
		 * @return instance of HexTileSeq with the given field values.
		 */
		public HexTileSeq create(Node h, int s, Node p) {
			HexTileSeq result = new HexTileSeq(false);
			result.head = h;
			result.manyNodes = s;
			result.precursor = p;
			return result;
		}

		/**
		 * Get the value of the model field.
		 * @param s ADT to work om.
		 * @return the value of the model field.
		 */
		public Node getCursor(HexTileSeq s) {
			return (Node)s.getCursor();
		}

		/**
		 * Return whether the wellFormed routine returns true for the argument
		 * @param s seq to check.
		 * @return whether the invariant checker approves the data structure
		 */
		public boolean wellFormed(HexTileSeq s) {
			return s.wellFormed();
		}


	}
}

