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

import junit.framework.TestCase;

public class TAQG32Test extends TestCase {

	public void testBest() {
		for (int a = 1; a < 32; a++) {
			for (int b = 1; b < 32; b++) {
				if (a == 1 || a == b) continue;
				fail: for (int c = 0; c < 32; c++) {
					int m = -1;
					for (int x = 0; x < 31; x++) {
						int y = GF32.add(x, c);
						int z = GF32.add(GF32.multiply(a, y), GF32.multiply(b, y));
						if (m == -1) {
							m = z;
						} else if (z != m) {
							continue fail;
						}
					}
					System.out.println(a + " " + b + " " + c);
				}
			}
		}
		System.out.println("DONE");
	}
	
}
