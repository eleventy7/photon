package com.skyscraper.prototypes;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class PriceSizeRecord {
  /**
   * The byte offset in the byte array for this DOUBLE. Byte length is 8.
   */
  private static final int PRICE_OFFSET = 0;

  /**
   * The byte offset in the byte array for this DOUBLE. Byte length is 8.
   */
  private static final int SIZE_OFFSET = 8;

  /**
   * The total bytes required to store a single record.
   */
  public static final int BUFFER_LENGTH = 16;

  /**
   * The internal DirectBuffer.
   */
  private DirectBuffer buffer = null;

  /**
   * The internal DirectBuffer used for mutatation opertions. Valid only if a mutable buffer was provided.
   */
  private MutableDirectBuffer mutableBuffer = null;

  /**
   * The starting offset for reading and writing.
   */
  private int initialOffset;

  /**
   * Flag indicating if the buffer is mutable.
   */
  private boolean isMutable = false;

  /**
   * Uses the provided {@link DirectBuffer} from the given offset.
   * @param buffer - buffer to read from and write to.
   * @param offset - offset to begin reading from/writing to in the buffer.
   */
  public void setUnderlyingBuffer(DirectBuffer buffer, int offset) {
    this.initialOffset = offset;
    this.buffer = buffer;
    if (buffer instanceof MutableDirectBuffer) {
      mutableBuffer = (MutableDirectBuffer) buffer;
      isMutable = true;
    }
    else {
      isMutable = false;
    }
    buffer.checkLimit(initialOffset + BUFFER_LENGTH);
  }

  /**
   * Reads price as stored in the buffer.
   */
  public Double readPrice() {
    return buffer.getDouble(initialOffset + PRICE_OFFSET);
  }

  /**
   * Writes price to the buffer. Returns true if success, false if not.
   * @param value Value for the price to write to buffer.
   */
  public boolean writePrice(Double value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putDouble(initialOffset + PRICE_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * Reads size as stored in the buffer.
   */
  public Double readSize() {
    return buffer.getDouble(initialOffset + SIZE_OFFSET);
  }

  /**
   * Writes size to the buffer. Returns true if success, false if not.
   * @param value Value for the size to write to buffer.
   */
  public boolean writeSize(Double value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putDouble(initialOffset + SIZE_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }
}
