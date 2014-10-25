package com.tomgibara.keycode;

/* modulus x^5 + x^2 + 1 */

/* values operate as LSBs */
final class GF32 {

	private static final int mod = 0x25;
	private static final int[] pow = new int[31];
	private static final int[] log = new int[32];

	static {
		int x = 1;
		for (int i = 0; i < 31; i++) {
			pow[i] = x;
			log[x] = i;
			x <<= 1;
			if ((x & 0x20) != 0) {
				x ^= mod;
			}
		}
	}
	
	public static int add(int a, int b) {
		return a ^ b;
	}
	
	public static int subtract(int a, int b) {
		return a ^ b;
	}
	
	public static int multiply(int a, int b) {
		if (a == 0 || b == 0) return 0;
		int logA = log[a];
		int logB = log[b];
		return pow[(logA + logB) % 31];
	}
	
	public static int divide(int a, int b) {
		if (a == 0) return 0;
		if (b == 0) throw new IllegalArgumentException("zero b");
		int logA = log[a];
		int logB = log[b];
		return pow[(logA - logB) % 31];
	}
	
}
