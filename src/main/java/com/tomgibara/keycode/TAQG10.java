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


final class TAQG10 {

	private static final int[] TABLE = {
		0, 3, 1, 7, 5, 9, 8, 6, 4, 2,
		7, 0, 9, 2, 1, 5, 4, 8, 6, 3,
		4, 2, 0, 6, 8, 7, 1, 3, 5, 9,
		1, 7, 5, 0, 9, 8, 3, 4, 2, 6,
		6, 1, 2, 3, 0, 4, 5, 9, 7, 8,
		3, 6, 7, 4, 2, 0, 9, 5, 8, 1,
		5, 8, 6, 9, 7, 2, 0, 1, 3, 4,
		8, 9, 4, 5, 3, 6, 2, 0, 1, 7,
		9, 4, 3, 8, 6, 1, 7, 2, 0, 5,
		2, 5, 8, 1, 4, 3, 6, 7, 9, 0
	};

	private static int accumulateChecksum(CharSequence cs, int start, int finish) {
		int x = 0;
		for (int i = start; i < finish; i++) {
			int c = cs.charAt(i);
			int v = c - 48;
			x = TABLE[x * 10 + v];
		}
		return x;
	}
	
	static char compute(CharSequence cs, int start, int finish) {
		return (char) (accumulateChecksum(cs, start, finish) + 48);
	}
	
	static boolean verify(CharSequence cs, int start, int finish) {
		return accumulateChecksum(cs, start, finish) == 0;
	}

}
