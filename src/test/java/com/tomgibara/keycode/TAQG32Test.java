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
