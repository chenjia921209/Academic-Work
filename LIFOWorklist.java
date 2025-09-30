package edu.uwm.cs351.util;

import java.util.NoSuchElementException;
import java.util.Stack;

public class LIFOWorklist<E> implements Worklist<E> {
	
	// 使用 Stack 來儲存元素，Stack 遵循 LIFO 規則後進先出
    private Stack<E> stack = new Stack<>();


	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public E next() {
		// 如果 stack 是空的，拋出 NoSuchElementException
        if (stack.isEmpty()) {
            throw new NoSuchElementException("The stack is empty");
        }
        return stack.pop();// pop() 方法會移除並返回 stack 的頂部元素
	}

	@Override
	public void add(E element) {
        stack.push(element);// push() 方法會將元素加入 stack 的頂部
	}
}
