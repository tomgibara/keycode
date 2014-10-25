package com.tomgibara.keycode;

import static com.tomgibara.keycode.Encoder.VALUES;

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
			int v = VALUES[c];
			x = TABLE[x * 10 + v];
		}
		return x;
	}
	
	static char compute(CharSequence cs, int start, int finish) {
		return Encoder.CHARS[ accumulateChecksum(cs, start, finish) ];
	}
	
	static boolean verify(CharSequence cs, int start, int finish) {
		return accumulateChecksum(cs, start, finish) == 0;
	}

}
