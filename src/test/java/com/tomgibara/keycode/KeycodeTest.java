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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

import com.tomgibara.keycode.Keycode.Format;

import junit.framework.TestCase;

public class KeycodeTest extends TestCase {

	public void testModifications() throws Exception {
		byte[] key = new byte[32];
		Random r = new Random(0L);
		r.nextBytes(key);
		Format format = Format.plain();
		Keycode keycode = format.keycode(key);
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
						Keycode badcode = format.parse(sb.toString());
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
		Format format = Format.plain();
		Keycode keycode = format.keycode(key);
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
					Keycode badcode = format.parse(sb);
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
			byte tag = (byte) r.nextInt(128);
			r.nextBytes(bytes);
			Format format = Format.plain();
			Keycode first = format.keycode(bytes, tag);
			String firstStr = first.toString();
			Keycode second;
			try {
				second = format.parse(firstStr);
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
			// don't rely on keycode equality alone to confirm roundtrip preseves all values
			assertTrue(Arrays.equals(bytes, second.getKey()));
			assertEquals(tag, second.getTag());
		}
	}
	
	public void testFormat() throws IOException, ClassNotFoundException {
		Random r = new Random(10001L);
		byte[] key = new byte[32];
		r.nextBytes(key);
		testFormat( Format.standard().keycode(key) );
		testFormat( Format.platform().keycode(key) );
		testFormat( Format.custom("  ", "\t").keycode(key) );
	}

	private void testFormat(Keycode keycode) throws IOException, ClassNotFoundException {
		Format format = keycode.getFormat();

		// confirm output is parsable
		String str = keycode.toString();
		assertEquals(keycode, format.parse(str));

		// confirm output matches expected format
		int stdLength = Format.plain().keycode(keycode).toString().length();
		int expectedLength = stdLength + 7 * 2 * format.getGroupSeparator().length() + 6 * format.getLineSeparator().length();
		assertEquals(expectedLength, str.length());
		
		// confirm keycode is serializable with that format
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(out);
		oo.writeObject(keycode);
		oo.close();
		byte[] bytes = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = new ObjectInputStream(in);
		Object result = oi.readObject();
		assertEquals(keycode, result);
		assertEquals(str, result.toString());
		Format f = ((Keycode) result).getFormat();
		assertEquals(format == Format.plain(), f == Format.plain());
		assertEquals(format == Format.standard(), f == Format.standard());
		assertEquals(format == Format.platform(), f == Format.platform());
	}

	public void testNoDigitPairs() {
		Format format = Format.plain();
		Random r = new Random();
		byte[] key = new byte[32];
		byte tag;
		for (int i = 0; i < 10000; i++) {
			r.nextBytes(key);
			tag = (byte) r.nextInt(128);
			String str = format.keycode(key, tag).toString();
			assertFalse(str, str.charAt(54) == str.charAt(55));
			assertFalse(str, str.charAt(55) == str.charAt(56));
			assertFalse(str, str.charAt(57) == str.charAt(58));
			assertFalse(str, str.charAt(58) == str.charAt(59));
			assertFalse(str, str.charAt(60) == str.charAt(61));
			assertFalse(str, str.charAt(61) == str.charAt(62));
		}
	}

}
