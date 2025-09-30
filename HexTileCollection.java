package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

// This is a Homework Assignment for CS 351 at UWM

/**
 * An array implementation of the Java Collection interface
 * We use java.util.AbstractCollection to implement most methods.
 * 
 * The data structure is a dynamic sized array.
 * The fields should be:
 * <dl>
 * <dt>data</dt> The data array.
 * <dt>manyItems</dt> Number of elements in the collection.
 * <dt>version</dt> Version number (used for fail-fast semantics)
 * </dl>
 * The class should define a private wellFormed() method
 * and perform assertion checks in each method.
 */
public class HexTileCollection extends AbstractCollection<HexTile> implements Iterable<HexTile>
// 繼承 AbstractCollection，這樣可以減少重複實作的方法
// extends {Something} implements {Something else}
{
	private static final int INITIAL_CAPACITY = 1;

	// TODO: Fields
	private HexTile[] data;  // 存放 HexTile 元素的陣列
	private int manyItems;   // 目前集合內的元素數量
	private int version;     // 用於 fail-fast 迭代器機制
/**迭代器 會在建立時，記錄當時的 version 值，並且在每次執行 next() 或 hasNext() 時檢查：
如果發現目前的 version 與當初記錄的 version 不同，則表示集合在迭代期間發生變更，
這時應該拋出 ConcurrentModificationException 來防止錯誤行為。
*/
	private HexTileCollection(boolean ignored) {} // DO NOT CHANGE THIS

	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		data = new HexTile[INITIAL_CAPACITY];//清理所有東西
		if(manyItems!= 0 ) {
		version++;
		manyItems=0;
		}

	}

	// The invariant:
	private boolean wellFormed() {
		//TODO: write the invariant checker
		// 0. data is not null   陣列不能為 null
	    if (data == null) return report("data array is null");
		// 1. manyItems is a possible count of elements given the capacity of the array
	    if (manyItems < 0 || manyItems > data.length) {
	        return report("manyItems is out of valid range");
	    }
		return true;
	}

	/**
	 * Initialize an empty HexTile collection with an initial
	 * capacity of INITIAL_CAPACITY. The {@link #add(HexTile)} method works
	 * efficiently (without needing more memory) until this capacity is reached.
	 * @postcondition
	 *   This HexTile collection is empty, has an initial
	 *   capacity of INITIAL_CAPACITY.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for an array with this many elements.
	 *   new HexTile[initialCapacity].
	 **/   
	public HexTileCollection()
	{
		// TODO: implement constructor
		// TODO: assert wellFormed() after body
		data = new HexTile[INITIAL_CAPACITY];  
		manyItems = 0;  
		version = 0;     
	}

	// TODO: override size (required!)

	@Override
	public int size() {// 1. the abstract class does not provide an implementation for the method. 
		//2. The inherited implementation needs to be added to in some way
		return manyItems;
	}
	
	private void ensureCapacity(int minimumCapacity)
	{
		if (data.length >= minimumCapacity) return;
		int newCapacity = Math.max(data.length*2, minimumCapacity);
		HexTile[] newData = new HexTile[newCapacity];
		for (int i=0; i < manyItems; i++) 
			newData[i] = data[i];

		data = newData;
	}
	
	// TODO: Another "required" override - iterator
	@Override // 1. the abstract class does not provide an implementation for the method. 
	
	public Iterator<HexTile> iterator() {
		MyIterator iterator = new MyIterator();
		// TODO Auto-generated method stub
		return iterator;
	}
	// TODO: You will need an "implementation" override
	public boolean add(HexTile e) {
		assert wellFormed():"invariant faled at start of add(Hextile e)";
		ensureCapacity(manyItems+1);
		
		data[manyItems] = e;
		manyItems++;
		version++;
		assert wellFormed():"invariant failed at the of od add(Hextile e)";
		return true;
		
	}

	// TODO: You will find an efficiency override necessary

	private class MyIterator implements Iterator<HexTile>
	// TODO: implements {Something}
	{

		int colVersion, currentIndex;
		boolean isCurrent;

		MyIterator(boolean ignored) {} // DO NOT CHANGE THIS

		private boolean wellFormed() {


			// 0. The outer invariant holds
			//		NB: To access the parent HexTileCollection of this iterator, use "HexTileCollection.this"
			//			e.g. HexTileCollection.this.getName()
			if(!HexTileCollection.this.wellFormed()) {
				return false; // 因為他會去跑上面的所以上面就九report所以就不用report 不然就會重複丟了 只需要最原始的
			};
			// TODO
			// Invariant for recommended fields:
			// NB: Don't check 1,2 unless the version matches.
			if(version != colVersion) {
				return true;
			}
			
			// 1. currentIndex is between -1 (inclusive) and manyItems (exclusive)
			if(currentIndex <-1 || currentIndex >=manyItems) {
				return report("currentIndex is between -1 (inclusive) and manyItems (exclusive");
			}
			// TODO
			// 2. currentIndex is equal to -1 only if isCurrent is false
			if(currentIndex ==-1 && isCurrent) {
				return report ("current is equal to -1 only if isCurrent is false");
			}
			// TODO

			return true;
		}	

		/**
		 * Instantiates a new MyIterator.
		 */
		public MyIterator() {
			// TODO
			 colVersion = version;
			 currentIndex = -1;
			isCurrent =false;
			assert wellFormed() : "invariant fails in iterator constructor";
			
		}

		@Override
		public boolean hasNext() {
			assert wellFormed():"invariant faled at start of hasNext"; //如果有改變才要在結尾也加上assert
			//exception
			if(colVersion != version) { //假設iterator的當前數量是12 version是一個被編輯過的會被改變
				//所以要確保他們是不是一樣 這樣代表有沒有被改變過
				throw new ConcurrentModificationException();
			}
			// TODO Auto-generated method stub
			return currentIndex < manyItems-1;
			 
		}

		@Override
		public HexTile next() {
			// TODO Auto-generated method stub
			assert wellFormed():"invariant faled at start of next";
			if(hasNext()) {
				isCurrent = true;
				currentIndex++;
				return data[currentIndex];
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			assert wellFormed():"invariant faled at start of remove";
			// TODO Auto-generated method stub
			if(colVersion !=version) {
				throw new ConcurrentModificationException();
			}
			if(isCurrent) {
				for(int i = currentIndex; i<manyItems-1;i++) {
					data[i]= data[i+1];
				}
				manyItems--;
				version++; //version是記錄改變幾次
				colVersion=version;
				currentIndex--;
				isCurrent=false;
			}else {
			
			throw new IllegalStateException();
			}
		}

		// TODO: Implement required methods
	}

	/**
	 * Generate a copy of this HexTile collection.
	 * @param - none
	 * @return
	 *   The return value is a copy of this HexTile collection. Subsequent changes to the
	 *   copy will not affect the original, nor vice versa.
	 * @exception OutOfMemoryError
	 *   Indicates insufficient memory for creating the clone.
	 **/ 
	@Override // decorate
	public HexTileCollection clone( ) { 
		assert wellFormed() : "invariant failed at start of clone";
		HexTileCollection result;

		try
		{
			result = (HexTileCollection) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{
			// This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}

		// all that is needed is to clone the data array.
		// (Exercise: Why is this needed?)
		result.data = data.clone( );

		assert wellFormed() : "invariant failed at end of clone";
		assert result.wellFormed() : "invariant on result failed at end of clone";
		return result;
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
		 * Create a debugging instance of the main class
		 * with a particular data structure.
		 * @param a static array to use
		 * @param m size to use
		 * @param v current version
		 * @return a new instance with the given data structure
		 */
		public HexTileCollection newInstance(HexTile[] a, int m, int v) {
			HexTileCollection result = new HexTileCollection(false);
			result.data = a;
			result.manyItems = m;
			result.version = v;
			return result;
		}

		/**
		 * Return an iterator for testing purposes.
		 * @param bc main class instance to use
		 * @param c current index of iterator
		 * @param i the value of 'isCurrent'
		 * @param v the value of colVersion
		 * @return iterator with this data structure
		 */
		public Iterator<HexTile> newIterator(HexTileCollection bc, int c, boolean i, int v) {
			MyIterator result = bc.new MyIterator(false);
			result.currentIndex = c;
			result.isCurrent = i;
			result.colVersion = v;
			return result;
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param bs instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(HexTileCollection bs) {
			return bs.wellFormed();
		}

		/**
		 * Return whether debugging instance meets the 
		 * requirements on the invariant.
		 * @param i instance of to use, must not be null
		 * @return whether it passes the check
		 */
		public boolean wellFormed(Iterator<HexTile> i) {
			return ((MyIterator)i).wellFormed();
		}
	}


}
