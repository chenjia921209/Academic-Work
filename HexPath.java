package edu.uwm.cs351;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * A class representing a path through hex space.
 * It started with a hex coordinate and then moves from one hexagon to an adjacent hexagon, 
 * one hex direction at a time.  The starting point cannot be changed, 
 * but directions can be added or removed in LIFO fashion.
 * The iterator lists all hexagons traversed, starting with the starting one.
 * The number of hex coordinates iterated through is one more than the length.
 * The iterator is not mutable (does not support remove).
 */
public class HexPath implements Iterable<HexCoordinate>, Cloneable {
	// TODO: Data structure
	private List<HexDirection> directions;
	private HexCoordinate start;
	private HexCoordinate end;
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private HexPath(boolean ignored) {} // do not change this constructor

	public HexPath(HexCoordinate start) {
		if(start == null) throw new NullPointerException("start==null");
		this.start=start;
		directions = new ArrayList<>();
		end = start;
	}

	private boolean wellFormed() {

		// TODO
		// check that all fields are non-null and that there
		// are no null direction and that the end is consistent
		if (directions == null || start == null || end == null) {
			return report("all fields are non-null ");
		}
		HexCoordinate temp = start;
		for (HexDirection d : directions) {
			if (d == null) return report("Null direction detected");
			temp = d.move(temp); //direction move coordinate
		}
		if (!temp.equals(end)) return report("End coordinate inconsistent");
		return true;
	}

	// TODO:
	// First stub everything needed to fix compiler errors
	// Then follow instructions from the homework description
	// for each of the methods you need to write
	@Override
	public Iterator<HexCoordinate> iterator() {
		return new Iterator<HexCoordinate>() {
			private HexCoordinate current = start;
			private int index = 0;

			//新增directions 的複製，確保迭代不受 HexPath 變更影響
			private final List<HexDirection> snapshot = new ArrayList<>(directions);
			@Override
			public boolean hasNext() {
				if (index == 0) {
					return true;
				}
				return index <= snapshot.size();
			}

			@Override
			public HexCoordinate next() {
				if (!hasNext()) {
					throw new NoSuchElementException("there is no next element");
				}
				// 保存當前的座標，這是這次迭代要返回的值
				HexCoordinate result = current;

				// 只有當還有方向可用時，才移動到下一個座標
				if (index < snapshot.size()) {
					current = snapshot.get(index).move(current);
				}
				index++;

				return result;
			}
		};
	}

	public void add(HexDirection hexDirection) {
		assert wellFormed() : "invariant failed at start of add";
		end = hexDirection.move(end);
		directions.add(hexDirection);
		assert wellFormed() : "invariant failed at end of add";

	}

	public boolean isEmpty() {
		assert wellFormed() : "invariant failed at start of isEmpty";
		return directions.size()==0;
	}

	public List<HexDirection> getDirections() {
		List<HexDirection> copy =new ArrayList<HexDirection>(directions);
		return copy;
	}

	public int length() {
		assert wellFormed() : "invariant failed at start of length";
		return directions.size();
	}

	public HexCoordinate getStart() {
		assert wellFormed() : "invariant failed at start of start";
		// TODO Auto-generated method stub
		return start;
		//		directions[]=start;
	}

	public HexDirection removeLast() {
		assert wellFormed() : "invariant failed at start of removeLast";
		if (directions.isEmpty()) throw new IllegalStateException("No directions to remove");
		HexDirection last = directions.remove(directions.size() - 1);
			end = last.reverse().move(end);
		assert wellFormed() : "invariant failed at end of removeLast";
		return last;


	}

	public HexCoordinate getEnd() {
		assert wellFormed() : "invariant failed at start of getEnd";
		// TODO Auto-generated method stub
		return end;
	}

	public void add(String string) {
		assert wellFormed() : "invariant failed at start of add";
		if(string==null) throw new NullPointerException("string is null");
		for(int i=0;i<string.length();i++) {
			char character =string.charAt(i);
			HexDirection addCharacter =HexDirection.fromNickName(Character.toString(character));
			add(addCharacter);

		}
		assert wellFormed() : "invariant failed at end of add";

	}

	public HexDirection last() {
		assert wellFormed() : "invariant failed at start of last";
		if(isEmpty()) throw new IllegalStateException("the arrayList is empty");
		HexDirection last = directions.get(directions.size()-1);
		return last;
	}

	public String getDirectionsAsString() {
		// TODO Auto-generated method stub
		StringBuilder string = new StringBuilder();
		for (HexDirection d : directions) {
			string.append(d.getNickName()); 
		}
		return string.toString();
	}

	@Override
	public HexPath clone()
	{  // Clone a HexTileSeq object.
		assert wellFormed() : "invariant failed at start of clone";
		HexPath result;

		try
		{
			result = (HexPath) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}

		result.directions = new ArrayList<>(this.directions);
		//		result.end=end;

		assert wellFormed() : "invariant failed at end of clone";
		assert result.wellFormed() : "invariant failed for clone";

		return result;
	}


	/** This class is used for internal testing.
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


		// TODO: Implement newInstance and wellFormed for Spy

		public HexPath newInstance(HexCoordinate start, List<HexDirection> dir, HexCoordinate end) {
			// TODO Auto-generated method stub

			HexPath path = new HexPath(false);
			path.start=start;
			path.directions = dir;
			path.end = end;
			return path;
		}

		public boolean wellFormed(HexPath p) {
			// TODO Auto-generated method stub
			return p.wellFormed();
		}
	}


}