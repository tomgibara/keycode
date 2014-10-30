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

import static com.tomgibara.keycode.Encoder.CHARS_32;
import static com.tomgibara.keycode.Encoder.VALUES_32;

final class TAQG32 {

	private final static int A = 2; // x^1 in GF(32)

	/* exposed for testing */
	static int op(int x, int y) {
		return GF32.add(GF32.multiply(A, x), y);
	}
	
	// find y st. x*y = z
	private static int opInv(int x, int z) {
		for (int y = 0; y < 32; y++) {
			if (op(x,y) == z) return y;
		}
		throw new IllegalArgumentException();
	}

	static void dump() {
		StringBuilder sb = new StringBuilder();
		for (int y = -1; y < 32; y++) {
			for (int x = -1; x < 32; x++) {
				final char c;
				if (y == -1) {
					if (x == -1) {
						c = ' ';
					} else {
						c = CHARS_32[x];
					}
				} else if (x == -1) {
					c = CHARS_32[y];
				} else {
					c = CHARS_32[op(x, y)];
				}
				sb.append(c);
			}
			sb.append('\n');
		}
		sb.append('\n');
		System.out.println(sb);
	}
	
	static void checkAntiSymmetric() {
		for (int x = 0; x < 32; x++) {
			for (int y = 0; y < 32; y++) {
				if (op(x,y) == op(y,x) && x != y) throw new IllegalStateException("not TA for " + x + "," + y);
				for (int c = 0; c < 32; c++) {
					if (op(op(c,x),y) == op(op(c,y),x) && x != y) throw new IllegalStateException("not TA for " + x + "," + y + " at " + c);
				}
			}
		}
	}
	
	private static int accumulateChecksum(CharSequence cs, int start, int finish) {
		int x = 0;
		for (int i = start; i < finish; i++) {
			int c = cs.charAt(i);
			int v = VALUES_32[c];
			x = op(x, v);
		}
		return x;
	}
	
	static char compute(CharSequence cs, int start, int finish) {
		return CHARS_32[opInv(accumulateChecksum(cs, start, finish), 0)];
	}
	
	static boolean verify(CharSequence cs, int start, int finish) {
		return accumulateChecksum(cs, start, finish) == 0;
	}
	
}
