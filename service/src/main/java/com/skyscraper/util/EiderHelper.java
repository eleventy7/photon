package com.skyscraper.util;

import org.agrona.DirectBuffer;

public final class EiderHelper {
  /**
   * private constructor.
   */
  private EiderHelper() {
    //unused;
  }

  /**
   * Reads the Eider Id from the buffer at the offset provided.
   */
  public static short getEiderId(DirectBuffer buffer, int offset) {
    return buffer.getShort(offset, java.nio.ByteOrder.LITTLE_ENDIAN);
  }

  /**
   * Reads the Eider Group Id from the buffer at the offset provided.
   */
  public static short getEiderGroupId(DirectBuffer buffer, int offset) {
    return buffer.getShort(offset + 2, java.nio.ByteOrder.LITTLE_ENDIAN);
  }
}
