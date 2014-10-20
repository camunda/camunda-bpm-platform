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
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;

/**
 * Caches the initial bytes that are read from the supplied {@link InputStream} and 
 * allows to rewind these. As soon as more than <code>size</code> bytes have been read,
 * rewinding fails.
 * 
 * @author Thorben Lindhauer
 */
public class RewindableInputStream extends InputStream {

  private static final SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  protected PushbackInputStream wrappedStream;
  
  protected byte[] buffer;
  protected int pos;
  protected boolean rewindable;
  
  public RewindableInputStream(InputStream input, int size) {
    this.wrappedStream = new PushbackInputStream(input, size);
    this.buffer = new byte[size];
    this.pos = 0;
    this.rewindable = true;
  }
  
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    int bytesRead = wrappedStream.read(b, off, len);
    
    if (bytesRead > 0) {
      if (rewindable && pos + bytesRead > buffer.length) {
        rewindable = false;
      }
      
      if (pos < buffer.length) {
        int freeBufferSpace = buffer.length - pos;
        int insertableBytes = Math.min(bytesRead, freeBufferSpace);
        System.arraycopy(b, off, buffer, pos, insertableBytes);
        pos += insertableBytes;
      }
    }
    
    return bytesRead;
  }
  
  public int read() throws IOException {
    int nextByte = wrappedStream.read();
    
    if (nextByte != -1) {
      if (pos < buffer.length) {
        buffer[pos] = (byte) nextByte;
        pos++;
      } else if (rewindable && pos >= buffer.length) {
        rewindable = false;
      }
    }
    
    return nextByte;
  }
  
  public int available() throws IOException {
    return wrappedStream.available();
  }
  
  public void close() throws IOException {
    wrappedStream.close();
  }
  
  public synchronized void mark(int readlimit) {
    wrappedStream.mark(readlimit);
  }
  
  public boolean markSupported() {
    return wrappedStream.markSupported();
  }
  
  public synchronized void reset() throws IOException {
    wrappedStream.reset();
  }
  
  public long skip(long n) throws IOException {
    return wrappedStream.skip(n);
  }
  
  /**
   * Rewinds the stream such that the initial bytes are returned when invoking read().
   * 
   * Throws an exception if more than the buffering limit has already been read.
   * @throws IOException
   */
  public void rewind() throws IOException {
    if (!rewindable) {
      throw LOG.unableToRewindInputStream();
    }
    
    wrappedStream.unread(buffer, 0, pos);
    pos = 0;
  }

  public int getRewindBufferSize() {
    return buffer.length;
  }
  
  /**
   * 
   * @return the number of bytes that can still be read and rewound.
   */
  public int getCurrentRewindableCapacity() {
    return buffer.length - pos;
  }
}
