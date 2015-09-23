# Bug Koops 1
**Bug Koops 1 (BK1)** is a simple format used by Bug Koops to compose back a data blob from multiple packets.
Please note that besides the guarantee that the data is not corrupt, there are no ties to the way you
get the packets (so no error correction). You can use any other barcode or any other way to transfer
the packets as long as it supports binary data.

## The format

First 10 bytes is the header as described here:
 1. **FirstMagicByte**, **SecondMagicByte** - 1 byte each used as identification bytes in case you want
 to distinguish between multiple data types **FirstMagicByte** = 222 and **SecondMagicByte** = 173
 2. **Version** - 1 byte: 4 most significant bits are reserved for "parameters"/slightly different flavors of
 a major version and the 4 least significant bits describe the major version (currently the only valid
 value for this field is 0)
 3. **MessageId** - 1 byte: used to distinguish between different messages sent at the same time
 4. **PacketCount** - 1 byte: number of packets in this message (so you can know when to stop waiting
 for more packets)
 5. **PacketId** - 1 byte: used to order the packets 0 <= **PacketId** <= **PacketCount**
 6. **EncodeMode** - 1 byte: how this packet is encoded. 6 least significant bits describe the mode
 itself and 2 most significant bits are used as parameters. Currently only 2 values are implemented:
    * Plain binary data: 0
    * DEFLATE compressed data: 1
 7. **Size** - 2 bytes: short unsigned integer representing the size of the this packet **after**
 decoding. First byte is the most significant
 8. **Checksum** - just in case XOR sum for bytes 3 to 7 ( **Checksum** = **Version** XOR **MessageId**
 XOR **PacketCount** XOR **PacketId** XOR **EncodeMode**)

All the bytes from here on are the data encoded according to **EncodeMode**.

> Any inconsistency in the header or data encoding should be treated as a corrupted packet and discarded as soon
as possible.

## Strategies for 1-way transfer
Because this format is designed specifically for 1-way transfer it makes sense to put some info about
this here.

Since the receiver can't request for anything (actually say anything) the only option is to continuously
send all of them for more than enough time so that the other end is able to capture them all.

### Bug Koops case (QRs)
Since we can only draw (read send) one QR at a time we don't have too much of a choice, we have to
iterate over all packets indefinitely. The only things we can tune are how much time we can show one
QR and in what order. The strategies that I played with (except for random walking which is just lame):
* **Naive**: circular iterating over packets with one pointer and with fixed time slice for every packet
* **Tortoise and Rabbit**: circular iterating over packets with 2 pointers in parallel (one twice as fast as the other)

Performance metrics on Note 3 and small step 250 ms:

|   Strategies   | 3 packets     |  9 packets     |  24 packets      |  72 packets |
| -------------- | -------------:| --------------:| ----------------:| -----------:|
| Naive best:    |   598 ms      |   2360 ms      |   11.2 s         |   Undefined |
| Naive average: |   985 ms      |   4285 ms      |   24.9 s         |   Undefined |
| Naive worst:   |  3751 ms      |   6750 ms      |   50.2 s         |   Undefined |
| TaR best:      |   540 ms      |   3430 ms      |   21.1 s         |   2 minutes |
| TaR average:   |  1130 ms      |   4760 ms      |   26.2 s         |   9 minutes |
| TaR worst:     |  2430 ms      |   7130 ms      |   40.2 s         |   Undefined |

> Undefined means over 10 minutes

What can not be shown in this table is the fact that TaR is much more nicely distributed around the
average value while Naive tends to be too random.
For an implementation you can check out [py-qr-transfer][1].

[1]: https://github.com/links234/py-qr-transfer
