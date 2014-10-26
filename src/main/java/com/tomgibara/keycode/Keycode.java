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

import static com.tomgibara.keycode.Encoder.VALUES;

import java.util.Arrays;

/**
 * <p>
 * Encapsulates 256 bit (32 byte) keys for purpose of parsing & formatting them
 * into ASCII keycodes. The formatted output is designed to provide brevity
 * readability and a degree of human verifiability combined with the support of
 * error detecting codes.
 * <p>
 * Instances of this class are immutable and are designed to be 'cheap' and
 * 'temporary'.
 * <p>
 * Passing null into any method or constructor in this class will raise a
 * {@code IllegalArgumentException}.
 * 
 * @author tomgibara
 * 
 */

//TODO should we support serializability? Contains sensitive data - should be resisted?
public final class Keycode {

	/**
	 * Defines formatting rules for outputting a keycode to a string.
	 * 
	 * @author tomgibara
	 */
	
	public static final class Format {

		/**
		 * With this format, the keycode will not include group separators or
		 * line separators. It will consist only of upper case alphanumeric
		 * characters.
		 */
		
		public static final Format UNBROKEN = new Format("", "");
		
		/**
		 * With this format, the keycode will have newlines separating each
		 * group-triple and a space between each group on a line. This is the
		 * standard format which is expected to provide the best
		 * communicability.
		 */
		
		public static final Format STANDARD = new Format(" ", "\n");
		
		private static boolean isWhitespaceOnly(String str) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c >= 128 || Encoder.VALUES[c] != -2) {
					return false;
				}
			};
			return true;
		}

		final String groupSeparator;
		final String lineSeparator;
		
		//TODO consider changing to static method
		public Format(String groupSeparator, String lineSeparator) {
			if (groupSeparator == null) throw new IllegalArgumentException("null groupSeparator");
			if (!isWhitespaceOnly(groupSeparator)) throw new IllegalArgumentException("non-whitespace groupSeparator");
			if (lineSeparator == null) throw new IllegalArgumentException("null lineSeparator");
			if (!isWhitespaceOnly(lineSeparator)) throw new IllegalArgumentException("non-whitespace lineSeparator");
			this.groupSeparator = groupSeparator;
			this.lineSeparator = lineSeparator;
		}

		public String getGroupSeparator() {
			return groupSeparator;
		}
		
		public String getLineSeparator() {
			return lineSeparator;
		}

		@Override
		public int hashCode() {
			return groupSeparator.hashCode() * 31 ^ lineSeparator.hashCode();
		}
		
		/**
		 * Two formats are equal if they produce identical output over all
		 * possible keys.
		 */
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Format)) return false;
			Format that = (Format) obj;
			if (!this.groupSeparator.equals(that.groupSeparator)) return false;
			if (!this.lineSeparator.equals(that.lineSeparator)) return false;
			return true;
		}
		
	}
	
	private static String toString(byte[] key) {
		StringBuilder sb = new StringBuilder();
		
		// first row
		int block1 = ((key[0] & 0xff) << 1) | ((key[1] & 0x80) >> 7);
		int block2 = ((key[1] & 0x7f) << 2) | ((key[2] & 0xc0) >> 6);
		int block3 =  (key[2] & 0x3f);
		Encoder.append3Bits(sb, block1);
		Encoder.append3Bits(sb, block2);
		Encoder.append2Bits(sb, block3);
		sb.append(TAQG10.compute(sb, 0, 8));
		
		// subsequent rows
		for (int i = 3; i < 33; i += 5) {
			Encoder.appendBytes(sb, key, i);
			int length = sb.length();
			sb.append(TAQG32.compute(sb, length - 8, length));
		}
		
		return sb.toString();
	}
	
	/**
	 * Parses a keycode from character data.
	 * 
	 * @param code
	 *            the character data of the code, typically a String
	 * @throws IllegalArgumentException
	 *             if the code contains non-whitespace, non-code characters OR
	 *             has an invalid structure OR a data error is detected
	 * @return a successfully parsed keycode
	 */
	
	public static Keycode parse(CharSequence code) {
		if (code == null) throw new IllegalArgumentException("null code");
		if (code.length() == 0) throw new IllegalArgumentException("empty code");
		
		// eliminate whitespace and check characters
		StringBuilder sb = null;
		int last = 0;
		int codeLength = code.length();
		for (int i = 0; i < codeLength; i++) {
			char c = code.charAt(i);
			if (c >= 128) throw new IllegalArgumentException("non-ascii character at " + (i + 1));
			int value = VALUES[c];
			switch (value) {
			case -2:
				if (last != i) {
					if (sb == null) sb = new StringBuilder(63);
					sb.append(code, last, i);
				}
				last = i + 1;
				continue;
			case -1: throw new IllegalArgumentException("invalid character at " + (i + 1));
			default: continue;
			}
		}
		
		// convert to a String
		//TODO risk that char sequence will not return same characters that were checked
		String str = sb == null ? code.toString() : sb.append(code, last, codeLength).toString();
		
		// basic checks
		if (str.length() == 0) throw new IllegalArgumentException("blank code");
		if (str.length() < 63) throw new IllegalArgumentException("short code: " + str.length() + " characters");
		if (str.length() > 63) throw new IllegalArgumentException("long code: " + str.length() + " characters");
		for (int i = 0; i < 8; i++) { // TODO strengthen
			char c = str.charAt(0);
			if (c < 48 || c >= 58) throw new IllegalArgumentException("expected digit at character " + (i+1));
		}
		if (str.charAt(0) == '0') throw new IllegalArgumentException("invalid zero at first character");
		if (str.charAt(3) == '0') throw new IllegalArgumentException("invalid zero at fourth character");

		// checksums
		if (!TAQG10.verify(str, 0, 9)) throw new IllegalArgumentException("invalid checksum for first row");
		for (int i = 9; i < 63; i += 9) {
			if (!TAQG32.verify(str, i, i + 9)) throw new IllegalArgumentException("invalid checksum for row " + (i / 9 + 1));
		}
		
		// parsing
		byte[] key = new byte[33];
		int block1 = Encoder.parse3Bits(str, 0);
		int block2 = Encoder.parse3Bits(str, 3);
		int block3 = Encoder.parse2Bits(str, 6);
		key[0] = (byte) (  block1 >> 1                  );
		key[1] = (byte) ( (block1 << 7) | (block2 >> 2) );
		key[2] = (byte) ( (block2 << 6) |  block3       );
		for (int i = 1; i < 7; i++) {
			Encoder.parseBytes(str, i * 9, key, i * 5 - 2);
		}

		// done
		return new Keycode(key, str);
	}
	
	private final byte[] key;
	private String code = null;

	private Keycode(byte[] key, String code) {
		this.key = key;
		this.code = code;
	}

	/**
	 * Encapsulates a 256 bit key for subsequent output as a keycode via the
	 * {@link #format(Format)} method. The tag is implicitly assumed to be
	 * zero.
	 * 
	 * @param key
	 *            a 32 byte array containing key data
	 */
	
	public Keycode(byte[] key) {
		this(key, (byte) 0);
	}
	
	/**
	 * Encapsulates a 256 bit key for subsequent output as a keycode, together
	 * with a single byte tag which is also encoded along with the key.
	 * 
	 * @param key
	 *            a 32 byte array containing key data
	 * @see #getTag()
	 */

	public Keycode(byte[] key, byte tag) {
		if (key == null) throw new IllegalArgumentException("null key");
		if (key.length != 32) throw new IllegalArgumentException("invalid key length");
		this.key = new byte[33];
		System.arraycopy(key, 0, this.key, 0, 32);
		this.key[32] = tag;
	}
	
	/**
	 * The key encapsulated by this object.
	 * 
	 * @return a 32 byte array containing a 256 bit key
	 */
	
	public byte[] getKey() {
		return Arrays.copyOf(key, 32);
	}

	/**
	 * The tag associated with this key. The tag may be used to distinguish
	 * multiple keys which are being supplied as part of a single message.
	 * Alternatively the tag may be used as part of an additional application
	 * specific checksum.
	 * 
	 * @return the tag associated with the key, typically zero
	 */
	
	public byte getTag() {
		return key[32];
	}

	/**
	 * Formats a key into a String using the supplied format.
	 * 
	 * @param format
	 *            controls the formatting of the output
	 * @return a string containing the keycode
	 */
	
	public String format(Format format) {
		if (format == null) throw new IllegalArgumentException("null format");
		String str = toString();
		String lineSep = format.lineSeparator;
		String groupSep = format.groupSeparator;
		boolean noLines = lineSep.isEmpty();
		boolean noGroups = groupSep.isEmpty();
		if (noLines && noGroups) return str;
		StringBuilder sb = new StringBuilder(63 + 6 * lineSep.length() + 14 * groupSep.length());
		for (int i = 0; i < 63; i+= 9) {
			if (i > 0) sb.append(lineSep);
			if (noGroups) {
				sb.append(str.substring(i    , i + 9));
			} else {
				sb.append(str.substring(i    , i + 3));
				sb.append(groupSep);
				sb.append(str.substring(i + 3, i + 6));
				sb.append(groupSep);
				sb.append(str.substring(i + 6, i + 9));
			}
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(key);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Keycode)) return false;
		Keycode that = (Keycode) obj;
		return Arrays.equals(this.key, that.key);
	}
	
	@Override
	public String toString() {
		if (code == null) {
			code = toString(key);
		}
		return code;
	}
	
}
