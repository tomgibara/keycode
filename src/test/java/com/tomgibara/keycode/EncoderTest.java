package com.tomgibara.keycode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

public class EncoderTest extends TestCase {

	public void testAppendBytes() throws Exception {
		Random r = new Random(0L);
		byte[] bs = new byte[5];
		for (int test = 0; test < 100; test++) {
			StringBuilder sb = new StringBuilder(8);
			r.nextBytes(bs);
			Encoder.appendBytes(sb, bs, 0);
			byte[] check = new byte[5];
			Encoder.parseBytes(sb, 0, check, 0);
			if (!Arrays.equals(bs, check)) {
				System.err.println(Arrays.toString(bs));
				System.err.println(sb);
				System.err.println(Arrays.toString(check));
				throw new IllegalStateException();
			}
		}
	}

	public void testAppend2Bits() throws Exception {
		Set<String> codes = new HashSet<>();
		for (int val = 0; val < (1 << 6); val++) {
			StringBuilder sb = new StringBuilder();
			Encoder.append2Bits(sb, val);
			String code = sb.toString();
			if (code.length() != 2) throw new IllegalStateException();
			int res = Encoder.parse2Bits(code, 0);
			if (res != val) throw new IllegalStateException("mismatch " + val + " -> " + res);
			if (!codes.add(code)) throw new IllegalStateException();
			if (code.charAt(0) == '0' || code.charAt(0) == code.charAt(1)) throw new IllegalStateException();
		}
	}
	
	public void testAppend3Bits() throws Exception {
		Set<String> codes = new HashSet<>();
		for (int val = 0; val < (1 << 9); val++) {
			StringBuilder sb = new StringBuilder();
			Encoder.append3Bits(sb, val);
			String code = sb.toString();
			if (code.length() != 3) throw new IllegalStateException();
			int res = Encoder.parse3Bits(code, 0);
			if (res != val) throw new IllegalStateException("mismatch " + val + " -> " + res);
			if (!codes.add(code)) throw new IllegalStateException();
			if (code.charAt(0) == '0' || code.charAt(0) == code.charAt(1) || code.charAt(0) == code.charAt(2) || code.charAt(1) == code.charAt(2)) throw new IllegalStateException();
		}
	}
	
}
