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

import java.util.Arrays;

final class Encoder {

	static final char[] CHARS_32 = {
		
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
		'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P',
		'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y'
		
	};
	
	static final byte[] VALUES_32 = new byte[128];
	
	static final char[] WHITESPACE = { ' ', '\t', '\n', '\r' };
	
	static {
		Arrays.fill(VALUES_32, (byte) -1);
		for (int i = 0; i < CHARS_32.length; i++) {
			VALUES_32[CHARS_32[i]] = (byte) i;
		}
		for (int i = 0; i < WHITESPACE.length; i++) {
			VALUES_32[WHITESPACE[i]] = -2;
		}
	}
	
	static void append9Bits(StringBuilder sb, int val) {
		int a = val / (9 * 8);
		a++;
		val %= 9 * 8;

		int b = val / 8;
		if (b >= a) b++;
		val %= 8;

		int c = val;
		if (c >= Math.min(a,b))  c++;
		if (c >= Math.max(a,b)) c++;

		sb.append((char) (48 + a));
		sb.append((char) (48 + b));
		sb.append((char) (48 + c));
	}

	static int parse9Bits(CharSequence src, int start) {
		int a = src.charAt(start + 0) - 48;
		int b = src.charAt(start + 1) - 48;
		int c = src.charAt(start + 2) - 48;

		{
			int dc = 0;
			if (c > a) dc++;
			if (c > b) dc++;
			c -= dc;
		}
		if (b > a) b--;
		a--;
		return (a * 9 + b) * 8 + c;
	}

	static void append6Bits(StringBuilder sb, int val) {
		int a = val / 9;
		a++;
		val %= 9;
		sb.append((char) (48 + a));

		int b = val;
		if (b >= a) b++;
		sb.append((char) (48 + b));
	}

	static int parse6Bits(CharSequence src, int start) {
		int a = src.charAt(start + 0) - 48;
		int b = src.charAt(start + 1) - 48;
		
		if (b > a) b--;
		a--;
		return a * 9 + b;
	}

/*
	    0       1       2       3       4   
	[      ][      ][      ][      ][      ]
	7654321076543210765432107654321076543210
	aaaaabbbbbcccccdddddeeeeefffffggggghhhhh
*/

	static void appendBytesBase32(StringBuilder sb, byte[] bs, int from) {
		int a = ((bs[from    ] & 0xf8) >> 3);
		int b = ((bs[from    ] & 0x07) << 2) | ((bs[from + 1] & 0xc0) >> 6);
		int c = ((bs[from + 1] & 0x3e) >> 1);
		int d = ((bs[from + 1] & 0x01) << 4) | ((bs[from + 2] & 0xf0) >> 4);
		int e = ((bs[from + 2] & 0x0f) << 1) | ((bs[from + 3] & 0x80) >> 7);
		int f = ((bs[from + 3] & 0x7c) >> 2);
		int g = ((bs[from + 3] & 0x03) << 3) | ((bs[from + 4] & 0xe0) >> 5);
		int h =  (bs[from + 4] & 0x1f);
		
		sb.append(CHARS_32[a]);
		sb.append(CHARS_32[b]);
		sb.append(CHARS_32[c]);
		sb.append(CHARS_32[d]);
		sb.append(CHARS_32[e]);
		sb.append(CHARS_32[f]);
		sb.append(CHARS_32[g]);
		sb.append(CHARS_32[h]);
	}
	
	static void parseBytesBase32(CharSequence src, int start, byte[] key, int offset) {
		int a = VALUES_32[src.charAt(start    )];
		int b = VALUES_32[src.charAt(start + 1)];
		int c = VALUES_32[src.charAt(start + 2)];
		int d = VALUES_32[src.charAt(start + 3)];
		int e = VALUES_32[src.charAt(start + 4)];
		int f = VALUES_32[src.charAt(start + 5)];
		int g = VALUES_32[src.charAt(start + 6)];
		int h = VALUES_32[src.charAt(start + 7)];

		key[offset    ] = (byte) ((a << 3) | (b >> 2));
		key[offset + 1] = (byte) ((b << 6) | (c << 1) | (d >> 4));
		key[offset + 2] = (byte) ((d << 4) | (e >> 1));
		key[offset + 3] = (byte) ((e << 7) | (f << 2) | (g >> 3));
		key[offset + 4] = (byte) ((g << 5) |  h      );
	}


}
