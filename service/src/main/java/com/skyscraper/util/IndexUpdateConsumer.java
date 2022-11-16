package com.skyscraper.util;

@FunctionalInterface
public interface IndexUpdateConsumer<T> {
  /**
   * Accepts an index update for the given offset and type<T> value
   */
  void accept(int offset, T t);
}
