Keycode
=======

Description
-----------

Keycode is a transcription friendly 256-bit key format [(spec)][1].

It takes a key like this:

    d642d9650d7bd3d895cd827848804004888dc70da684c74732a9daadc785d205

and converts it into something like

    TR1 DJR 8D6
    FF9 WH5 EDJ
    G9V 4H0 20C
    0J4 8UH QDF
    LT2 CEH RJ4
    M7D AUH V5E
    684 136 109

for the purpose of human transcription and possibly spoken key-exchange.

Design
------

The representation is brief, requiring fewer characters than the hexadecimal
encoding, nevertheless there are a number of important advantages:

* A tabular layout makes for easy visual confirmation that a key is complete:
  7 rows of three alphanumeric triples.
* A limited character set avoids the characters `I`, `O`, `S`, `Z` since they
  may be mistaken for digits.
* The digit grouping increases readability and encourages consistent verbal
  communication when needed.
* **Every row is protected by a checksum digit that can detect exactly one
  changed, or one transposed digit.**

The last row may be used as the basis of partial key verification, to assist
with this:

* The row consists only of digits (which can be clearly spoken in all languages
  and may be easily typed on a keypad)
* The groups never contain adjacent digit pairs (like `77`), this avoids the use
  of idiomatic verbal constructs (such as "double 7").
* No group in the row begins with a zero yielding regular three digit numbers.

Ergonomics and error detection combine to form a convenient format for written
and spoken 256-bit key exchange.

Usage
-----

The library is very simple, consisting of just two abstractions: the
`Keycode.Format` class which encapsulates formatting rules, and the `Keycode`
class which combines *key data* with a *format*.

To use the library you will need to be working with 256-bit keys.

    KeyGenerator keygen = KeyGenerator.getInstance("AES"); keygen.init(256);
    byte[] key = keygen.generateKey().getEncoded();

You can then produce a keycode in a standard format, simply and fluently:

    String standard = Keycode.Format.standard().keycode(key).toString();

This will produce output something like:

    G8J HTV TFU
    5U5 YNE WE2
    535 VF4 3FW
    R34 1B6 87A
    5CM BJ6 RR2
    U78 9W2 AC9
    287 304 101

For some applications, it may be useful to produce the keycode without any
whitespace.

    String plain = Keycode.Format.plain().keycode(key).toString();
    // G8JHTVTFU5U5YNEWE2535VF43FWR341B687A5CMBJ6RR2U789W2AC9287304101

A `Keycode.Format.platform()` format is also available which uses platform
dependent line separators. Formats using custom whitespace are also possible
using `Keycode.Format.custom()`.

Parsing a keycode is also simple and fluent:

    byte[] key = Keycode.Format.standard().parse(standard).getKey();

Failure to parse a keycode (for any reason, including the presence of invalid
characters, invalid structure, detected errors, etc.) will be reported with an
`IllegalArgumentException` from the `parse()` method.

*Note that a keycode generated with any format is parsable by any other format.*
Finally, note that all classes are immutable, serializable and threadsafe.

[1]: https://raw.githubusercontent.com/wiki/tomgibara/keycode/docs/keycode-specification-1.0.pdf "Keycode specification 1.0"
