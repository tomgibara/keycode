Keycode
=======

Description
-----------

Keycode is a transcription friendly 256-bit key format.

It takes a key like this:

    f38640e25801b211ae50349f9a02cc8ecf83acf04488bc4b817ca0923708357e

and converts it into something like

    769 142 108
    V9C 03C GHW
    MR8 397 VTJ
    0B6 8WK V3A
    MKQ 492 5VE
    9E0 PR8 4JY
    6V4 3AY G0W

for the purpose of human transcription and possibly spoken key-exchange.

Design
------

The representation is brief, requiring fewer characters than the hexadecimal encoding, nevertheless there are a number of important advantages:

* A tabular layout makes for easy visual confirmation that a key is complete: 7 rows of three alphanumeric triples.
* A limited character set avoids the characters `I`, `O`, `S`, `Z` since they may be mistaken for digits.
* The digit grouping increases readability and encourages consistent verbal communication when needed.
* A numeric first row may be used as the basis of partial key verification (digits can be clearly spoken in all languages and may be easily typed on a phone keypad).
* To make verbal communication even more reliable, the first row never contains adjacent digit pairs (like `77`), this avoids the use of verbal constructs (such as "double 7").
* **Every row is protected by a checksum digit that can detect extactly one changed, or one transposed digit.**

These error detection capabilities combine to form a robust format for written and spoken 256-bit key exchange.

Usage
-----

The library is very simple, consisting of just two abstractions: the `Keycode.Format` class which encapsulates formatting rules, and the `Keycode` class which combines *key data* with a *format*.

To use the library you will need to be working with 256-bit keys.

    KeyGenerator keygen = KeyGenerator.getInstance("AES"); keygen.init(256);
    byte[] key = keygen.generateKey().getEncoded();

You can then produce a keycode in a standard format, simply and fluently:

    String standard = Keycode.Format.standard().keycode(key).toString();

This will produce output something like:

    542 587 122
    8UV DXR AMG
    0NL XKT UWF
    6FB 1RN FCH
    0YC E47 D2K
    BC2 BR5 9HF
    6WG RT0 G06

For some applications, it may be useful to produce the keycode without any whitespace.

    String plain = Keycode.Format.plain().keycode(key).toString();
    // 5425871228UVDXRAMG0NLXKTUWF6FB1RNFCH0YCE47D2KBC2BR59HF6WGRT0G06

A `Keycode.Format.platform()` format is also available which uses platform dependent line separators. Formats using custom whitespace are also possible using `Keycode.Format.custom()`.

Parsing a keycode is also simple and fluent:

    byte[] key = Keycode.Format.standard().parse(standard).getKey();

Failure to parse a keycode (for any reason, including the presence of invalid characters, invalid structure, detected errors, etc.) will be reported with an `IllegalArgumentException` from the `parse()` method.

*Note that a keycode generated with any format is parsable by any other format.* Finally, note that all classes are immutable, serializable and threadsafe.
