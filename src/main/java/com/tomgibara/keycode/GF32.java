/*
 *   Copyright 2014 Tom Gibara
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
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
