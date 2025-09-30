package edu.uwm.cs351.util;

import java.util.ArrayList;
import java.util.List;

public class Primes {

	/**
	 * Compute and return the next prime larger than n.
	 * @param n integer to exceed
	 * @return next prime number.
	 */
	public static int nextPrime(int n) {
		// we use a very simple algorithm
		for (;;) {
			if (isPrime(++n)) return n;
		}
	}
	
	/** Compute and return the next prime number greater than n
	 * where (n-2) is also a prime.
	 * @param n integer to exceed
	 * @return next upper twin prime number.
	 */
	public static int nextTwinPrime(int n) {
		if (n < 5) return 5;
		// brute force
		--n; // so we check (n-1,n+1)
		n |= 1; // force odd
		for (;;) {
			if (isPrime(n)) {
				if (isPrime(n+2)) return n+2;
				else n+=4;
			} else n+=2;
		}
	}
	
	public static boolean isPrime(int n) {
		if (n < 2) return false;
		int bound = (int)Math.sqrt(n);
		while (bound > lastSmall()) {
			// System.out.println("Larger than " + lastSmall());
			smallPrimes.add(nextPrime(lastSmall()));
		}
		for (int prime : smallPrimes) {
			if (n % prime == 0) return false;
			if (prime >= bound) return true;
		}
		throw new AssertionError("can't get here: " + n);
	}

	private static Integer lastSmall() {
		return smallPrimes.get(smallPrimes.size()-1);
	}
	
	private static List<Integer> smallPrimes = new ArrayList<Integer>();
	static {
		smallPrimes.add(2);
	}
	
	public static void main(String[] args) {
		int p = nextPrime(10000);
		System.out.println("First prime over 10000 is " + p);
		for (p=5; p < 1000; p = nextTwinPrime(p)) {
			System.out.println("Twin primes: " + (p-2) + ", " + p);
		}
	}
}
