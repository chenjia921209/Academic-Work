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
 *
 * @note
 *   (1) The capacity of a sequence can change after it's created, but
 *   the maximum capacity is limited by the amount of free memory on the 
 *   machine. The constructor, addAfter, 
 *   addBefore, clone, 
 *   and concatenation will result in an
 *   OutOfMemoryError when free memory is exhausted.
 *   <p>
 *   (2) A sequence's capacity cannot exceed the maximum integer 2,147,483,647
 *   (Integer.MAX_VALUE). Any attempt to create a larger capacity
 *   results in a failure due to an arithmetic overflow. 
 *   
 *   NB: Neither of these conditions require any work for the implementors (students).
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
	private HexTile[ ] data;
	private int manyItems;
	private int currentIndex; 
	private static int INITIAL_CAPACITY = 1;
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}
	private boolean wellFormed() {
		// Check the invariant.
		// 1. data is never null 確保data陣列已經初始化不會出現null
		if (data == null) return report("data is null"); // test the NEGATION of the condition
		// 2. The data array is at least as long as the number of items
		//    claimed by the sequence. 確保data陣列的長度至少等於manyItems這樣才不會超過範圍
		// TODO
		if(manyItems >data.length) //要看的是元素的數量不應該超過陣列的長度
			return report("The data array needs to be as long as the number of items");
		// 3. currentIndex is never negative and never more than the number of
		//    items claimed by the sequence.
		// TODO
		if(currentIndex <0 || currentIndex> manyItems) 
			return report("currentIndex is never negative and never more than the number of items claimed by the sequence.");
		// If no problems discovered, return true
		return true;
	}
	// This is only for testing the invariant.  Do not change!
	private HexTileSeq(boolean testInvariant) { }
	
	/**
	 * Initialize an empty sequence with an initial capacity of INITIAL_CAPACITY. 初始化一個空序列，其初始容量為 INITIAL_CAPACITY。
	 * The addAfter and addBefore methods work
	 * efficiently (without needing more memory) until this capacity is reached.高效率（不需要更多記憶體）直到達到該容量。
	 * @param - none
	 * @postcondition
	 *   This sequence is empty and has an initial capacity of INITIAL_CAPACITY
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for initial array.
	 **/   
	public HexTileSeq( ) //defult constructor市調用另一個被初始過的東西過來初始化新的物件 結束時確保assert狀態符合狀態
	{
		// NB: NEVER assert the invariant at the START of the constructor.
		// (Why not?  Think about it.) 因為在構造函數執行的時候，類別狀態還沒有被初始化，因此如果在執行之前揪去進行檢查，很有可能會得到錯誤的答案
		// TODO: Implement this code.
		this(INITIAL_CAPACITY); 
		assert wellFormed() : "Invariant false at end of constructor";
	}
	/**
	 * Initialize an empty sequence with a specified initial capacity. Note that
	 * the addAfter and addBefore methods work
	 * efficiently (without needing more memory) until this capacity is reached.
	 * @param initialCapacity
	 *   the initial capacity of this sequence
	 * @precondition
	 *   initialCapacity is non-negative.
	 * @postcondition
	 *   This sequence is empty and has the given initial capacity.
	 * @exception IllegalArgumentException
	 *   Indicates that initialCapacity is negative.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for an array with this many elements.
	 *   new HexTile[initialCapacity].
	 **/   
	public HexTileSeq(int initialCapacity) //constructor
	{
		if(initialCapacity<0) { //確認他不會是負數
		// TODO: Implement this code.
        throw new IllegalArgumentException("Capacity cannot be negative.");
		}
		data = new HexTile[initialCapacity]; // 初始化陣列
		manyItems=0;
		currentIndex= manyItems;
		assert wellFormed() : "Invariant false at end of constructor";
	}
	/**
	 * Determine the number of elements in this sequence.
	 * @param - none
	 * @return
	 *   the number of elements in this sequence
	 **/ 
	public int size( )
	{
		assert wellFormed() : "invariant failed at start of size";
		// TODO: Implement this code.
		// size() should not modify anything, so we omit testing the invariant here
		return manyItems;
		
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
	    if (manyItems > 0) {
	        currentIndex = 0;  // 指向第一個元素
	    } else {
	        currentIndex = manyItems;  // 沒有當前元素
	    }
		    
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
	public boolean isCurrent( ) //檢查序列中是否還有當前元素current element
	{
		assert wellFormed() : "invariant failed at start of isCurrent";
		// TODO: Implement this code.
// 檢查 currentIndex 是否在有效範圍內
		
		// if((currentIndex <= 0 && manyItems == 0)|| currentIndex >= manyItems)
		if(currentIndex == manyItems) {
			//如果序列是空的 (manyItems == 0)，那 currentIndex 也應該是無效的 (<= 0)。
			//如果 currentIndex 超出了有效範圍 (>= manyItems)，那就沒有 current 元素。
				return false;
		}else {
			return true;
		}
		
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
	    // 如果沒有當前元素，拋出異常
	    if (!isCurrent()) {
	        throw new IllegalStateException("No current element to return.");
	    }
	    
	    // 回傳當前的元素
	    return data[currentIndex];
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
		if (!isCurrent()) {
	        throw new IllegalStateException("Cannot advance: no current element.");
	    } else {
	    	if(currentIndex == manyItems-1) {
	    		currentIndex = manyItems;
	    	
	    } else {
		currentIndex++; //要改變array[]裡面的數字 因為要移動 所以要＋＋ 
	    }
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
		// You will need to shift elements in the array.
		 // 如果沒有當前元素，拋出異常
	    if (!isCurrent()) {
	        throw new IllegalStateException("No current element to remove.");
	    }

	    // 將當前元素刪除
	    // 從 currentIndex 位置開始，將後面的元素向前移動
	    for (int i = currentIndex; i < manyItems-1; i++) {
	        data[i] = data[i+1];  // 將元素移動到前一個位置
	    }

	    // 更新 manyItems，因為我們刪除了一個元素
	    manyItems--;
    	data[manyItems]=null;

		assert wellFormed() : "invariant failed at end of removeCurrent";
	}
	/**
	 * Add a new element to this sequence, before the current element. 
	 * If the new element would take this sequence beyond its current capacity,
	 * 在當前元素 (current element) 之前新增一個元素到這個序列 (sequence)。
	 * * 如果新增的元素超過了目前的容量，則在新增之前會擴增容量。
	 * then the capacity is increased before adding the new element.
	 * @param element
	 *   the new element that is being added, it is allowed to be null
	 * @postcondition
	 *   A new copy of the element has been added to this sequence. If there was
	 *   a current element, then the new element is placed before the current
	 *   element. If there was no current element, then the new element is placed
	 *   at the start of the sequence. In all cases, the new element becomes the
	 *   new current element of this sequence. 
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for increasing the sequence's capacity.
	 * @note
	 *   An attempt to increase the capacity beyond
	 *   Integer.MAX_VALUE will cause the sequence to fail with an
	 *   arithmetic overflow.
	 **/
	public void addBefore(HexTile element)
	{
		assert wellFormed() : "invariant failed at start of addBefore";
		// TODO: Implement this code.
	    if (!isCurrent()) { 
	       ensureCapacity(manyItems + 1);  // 確保有足夠的容量
	        for (int i = manyItems; i> 0; i--) {  // 從最後一個元素開始，將所有元素向後移動一位
	            data[i] = data[i - 1];
	        }
	        currentIndex = 0;
	        data[currentIndex] = element;  // 把新元素設為當前元素
	    }else {
		ensureCapacity(manyItems + 1);
	    // 將 `element` 插入 `currentIndex` 之前的位置
	    for (int i = manyItems; i > currentIndex; i--) {
	        data[i] = data[i - 1];
	    }
	    
	    data[currentIndex] = element;
	    }
	    manyItems++;
	    
		assert wellFormed() : "invariant failed at end of addBefore";
	}
	/**
	 * Add a new element to this sequence, after the current element. 
	 * If the new element would take this sequence beyond its current capacity,
	 * then the capacity is increased before adding the new element.
	 * @param element
	 *   the new element that is being added, may be null
	 * @postcondition
	 *   A new copy of the element has been added to this sequence. If there was
	 *   a current element, then the new element is placed after the current
	 *   element. If there was no current element, then the new element is placed
	 *   at the end of the sequence. In all cases, the new element becomes the
	 *   new current element of this sequence. 
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for increasing the sequence's capacity.
	 * @note
	 *   An attempt to increase the capacity beyond
	 *   Integer.MAX_VALUE will cause the sequence to fail with an
	 *   arithmetic overflow.
	 **/
	public void addAfter(HexTile element)
	{
		assert wellFormed() : "invariant failed at start of addAfter";
		// TODO: Implement this code.
		  // 1. 如果容量已滿，擴展陣列容量
	    if (manyItems == data.length) {
	        ensureCapacity((data.length == 0) ? 1 : data.length * 2);
	    }

	    // 2. 決定插入位置
	    if (isCurrent()) {
	        // (a) 如果有 current element，則在 currentIndex 之後插入
	        for (int i = manyItems; i > currentIndex; i--) {
	            data[i] = data[i - 1]; // 元素向右移動
	        }
	        data[currentIndex + 1] = element; // 插入新元素
	        currentIndex++; // 讓新元素變成當前元素
	    } else {
	        // (b) 如果沒有 current element，則在序列末尾插入
	        data[manyItems] = element;
	        currentIndex = manyItems; // 新增的元素變成 current element
	    }

	    manyItems++; // 更新數量
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
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory to increase the size of this sequence.
	 * @note
	 *   An attempt to increase the capacity beyond
	 *   Integer.MAX_VALUE will cause an arithmetic overflow
	 *   that will cause the sequence to fail.
	 **/
	public void addAll(HexTileSeq addend)
	{
		assert wellFormed() : "invariant failed at start of addAll";
		// TODO: Implement this code.
		if (addend == null) {
	        throw new NullPointerException("addend cannot be null");
	    }

	    // 確保當前陣列有足夠的空間來存放 addend 的所有元素
	    ensureCapacity(manyItems + addend.manyItems);

	    // 把 addend 的元素逐一複製到當前序列的尾端
	    if(!isCurrent()) {
	    	
	    	currentIndex = manyItems + addend.manyItems;
	    }
	    int r = manyItems;
	    for (int i = 0; i < addend.manyItems; i++) {
	    	
	        data[r] = addend.data[i];
	        r++;
	    }
	    

	    // 更新 manyItems 的數量
	    manyItems += addend.manyItems;


		assert wellFormed() : "invariant failed at end of addAll";
	}   
	/**
	 * Change the current capacity of this sequence.
	 * @param minimumCapacity
	 *   the new capacity for this sequence
	 * @postcondition
	 *   This sequence's capacity has been changed to at least minimumCapacity.
	 *   If the capacity was already at or greater than minimumCapacity,
	 *   then the capacity is left unchanged.
	 *   If the size is changed, it must be at least twice as big as before.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for: new array of minimumCapacity elements.
	 **/
	private void ensureCapacity(int minimumCapacity)
	{
		// TODO: Implement this code.
		// This is a private method: don't check invariants
		// If there is enough capacity already, do nothing
		// Otherwise, if double the current capacity is enough, use that
		// Otherwise, use minimumCapacity as the new capacity
		if (data.length < minimumCapacity){
			int newSize = data.length*2;
		    if (newSize < minimumCapacity) newSize = minimumCapacity;
		    HexTile[] newArray = new HexTile[newSize];
		    for (int i=0; i < manyItems; ++i) {
		        newArray[i] = data[i]; 
		      }
		    data = newArray;
		}
	}
	/**
	 * Generate a copy of this sequence.
	 * @param - none
	 * @return
	 *   The return value is a copy of this sequence. Subsequent changes to the
	 *   copy will not affect the original, nor vice versa.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for creating the clone.
	 **/ 
	public HexTileSeq clone( )
	{  // Clone a HexTileSeq object.
		assert wellFormed() : "invariant failed at start of clone";
		HexTileSeq answer;
		try
		{
			answer = (HexTileSeq) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}
		// TODO: clone the data array
		 // 深拷貝 data 陣列
	    answer.data = new HexTile[data.length];
	    for (int i = 0; i < data.length; i++) {
	        answer.data[i] = data[i]; // 或者根據需要進行深拷貝
	    }
		assert wellFormed() : "invariant failed at end of clone";
		assert answer.wellFormed() : "invariant failed for clone";
		
		return answer;
	}
	
	/** This class is used for internal testing.
	 * Do not modify it, even if Eclipse suggests that you do so.
	 * It will make it impossible to satsfy internal tests.
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
		 * Create a debugging instance of a HexTileSeq
		 * with a particular data structure.
		 * @param a static array to use
		 * @param m size to use
		 * @param c current index for cursor
		 * @return a new instance of a HexTileSeq with the given data structure
		 */
		public HexTileSeq newInstance(HexTile[] a, int m, int c) {
			HexTileSeq result = new HexTileSeq(false);
			result.data = a;
			result.manyItems = m;
			result.currentIndex = c;
			return result;
		}
		
		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param hs instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(HexTileSeq hs) {
			return hs.wellFormed();
		}
		/**
		 * Ensure the capacity of a sequence and return a copy of the resulting array.
		 * @param hs sequence to check
		 * @param cap capacity desired
		 * @return copy of the resulting array
		 */
		public HexTile[] ensureCapacity(HexTileSeq hs, int cap) {
			hs.ensureCapacity(cap);
			return hs.data.clone();
		}
	}
}