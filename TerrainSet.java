package edu.uwm.cs351;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An immutable set of terrains.
 */
public class TerrainSet extends AbstractSet<Terrain> {
	private static Terrain[] allTerrains = Terrain.values();

	private final int bits;
	private final int size;

	/**
	 * Create an immutable set of terrains with the given terrains
	 * @param ts terrains to use, must not include null.  Duplicates are ignored.
	 */
	public TerrainSet(Terrain... ts) {
		// TODO
		int temBit = 0;// 暫存用的 bits，從 0 開始（空集合）
		int countSize = 0;     // 統計有幾個地形被加入進來
		for (Terrain t : ts) {
			if (t == null) {
				throw new NullPointerException("Terrain can not be null");
			}
			int placeOrd = 1 << t.ordinal(); //ordinal() 會回傳該地形在 enum 中的位置
			//用一個整數的「位元」來表示每個地形是否存在於集合中
			if ((temBit & placeOrd) == 0) { 
				temBit |= placeOrd;// 加入該地形：將對應位元設為 1
				countSize++;     // 增加地形數量
			}
		}
		this.bits = temBit;
		this.size = countSize;
	}
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Terrain)) { // 如果不是 Terrain 類型，直接回傳 false
			return false;
		}

		Terrain t = (Terrain) o;
		int placeOrd = 1 << t.ordinal();
		return (bits & placeOrd) != 0; // 如果該位元為 1，表示存在
	}
	
	@Override
	public Iterator<Terrain> iterator() {
		return new TerrainIterator();
	}

	private class TerrainIterator implements Iterator<Terrain> {
		private int currentBit = 1; //初始為最右邊的 bit（2^0 = 1）
		private int remainingBits = bits;
		private int currentOrdinal = 0;// 對應 Terrain enum 的 ordinal 值

		@Override
		public boolean hasNext() {
			return remainingBits != 0;
		}

		@Override
		public Terrain next() {
			if (!hasNext()) throw new NoSuchElementException();

			// 移動 currentBit 直到對應位元是 1
			while ((remainingBits & currentBit) == 0) {
				currentBit <<= 1;// 把 currentBit 往左移一位（等同於乘 2）
				currentOrdinal++;
			}

			// 找到對應的 Terrain
			Terrain result = Terrain.values()[currentOrdinal];

			// 清除這個位元，準備下一次
			remainingBits &= ~currentBit; // 將這個已經處理過的 bit 清掉（將該 bit 設為 0）
			// 準備處理下一個 bit
			currentBit <<= 1;
			currentOrdinal++;

			return result;
		}
	}



}


