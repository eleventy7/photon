package com.skyscraper.prototypes;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class ReconstructedBookFlyweight {
  /**
   * The eider spec id for this type. Not written to the output buffer as there is no header.
   */
  public static final short EIDER_ID = 5;

  /**
   * The byte offset in the byte array for this SHORT. Byte length is 2.
   */
  private static final int SIDE_OFFSET = 0;

  /**
   * The byte offset in the byte array for this SHORT. Byte length is 2.
   */
  private static final int VENUE_OFFSET = 2;

  /**
   * The byte offset in the byte array for this SHORT. Byte length is 2.
   */
  private static final int CCYPAIR_OFFSET = 4;

  /**
   * The byte offset in the byte array for this LONG. Byte length is 8.
   */
  private static final int UPDATEDMICROS_OFFSET = 6;

  /**
   * The byte offset in the byte array for this INT. Byte length is 4.
   */
  private static final int PRICESIZERECORD_COUNT_OFFSET = 14;

  /**
   * The byte offset in the byte array to start writing PriceSizeRecord.
   */
  private static final int PRICESIZERECORD_RECORD_START_OFFSET = 18;

  /**
   * The total bytes required to store the core data, excluding any repeating record data. Use precomputeBufferLength to compute buffer length this object.
   */
  private static final int BUFFER_LENGTH = 18;

  /**
   * Indicates if this flyweight holds a fixed length object.
   */
  public static final boolean FIXED_LENGTH = false;

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
   * The max number of items allocated for this record. Use resize() to alter.
   */
  private int PRICESIZERECORD_COMMITTED_SIZE = 0;

  /**
   * The flyweight for the PriceSizeRecord record.
   */
  private PriceSizeRecord PRICESIZERECORD_FLYWEIGHT = new PriceSizeRecord();

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
   * Reads side as stored in the buffer.
   */
  public short readSide() {
    return buffer.getShort(initialOffset + SIDE_OFFSET);
  }

  /**
   * Writes side to the buffer. Returns true if success, false if not.
   * @param value Value for the side to write to buffer.
   */
  public boolean writeSide(short value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putShort(initialOffset + SIDE_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * Reads venue as stored in the buffer.
   */
  public short readVenue() {
    return buffer.getShort(initialOffset + VENUE_OFFSET);
  }

  /**
   * Writes venue to the buffer. Returns true if success, false if not.
   * @param value Value for the venue to write to buffer.
   */
  public boolean writeVenue(short value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putShort(initialOffset + VENUE_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * Reads ccyPair as stored in the buffer.
   */
  public short readCcyPair() {
    return buffer.getShort(initialOffset + CCYPAIR_OFFSET);
  }

  /**
   * Writes ccyPair to the buffer. Returns true if success, false if not.
   * @param value Value for the ccyPair to write to buffer.
   */
  public boolean writeCcyPair(short value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putShort(initialOffset + CCYPAIR_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * Reads updatedMicros as stored in the buffer.
   */
  public long readUpdatedMicros() {
    return buffer.getLong(initialOffset + UPDATEDMICROS_OFFSET, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Writes updatedMicros to the buffer. Returns true if success, false if not.
   * @param value Value for the updatedMicros to write to buffer.
   */
  public boolean writeUpdatedMicros(long value) {
    if (!isMutable) throw new RuntimeException("Cannot write to immutable buffer");
    mutableBuffer.putLong(initialOffset + UPDATEDMICROS_OFFSET, value, java.nio.ByteOrder.LITTLE_ENDIAN);
    return true;
  }

  /**
   * True if transactions are supported; false if not.
   */
  public boolean supportsTransactions() {
    return false;
  }

  /**
   * Precomputes the required buffer length with the given record sizes
   */
  public int precomputeBufferLength(int PriceSizeRecordCount) {
    return BUFFER_LENGTH + (PriceSizeRecordCount * PriceSizeRecord.BUFFER_LENGTH);
  }

  /**
   * The required buffer size given current max record counts
   */
  public int committedBufferLength() {
    return BUFFER_LENGTH + (PRICESIZERECORD_COMMITTED_SIZE * PriceSizeRecord.BUFFER_LENGTH);
  }

  /**
   * Sets the amount of PriceSizeRecord items that can be written to the buffer
   */
  public void resetPriceSizeRecordSize(int PriceSizeRecordCommittedSize) {
    PRICESIZERECORD_COMMITTED_SIZE = PriceSizeRecordCommittedSize;
    buffer.checkLimit(committedBufferLength());
    mutableBuffer.putInt(PRICESIZERECORD_COUNT_OFFSET + initialOffset, PriceSizeRecordCommittedSize, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Returns & internally sets the amount of PriceSizeRecord items that the buffer potentially contains
   */
  public int readPriceSizeRecordSize() {
    PRICESIZERECORD_COMMITTED_SIZE = mutableBuffer.getInt(PRICESIZERECORD_COUNT_OFFSET);
    return PRICESIZERECORD_COMMITTED_SIZE;
  }

  /**
   * Gets the PriceSizeRecord flyweight at the given index
   */
  public PriceSizeRecord getPriceSizeRecord(int offset) {
    if (PRICESIZERECORD_COMMITTED_SIZE < offset) throw new RuntimeException("cannot access record beyond committed size");
    PRICESIZERECORD_FLYWEIGHT.setUnderlyingBuffer(this.buffer, PRICESIZERECORD_RECORD_START_OFFSET + initialOffset + (offset * PriceSizeRecord.BUFFER_LENGTH));
    return PRICESIZERECORD_FLYWEIGHT;
  }
}
