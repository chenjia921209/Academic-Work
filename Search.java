package edu.uwm.cs351;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import edu.uwm.cs351.HexBoard.HexPiece;
import edu.uwm.cs351.util.Worklist;

/**
 * Class to find paths across hex tile boards.
 */
public class Search {
	private final TerrainSet passable;
	private final Worklist<HexPiece> worklist;
	private final Map<HexCoordinate, HexDirection> visited = new HashMap<>();
	
	/**
	 * Create a searcher that uses the given worklist.
	 * @param ts terrains which we can pass through
	 * @param w worklist to use to find path.
	 */
	public Search(TerrainSet ts, Worklist<HexPiece> w) {
		passable = ts;
		worklist = w;
	}
	
	private void clear() {
		// TODO: empty worklist and visited set	
		visited.clear();
		while (worklist.hasNext()) {
			worklist.next(); // 清空 worklist 中現有元素
		}
	}
	
	/**
	 * Find a path through a hex board.
	 * @param from coordinate to start from (must not be null)
	 * @param to coordinate to reach (must not be null)
	 * @param b hex board to traverse
	 * @return a search state including a possible path, or null if no path is found.
	 */
	public HexPath find(HexCoordinate from, HexCoordinate to, HexBoard b) {
		clear();
		HexPiece start = b.get(from);
		if (start == null || !passable.contains(start.getTerrain())) return null; //起點無效或不在可通行的地形範圍內
		visited.put(from, null); // start of path
		// TODO: Use worklist to find path to "to"
		worklist.add(start);

		while (worklist.hasNext()) {
			HexPiece current = worklist.next();
			HexCoordinate loc = current.getLocation(); //工作列表中取出下一個棋子 (HexPiece)，並取得它的位置 loc

			if (loc.equals(to)) {
				return makePath(to);//當前位置等於終點 to，則呼叫 makePath(to) 方法來建立從起點到終點的路徑並返回
			}

			for (HexDirection d : HexDirection.values()) {
				HexPiece neighbor = current.move(d);
				if (neighbor == null) continue;

				HexCoordinate nloc = neighbor.getLocation();
				if (!passable.contains(neighbor.getTerrain())) continue;
				if (visited.containsKey(nloc)) continue;//檢查該位置是否已經被訪問過

				worklist.add(neighbor);
				visited.put(nloc, d.reverse()); // 記錄回到目前位置的方向
			}
		}

		return null; // 找不到路

	}
	
	/**
	 * Create a hex path starting at the initial coordinate to the coordinate given.
	 * This method should only be called if the {@link visited} map contains all
	 * the information to trace back to the initial hex coordinate.
	 * @param to coordinate to make the path to
	 * @return hex path, never null
	 */
	private HexPath makePath(HexCoordinate to) {
		// 第一步：往回建構「反過來」的路徑
		HexPath reversed = new HexPath(to);
		HexCoordinate current = to;

		while (visited.get(current) != null) {
			HexDirection dir = visited.get(current); // 我們是怎麼走到 current 的
			HexDirection back = dir.reverse();       // 所以我們回去時是 dir.reverse()
			reversed.add(back);                      // 加入反方向
			current = dir.move(current);             // 回到前一格
		}

		// 第二步：把 reversed 裡面的方向反過來，加到正確順序的 HexPath
		HexPath result = new HexPath(current);
		while (!reversed.isEmpty()) {
			result.add(reversed.removeLast());
		}

		return result; // TODO: Write this recursive helper method
	}
	public void markVisited(Graphics g) {
		// TODO
		int width = HexTile.WIDTH /4;
		
		for(HexCoordinate coordinate : visited.keySet()) {
			Point p = coordinate.toPoint(HexTile.WIDTH);
			g.drawLine(p.x-width, p.y-width, p.x+width, p.y+width);
			g.drawLine(p.x+width, p.y-width, p.x-width, p.y+width);
		}
	}
}
