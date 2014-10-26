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

import static com.tomgibara.keycode.Encoder.CHARS;
import static com.tomgibara.keycode.Encoder.VALUES;

import java.util.Arrays;
import java.util.Random;

import com.tomgibara.keycode.Keycode.Format;

import junit.framework.TestCase;

public class KeycodeTest extends TestCase {

	public void testModifications() throws Exception {
		byte[] key = new byte[32];
		Random r = new Random(0L);
		r.nextBytes(key);
		Keycode keycode = new Keycode(key);
		String str = keycode.toString();
		StringBuilder sb = new StringBuilder(str);

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 9; j++) {
				int index = i * 9 + j;
				char oldChar = sb.charAt(index);
				int oldVal = VALUES[oldChar] & 0xff;
				int limit = i < 9 ? 10 : 32;
				for (int newVal = 0; newVal < limit; newVal++) {
					if (newVal == oldVal) continue;
					char newChar = CHARS[newVal];
					sb.setCharAt(index, newChar);
					try {
						Keycode badcode = Keycode.parse(sb.toString());
						System.out.println("Row " + i + " Column " + j);
						System.out.println("BAD KEYCODE:");
						System.out.println(badcode);
						throw new RuntimeException("Expected checksum error");
					} catch (IllegalArgumentException e) {
						/* expected */
					}
				}
				sb.setCharAt(index, oldChar);
			}
		}
	}
	
	public void testTranspositions() throws Exception {
		byte[] key = new byte[32];
		Random r = new Random(0L);
		r.nextBytes(key);
		Keycode keycode = new Keycode(key);
		String str = keycode.toString();
		StringBuilder sb = new StringBuilder(str);

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				int index = i * 9 + j;
				char c1 = sb.charAt(index    );
				char c2 = sb.charAt(index + 1);
				if (c1 == c2) continue;
				sb.setCharAt(index    , c2);
				sb.setCharAt(index + 1, c1);
				try {
					Keycode badcode = Keycode.parse(sb);
					System.out.println(keycode);
					System.out.println("BAD KEYCODE:");
					System.out.println(badcode);
					throw new RuntimeException("Expected checksum error " + i + "," + j);
				} catch (IllegalArgumentException e) {
					/* expected */
				}
				sb.setCharAt(index    , c1);
				sb.setCharAt(index + 1, c2);
			}
		}
	}

	public void testBijective() throws Exception {
		Random r = new Random(0L);
		for (int test = 0; test < 10000; test++) {
			byte[] bytes = new byte[32];
			r.nextBytes(bytes);
			Keycode first = new Keycode(bytes);
			String firstStr = first.toString();
			Keycode second;
			try {
				second = Keycode.parse(firstStr);
			} catch (IllegalArgumentException e) {
				System.err.println(firstStr);
				throw e;
			}
			if (!first.equals(second)) {
				System.err.println("FIRST:");
				System.err.println(first);
				System.err.println(Arrays.toString(first.getKey()));
				System.err.println("SECOND:");
				System.err.println(second);
				System.err.println(Arrays.toString(second.getKey()));
				throw new IllegalStateException("not equal");
			}
		}
	}
	
	public void testFormat() {
		Random r = new Random(10001L);
		byte[] key = new byte[32];
		r.nextBytes(key);
		Keycode keycode = new Keycode(key);
		
		String standard = keycode.format(Format.STANDARD);
		assertEquals(63 + 6 + 14, standard.length());
		assertEquals(keycode, Keycode.parse(standard));
		
		System.out.println(standard);
	}
	
}