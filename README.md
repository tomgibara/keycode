Keycode
=======

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

The representation is brief, requiring fewer characters than the hexadecimal encoding, nevertheless there are a number of important advantages:

* A tabular layout makes for easy visual confirmation that a key is complete: 7 rows of three alphanumeric triples.
* A limited character set avoids the characters `I`, `O`, `S`, `Z` since they may be mistaken for digits.
* The digit grouping increases readability and encourages consistent verbal communication when needed.
* A numeric first row may be used as the basis of partial key verification (digits can be clearly spoken in all languages and may be easily typed on a phone keypad).
* To make verbal communication even more reliable, the first row never contains adjacent digit pairs (like `77`), this avoids the use of verbal constructs (such as "double 7").
* **Every row is protected by a checksum digit that can detect extactly one changed, or one transposed digit.**

These error detection capabilities combine to form a robust format for written and spoken 256-bit key exchange.
