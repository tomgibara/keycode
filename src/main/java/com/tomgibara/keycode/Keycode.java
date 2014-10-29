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

import java.io.Serializable;
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

public final class Keycode implements Serializable {

	private static String encode(byte[] key) {
		StringBuilder sb = new StringBuilder();
		
		// initial rows
		for (int i = 0; i < 30; i += 5) {
			Encoder.appendBytes(sb, key, i);
			int length = sb.length();
			sb.append(TAQG32.compute(sb, length - 8, length));
		}
		
		// last row
		int block1 = ((key[30] & 0xff) << 1) | ((key[31] & 0x80) >> 7);
		int block2 = ((key[31] & 0x7f) << 2) | ((key[32] & 0xc0) >> 6);
		int block3 =  (key[32] & 0x3f);
		Encoder.append9Bits(sb, block1);
		Encoder.append9Bits(sb, block2);
		Encoder.append6Bits(sb, block3);
		char c = TAQG10.compute(sb, 54, 62);
		char k = sb.charAt(61);
		if (c == k) {
			block2 ^= 2; // flip the sign-bit of the tag
			sb.setLength(57);
			Encoder.append9Bits(sb, block2);
			Encoder.append6Bits(sb, block3);
			c = TAQG10.compute(sb, 54, 62);
		}
		sb.append(c);
		
		return sb.toString();
	}
	
	private static byte[] decode(String str) {
		// basic checks
		if (str.length() == 0) throw new IllegalArgumentException("blank code");
		if (str.length() < 63) throw new IllegalArgumentException("short code: " + str.length() + " characters");
		if (str.length() > 63) throw new IllegalArgumentException("long code: " + str.length() + " characters");
		for (int i = 54; i < 62; i++) { // TODO strengthen
			char c = str.charAt(i);
			if (c < 48 || c >= 58) throw new IllegalArgumentException("expected digit at character " + (i+1));
		}
		if (str.charAt(54) == '0') throw new IllegalArgumentException("invalid zero at first character of last row");
		if (str.charAt(57) == '0') throw new IllegalArgumentException("invalid zero at fourth character of last row");
		if (str.charAt(60) == '0') throw new IllegalArgumentException("invalid zero at seventh character of last row");

		// checksums
		for (int i = 0; i < 54; i += 9) {
			if (!TAQG32.verify(str, i, i + 9)) throw new IllegalArgumentException("invalid checksum for row " + (i / 9 + 1));
		}
		if (!TAQG10.verify(str, 54, 63)) throw new IllegalArgumentException("invalid checksum for last row");
		
		// parsing
		byte[] key = new byte[33];
		for (int i = 0; i < 6; i++) {
			Encoder.parseBytes(str, i * 9, key, i * 5);
		}
		int block1 = Encoder.parse9Bits(str, 54);
		int block2 = Encoder.parse9Bits(str, 57);
		int block3 = Encoder.parse6Bits(str, 60);
		key[30] = (byte) (  block1                 >> 1               );
		key[31] = (byte) (  block1                 << 7 | block2 >> 2 );
		key[32] = (byte) ( (block2 & 0b0000111101) << 6 | block3      );
		
		// all good - return
		return key;
	}

	/**
	 * Defines formatting rules for outputting a keycode to a string.
	 * 
	 * @author tomgibara
	 */
	
	public static final class Format implements Serializable {

		private static boolean isWhitespaceOnly(String str) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c >= 128 || Encoder.VALUES[c] != -2) {
					return false;
				}
			};
			return true;
		}

		private static final long serialVersionUID = -7846537134506341028L;

		private static final Format PLAIN = new Format("", "");
		private static final Format STANDARD = new Format(" ", "\n");
		private static final Format PLATFORM;
		
		static {
			String s = String.format("%n");
			PLATFORM = s.equals(STANDARD.lineSeparator) ? STANDARD : new Format(" ", s);
		}
		
		/**
		 * With this format, the keycode will not include group separators or
		 * line separators. It will consist only of upper case alphanumeric
		 * characters.
		 * 
		 * @return a format without whitespace
		 */
		
		public static Format plain() {
			return PLAIN;
		}
		
		/**
		 * A format in which, the keycode will have newlines separating each
		 * group-triple and a space between each group on a line. This is the
		 * standard format which is expected to provide the best
		 * communicability.
		 * 
		 * @return a format with standard whitespace
		 */
		
		public static Format standard() {
			return STANDARD;
		}
		
		/**
		 * Matches the standard format with the exception that the platform line
		 * separator is used. This format will be identical to that of
		 * {@link #formatStandard()} on platforms which use a single newline as a
		 * line separator.
		 * 
		 * @return a format with platform whitespace
		 */
		
		public static Format platform() {
			return PLATFORM;
		}

		/**
		 * Creates a new format with the specified group and line separators.
		 * Separators must only consist of ASCII whitespace, specifically the
		 * characters: ' ', '\t', '\n', '\r'
		 * 
		 * @param groupSeparator
		 *            the characters inserted between groups
		 * @param lineSeparator
		 *            the characters inserted between lines
		 * @return a format with the specified separators
		 */
		
		public static Format custom(String groupSeparator, String lineSeparator) {
			if (groupSeparator == null) throw new IllegalArgumentException("null groupSeparator");
			if (!isWhitespaceOnly(groupSeparator)) throw new IllegalArgumentException("non-whitespace groupSeparator");
			if (lineSeparator == null) throw new IllegalArgumentException("null lineSeparator");
			if (!isWhitespaceOnly(lineSeparator)) throw new IllegalArgumentException("non-whitespace lineSeparator");
			//TODO canonicalize against standard formats?
			return new Format(groupSeparator, lineSeparator);
		}
		
		final String groupSeparator;
		final String lineSeparator;
		
		private Format(String groupSeparator, String lineSeparator) {
			this.groupSeparator = groupSeparator;
			this.lineSeparator = lineSeparator;
		}

		public String getGroupSeparator() {
			return groupSeparator;
		}
		
		public String getLineSeparator() {
			return lineSeparator;
		}

		/**
		 * Encapsulates a 256 bit key for subsequent output as a keycode via the
		 * {@link #Keycode.toString()} method. The tag is implicitly assumed to
		 * be zero.
		 * 
		 * @param key
		 *            a 32 byte array containing key data
		 * @throws IllegalArgumentException
		 *             if the array is not 32 bytes long
		 */
		
		public Keycode keycode(byte[] key) {
			if (key == null) throw new IllegalArgumentException("null key");
			if (key.length != 32) throw new IllegalArgumentException("invalid key length");
			byte[] copy = new byte[33];
			System.arraycopy(key, 0, copy, 0, 32);
			return new Keycode(this, copy, encode(copy));
		}

		/**
		 * Encapsulates a 256 bit key for subsequent output as a keycode,
		 * together with a 7 bit tag which is also encoded along with the key.
		 * The tag must be non-negative, that is, only the most-significant-bit
		 * must be zero.
		 * 
		 * @param key
		 *            a 32 byte array containing key data
		 * @param tag
		 *            a 7 bit value that augments the key data
		 * @throws IllegalArgumentException
		 *             if the array is not 32 bytes long
		 * @see #getTag()
		 */

		public Keycode keycode(byte[] key, byte tag) {
			if (key == null) throw new IllegalArgumentException("null key");
			if (key.length != 32) throw new IllegalArgumentException("invalid key length");
			if (tag < 0) throw new IllegalArgumentException("negative tag");
			byte[] copy = new byte[33];
			System.arraycopy(key, 0, copy, 0, 32);
			copy[32] = tag;
			return new Keycode(this, copy, encode(copy));
		}
		
		/**
		 * Returns a keycode with the same key and tag as an existing keycode,
		 * 
		 * @param an existing keycode
		 * @return the same key combined with this format.
		 */

		public Keycode keycode(Keycode keycode) {
			if (keycode == null) throw new IllegalArgumentException("null keycode");
			return keycode.format.equals(this) ? keycode : new Keycode(this, keycode.key, keycode.code);
		}
		
		/**
		 * <p>
		 * Parses a keycode from character data.
		 * <p>
		 * Note: The parse method is not strict, in the sense that any
		 * whitespace group and line separators will be processed, irrespective
		 * of the format used to perform the parsing.
		 * 
		 * 
		 * @param code
		 *            the character data of the code, typically a String
		 * @throws IllegalArgumentException
		 *             if the code contains non-whitespace, non-code characters
		 *             OR has an invalid structure OR a data error is detected
		 * @return a successfully parsed keycode with this format
		 */
		
		public Keycode parse(CharSequence code) {
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
			
			// convert to a String and decode
			//note: risk that char sequence will not return same characters that were checked
			String str = sb == null ? code.toString() : sb.append(code, last, codeLength).toString();
			byte[] key = decode(str);

			// done
			return new Keycode(this, key, str);
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
		
		private Object readResolve() {
			if (this.equals(STANDARD)) return STANDARD;
			if (this.equals(PLAIN)) return PLAIN;
			if (this.equals(PLATFORM)) return PLATFORM;
			return this;
		}
	}
	
	private static final long serialVersionUID = -8610389751205547848L;

	private final Format format;
	private final byte[] key;
	private String code;

	private Keycode(Format format, byte[] key, String code) {
		this.format = format;
		this.key = key;
		this.code = code;
	}
	
	/**
	 * The format which controls the output of {@link #toString()}
	 * 
	 * @return the keycode format
	 */
	
	public Format getFormat() {
		return format;
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
	 * @return the tag associated with the key, non-negative, typically zero
	 */
	
	public byte getTag() {
		return key[32];
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(key) ^ format.hashCode();
	}
	
	/**
	 * Two keycodes are equal if they encapsulate the same key and produce the
	 * same output.
	 */
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Keycode)) return false;
		Keycode that = (Keycode) obj;
		if (!Arrays.equals(this.key, that.key)) return false;
		if (!this.format.equals(that.format)) return false;
		return true;
	}
	
	/**
	 * Formats a key into a @code{String} using the keycode format.
	 * 
	 * @param format
	 *            controls the formatting of the output
	 * @see #getFormat()
	 * @return a string containing the coded key
	 */
	
	@Override
	public String toString() {
		String lineSep = format.lineSeparator;
		String groupSep = format.groupSeparator;
		boolean noLines = lineSep.isEmpty();
		boolean noGroups = groupSep.isEmpty();
		if (noLines && noGroups) return code;
		StringBuilder sb = new StringBuilder(63 + 6 * lineSep.length() + 14 * groupSep.length());
		for (int i = 0; i < 63; i+= 9) {
			if (!noLines && i > 0) sb.append(lineSep);
			if (noGroups) {
				sb.append(code.substring(i    , i + 9));
			} else {
				sb.append(code.substring(i    , i + 3));
				sb.append(groupSep);
				sb.append(code.substring(i + 3, i + 6));
				sb.append(groupSep);
				sb.append(code.substring(i + 6, i + 9));
			}
		}
		return sb.toString();
	}
	
	
}
