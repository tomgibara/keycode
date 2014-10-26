package com.tomgibara.keycode;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

public class KeycodeExamples {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		
		// obtain a 256 bit AES key
		KeyGenerator keygen = KeyGenerator.getInstance("AES"); keygen.init(256);
		byte[] key = keygen.generateKey().getEncoded();

		// output keycode with newlines and spaces
		String standard = Keycode.Format.standard().keycode(key).toString();

		// output keycode with no whitespace
		String plain = Keycode.Format.plain().keycode(key).toString();
		
		// output keycode with platform specific whitespace
		String platform = Keycode.Format.platform().keycode(key).toString();

		System.out.println(standard);
		System.out.println();
		System.out.println(plain);
		System.out.println();
		System.out.println(platform);

		// parse the generated output
		Keycode.Format.standard().parse(standard).getKey();
		
	}
	
}
