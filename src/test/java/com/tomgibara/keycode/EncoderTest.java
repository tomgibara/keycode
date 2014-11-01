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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

public class EncoderTest extends TestCase {

	public void testAppendBytesBase32() throws Exception {
		Random r = new Random(0L);
		byte[] bs = new byte[5];
		for (int test = 0; test < 100; test++) {
			StringBuilder sb = new StringBuilder(8);
			r.nextBytes(bs);
			Encoder.appendBytesBase32(sb, bs, 0);
			byte[] check = new byte[5];
			Encoder.parseBytesBase32(sb, 0, check, 0);
			if (!Arrays.equals(bs, check)) {
				System.err.println(Arrays.toString(bs));
				System.err.println(sb);
				System.err.println(Arrays.toString(check));
				throw new IllegalStateException();
			}
		}
	}

	public void testAppend6Bits() throws Exception {
		Set<String> codes = new HashSet<>();
		for (int val = 0; val < (1 << 6); val++) {
			StringBuilder sb = new StringBuilder();
			Encoder.append6Bits(sb, val);
			String code = sb.toString();
			if (code.length() != 2) throw new IllegalStateException();
			int res = Encoder.parse6Bits(code, 0);
			if (res != val) throw new IllegalStateException("mismatch " + val + " -> " + res);
			if (!codes.add(code)) throw new IllegalStateException();
			if (code.charAt(0) == '0' || code.charAt(0) == code.charAt(1)) throw new IllegalStateException();
		}
	}
	
	public void testAppend9Bits() throws Exception {
		Set<String> codes = new HashSet<>();
		for (int val = 0; val < (1 << 9); val++) {
			StringBuilder sb = new StringBuilder();
			Encoder.append9Bits(sb, val);
			String code = sb.toString();
			if (code.length() != 3) throw new IllegalStateException();
			int res = Encoder.parse9Bits(code, 0);
			if (res != val) throw new IllegalStateException("mismatch " + val + " -> " + res);
			if (!codes.add(code)) throw new IllegalStateException();
			if (code.charAt(0) == '0' || code.charAt(0) == code.charAt(1) || code.charAt(0) == code.charAt(2) || code.charAt(1) == code.charAt(2)) throw new IllegalStateException();
		}
	}
	
}
