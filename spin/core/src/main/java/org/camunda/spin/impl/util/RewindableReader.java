/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.impl.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;

/**
 * Caches the initial characters that are read from the supplied {@link Reader} and
 * allows to rewind these. As soon as more than <code>size</code> characters have been read,
 * rewinding fails.
 *
 * @author Thorben Lindhauer
 */
public class RewindableReader extends Reader {

  private static final SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  protected PushbackReader wrappedReader;

  protected char[] buffer;
  protected int pos;
  protected boolean rewindable;

  public RewindableReader(Reader input, int size) {
    this.wrappedReader = new PushbackReader(input, size);
    this.buffer = new char[size];
    this.pos = 0;
    this.rewindable = true;
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    int charactersRead = wrappedReader.read(cbuf, off, len);

    if (charactersRead > 0) {
      if (rewindable && pos + charactersRead > buffer.length) {
        rewindable = false;
      }

      if (pos < buffer.length) {
        int freeBufferSpace = buffer.length - pos;
        int insertableCharacters = Math.min(charactersRead, freeBufferSpace);
        System.arraycopy(cbuf, off, buffer, pos, insertableCharacters);
        pos += insertableCharacters;
      }
    }

    return charactersRead;
  }

  public int read() throws IOException {
    int nextCharacter = wrappedReader.read();

    if (nextCharacter != -1) {
      if (pos < buffer.length) {
        buffer[pos] = (char) nextCharacter;
        pos++;
      } else if (rewindable && pos >= buffer.length) {
        rewindable = false;
      }
    }

    return nextCharacter;
  }

  public void close() throws IOException {
    wrappedReader.close();
  }

  public synchronized void mark(int readlimit) throws IOException {
    wrappedReader.mark(readlimit);
  }

  public boolean markSupported() {
    return wrappedReader.markSupported();
  }

  public synchronized void reset() throws IOException {
    wrappedReader.reset();
  }

  public long skip(long n) throws IOException {
    return wrappedReader.skip(n);
  }

  /**
   * Rewinds the reader such that the initial characters are returned when invoking read().
   *
   * Throws an exception if more than the buffering limit has already been read.
   * @throws IOException
   */
  public void rewind() throws IOException {
    if (!rewindable) {
      throw LOG.unableToRewindReader();
    }

    wrappedReader.unread(buffer, 0, pos);
    pos = 0;
  }

  public int getRewindBufferSize() {
    return buffer.length;
  }

  /**
   *
   * @return the number of characters that can still be read and rewound.
   */
  public int getCurrentRewindableCapacity() {
    return buffer.length - pos;
  }

}
