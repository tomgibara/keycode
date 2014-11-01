package com.tomgibara.keycode;

import static java.lang.String.format;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;

import com.tomgibara.bits.BitVector;

public class KeycodeExamples {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		PrintStream out = System.out;

		// obtain a 256 bit AES key
		KeyGenerator keygen = KeyGenerator.getInstance("AES"); keygen.init(256);
		byte[] key = keygen.generateKey().getEncoded();

		// hex representation
		out.println(String.format("%064x", new BigInteger(1, key)));

		// output keycode with newlines and spaces
		String standard = Keycode.Format.standard().keycode(key).toString();

		// output keycode with no whitespace
		String plain = Keycode.Format.plain().keycode(key).toString();

		// output keycode with platform specific whitespace
		String platform = Keycode.Format.platform().keycode(key).toString();

		out.println();
		out.println(standard);
		out.println();
		out.println(plain);
		out.println();
		out.println(platform);

		// parse the generated output
		Keycode.Format.standard().parse(standard).getKey();

		// show all zero key
		System.out.println();
		Arrays.fill(key, (byte) 0);
		System.out.println(Keycode.Format.plain().keycode(key));
		
		out.println();
		BitVector v = new BitVector(new Random(0), 256);
		System.out.println(v.getBits(251, 5));
		out.println("Key as hex: " + v.toString(16).substring(0, 32));
		out.println("            " + v.toString(16).substring(   32));
		StringBuilder sb = new StringBuilder(" 0");
		for (int row = 0; row < 6; row++) {
			int c = 0;
			for (int col = 0; col < 8; col++) {
				if (col != 0) out.print(' ');
				int i = 251 - 5 * (row * 8 + col);
				int n = (int) v.getBits(i, 5);
				c = TAQG32.op(c, n);
				if (row == 0) sb.append(format("% 3d", c));
				out.print(format("% 3d", n));
			}
			c = TAQG32.opInv(c, 0);
			out.println(format(" *% 3d", c));
		}
		BitVector w = new BitVector(24);
		w.setShort(8, v.shortValue());
		out.println("Remaining bits: " + w.getBits(15, 9) + " " + w.getBits(6, 9) + " " + w.getBits(0, 6));
		out.println("Partial sums (first row):" + sb);
		out.println();
		out.println(Keycode.Format.standard().keycode(v.toByteArray()));
	}
	
}
