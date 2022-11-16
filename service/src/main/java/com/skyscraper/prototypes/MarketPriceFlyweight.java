package com.skyscraper.prototypes;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class MarketPriceFlyweight {
  /**
   * The eider spec id for this type. Not written to the output buffer as there is no header.
   */
  public static final short EIDER_ID = 4;

  /**
   * The byte offset in the byte array for this LONG. Byte length is 8.
   */
  private static final int TIME_OFFSET = 0;

  /**
   * The byte offset in the byte array for this DOUBLE. Byte length is 8.
   */
  private static final int PRICE_OFFSET = 8;

  /**
   * The byte offset in the byte array for this LONG. Byte length is 8.
   */
  private static final int RECEIVETIME_OFFSET = 16;

  /**
   * The byte offset in the byte array for this LONG. Byte length is 8.
   */
  private static final int MICRORECEIVETIME_OFFSET = 24;

  /**
   * The total bytes required to store this fixed length object.
   */
  public static final int BUFFER_LENGTH = 32;

  /**
   * Indicates if this flyweight holds a fixed length object.
   */
  public static final boolean FIXED_LENGTH = true;

  /**
   * The internal DirectBuffer.
   */
  private DirectBuffer buffer = null;

  /**
   * The internal DirectBuffer used for mutatation opertions. Valid only if a mutable buffer was provided.
   */
  private MutableDirectBuffer mutableBuffer = null;

  /**
   * The internal UnsafeBuffer. Valid only if an unsafe buffer was provided.
   */
  private UnsafeBuffer unsafeBuffer = null;

  /**
   * The starting offset for reading and writing.
   */
  private int initialOffset;

  /**
   * Flag indicating if the buffer is mutable.
   */
  private boolean isMutable = false;

  /**
   * Flag indicating if the buffer is an UnsafeBuffer.
   */
  private boolean isUnsafe = false;

  /**
   * Uses the provided {@link DirectBuffer} from the given offset.
   * @param buffer - buffer to read from and write to.
   * @param offset - offset to begin reading from/writing to in the buffer.
   */
  public void setUnderlyingBuffer(DirectBuffer buffer, int offset) {
    this.initialOffset = offset;
    this.buffer = buffer;
    if (buffer instanceof UnsafeBuffer) {
      unsafeBuffer = (UnsafeBuffer) buffer;
      mutableBuffer = (MutableDirectBuffer) buffer;
      isUnsafe = true;
      isMutable = true;
    }
    else if (buffer instanceof MutableDirectBuffer) {
      mutableBuffer = (MutableDirectBuffer) buffer;
      isUnsafe = false;
      isMutable = true;
    }
    else {
      isUnsafe = false;
      isMutable = false;
    }
    buffer.checkLimit(initialOffset + BUFFER_LENGTH);
  }

  /**
   * Returns the eider sequence.
   * @return EIDER_ID.
   */
  public short eiderId() {
    return EIDER_ID;
  }

  /**
   * Reads time as stored in the buffer.
   */
  public long readTime() {
    return buffer.getLong(initialOffset + TIME_OFFSET, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Writes time to the buffer. Returns true if success, false if not.
   * @param value Value for the time to write to buffer.
   */
  public boolean writeTime(long value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putLong(initialOffset + TIME_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
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
   * Reads receiveTime as stored in the buffer.
   */
  public long readReceiveTime() {
    return buffer.getLong(initialOffset + RECEIVETIME_OFFSET, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Writes receiveTime to the buffer. Returns true if success, false if not.
   * @param value Value for the receiveTime to write to buffer.
   */
  public boolean writeReceiveTime(long value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putLong(initialOffset + RECEIVETIME_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * Reads microReceiveTime as stored in the buffer.
   */
  public long readMicroReceiveTime() {
    return buffer.getLong(initialOffset + MICRORECEIVETIME_OFFSET, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Writes microReceiveTime to the buffer. Returns true if success, false if not.
   * @param value Value for the microReceiveTime to write to buffer.
   */
  public boolean writeMicroReceiveTime(long value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putLong(initialOffset + MICRORECEIVETIME_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * True if transactions are supported; false if not.
   */
  public boolean supportsTransactions() {
    return false;
  }
}
