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
package org.camunda.bpm.engine.test.standalone.variables;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.camunda.bpm.engine.impl.variable.serializer.FileValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.type.FileValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ronny Bräunlich
 *
 */
public class FileValueSerializerTest {

  private static final String SEPARATOR = "#";
  private FileValueSerializer serializer;

  @Before
  public void setUp() {
    serializer = new FileValueSerializer();
  }

  @Test
  public void testTypeIsFileValueType() {
    assertThat(serializer.getType(), is(instanceOf(FileValueTypeImpl.class)));
  }

  @Test
  public void testWriteFilenameOnlyValue() {
    String filename = "test.txt";
    FileValue fileValue = Variables.fileValue(filename).create();
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(valueFields.getByteArrayValue(), is(nullValue()));
    assertThat(valueFields.getTextValue(), is(filename));
    assertThat(valueFields.getTextValue2(), is(nullValue()));
  }

  @Test
  public void testWriteMimetypeAndFilenameValue() {
    String filename = "test.txt";
    String mimeType = "text/json";
    FileValue fileValue = Variables.fileValue(filename).mimeType(mimeType).create();
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(valueFields.getByteArrayValue(), is(nullValue()));
    assertThat(valueFields.getTextValue(), is(filename));
    assertThat(valueFields.getTextValue2(), is(mimeType + SEPARATOR));
  }

  @Test
  public void testWriteMimetypeFilenameAndBytesValue() throws UnsupportedEncodingException {
    String filename = "test.txt";
    String mimeType = "text/json";
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    FileValue fileValue = Variables.fileValue(filename).mimeType(mimeType).file(is).create();
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(new String(valueFields.getByteArrayValue(), "UTF-8"), is("text"));
    assertThat(valueFields.getTextValue(), is(filename));
    assertThat(valueFields.getTextValue2(), is(mimeType + SEPARATOR));
  }

  @Test
  public void testWriteMimetypeFilenameBytesValueAndEncoding() throws UnsupportedEncodingException {
    String filename = "test.txt";
    String mimeType = "text/json";
    Charset encoding = Charset.forName("UTF-8");
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    FileValue fileValue = Variables.fileValue(filename).mimeType(mimeType).encoding(encoding).file(is).create();
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(new String(valueFields.getByteArrayValue(), "UTF-8"), is("text"));
    assertThat(valueFields.getTextValue(), is(filename));
    assertThat(valueFields.getTextValue2(), is(mimeType + SEPARATOR + encoding.name()));
  }

  @Test
  public void testWriteMimetypeFilenameAndBytesValueWithShortcutMethod() throws URISyntaxException, UnsupportedEncodingException {
    File file = new File(this.getClass().getClassLoader().getResource("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt").toURI());
    FileValue fileValue = Variables.fileValue(file);
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(new String(valueFields.getByteArrayValue(), "UTF-8"), is("text"));
    assertThat(valueFields.getTextValue(), is("simpleFile.txt"));
    assertThat(valueFields.getTextValue2(), is("text/plain" + SEPARATOR));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testThrowsExceptionWhenConvertingUnknownUntypedValueToTypedValue() {
    serializer.convertToTypedValue((UntypedValueImpl) Variables.untypedValue(new Object()));
  }

  @Test
  public void testReadFileNameMimeTypeAndByteArray() throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    byte[] data = new byte[is.available()];
    DataInputStream dataInputStream = new DataInputStream(is);
    dataInputStream.readFully(data);
    dataInputStream.close();
    MockValueFields valueFields = new MockValueFields();
    String filename = "file.txt";
    valueFields.setTextValue(filename);
    valueFields.setByteArrayValue(data);
    String mimeType = "text/plain";
    valueFields.setTextValue2(mimeType + SEPARATOR);

    FileValue fileValue = serializer.readValue(valueFields, true);

    assertThat(fileValue.getFilename(), is(filename));
    assertThat(fileValue.getMimeType(), is(mimeType));
    checkStreamFromValue(fileValue, "text");
  }

  @Test
  public void testReadFileNameEncodingAndByteArray() throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    byte[] data = new byte[is.available()];
    DataInputStream dataInputStream = new DataInputStream(is);
    dataInputStream.readFully(data);
    dataInputStream.close();
    MockValueFields valueFields = new MockValueFields();
    String filename = "file.txt";
    valueFields.setTextValue(filename);
    valueFields.setByteArrayValue(data);
    String encoding = SEPARATOR + "UTF-8";
    valueFields.setTextValue2(encoding);

    FileValue fileValue = serializer.readValue(valueFields, true);

    assertThat(fileValue.getFilename(), is(filename));
    assertThat(fileValue.getEncoding(), is("UTF-8"));
    assertThat(fileValue.getEncodingAsCharset(), is(Charset.forName("UTF-8")));
    checkStreamFromValue(fileValue, "text");
  }

  @Test
  public void testReadFullValue() throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    byte[] data = new byte[is.available()];
    DataInputStream dataInputStream = new DataInputStream(is);
    dataInputStream.readFully(data);
    dataInputStream.close();
    MockValueFields valueFields = new MockValueFields();
    String filename = "file.txt";
    valueFields.setTextValue(filename);
    valueFields.setByteArrayValue(data);
    String mimeType = "text/plain";
    String encoding = "UTF-16";
    valueFields.setTextValue2(mimeType + SEPARATOR + encoding);

    FileValue fileValue = serializer.readValue(valueFields, true);

    assertThat(fileValue.getFilename(), is(filename));
    assertThat(fileValue.getMimeType(), is(mimeType));
    assertThat(fileValue.getEncoding(), is("UTF-16"));
    assertThat(fileValue.getEncodingAsCharset(), is(Charset.forName("UTF-16")));
    checkStreamFromValue(fileValue, "text");
  }

  @Test
  public void testReadFilenameAndByteArrayValue() throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/engine/test/standalone/variables/simpleFile.txt");
    byte[] data = new byte[is.available()];
    DataInputStream dataInputStream = new DataInputStream(is);
    dataInputStream.readFully(data);
    dataInputStream.close();
    MockValueFields valueFields = new MockValueFields();
    String filename = "file.txt";
    valueFields.setTextValue(filename);
    valueFields.setByteArrayValue(data);

    FileValue fileValue = serializer.readValue(valueFields, true);

    assertThat(fileValue.getFilename(), is(filename));
    assertThat(fileValue.getMimeType(), is(nullValue()));
    checkStreamFromValue(fileValue, "text");
  }

  @Test
  public void testReadFilenameValue() throws IOException {
    MockValueFields valueFields = new MockValueFields();
    String filename = "file.txt";
    valueFields.setTextValue(filename);

    FileValue fileValue = serializer.readValue(valueFields, true);

    assertThat(fileValue.getFilename(), is(filename));
    assertThat(fileValue.getMimeType(), is(nullValue()));
    assertThat(fileValue.getValue(), is(nullValue()));
  }

  @Test
  public void testNameIsFile() {
    assertThat(serializer.getName(), is("file"));
  }

  @Test
  public void testWriteFilenameAndEncodingValue() {
    String filename = "test.txt";
    String encoding = "UTF-8";
    FileValue fileValue = Variables.fileValue(filename).encoding(encoding).create();
    ValueFields valueFields = new MockValueFields();

    serializer.writeValue(fileValue, valueFields);

    assertThat(valueFields.getByteArrayValue(), is(nullValue()));
    assertThat(valueFields.getTextValue(), is(filename));
    assertThat(valueFields.getTextValue2(), is(SEPARATOR + encoding));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSerializeFileValueWithoutName() {
    Variables.fileValue((String) null).file("abc".getBytes()).create();
  }

  private void checkStreamFromValue(TypedValue value, String expected) {
    InputStream stream = (InputStream) value.getValue();
    Scanner scanner = new Scanner(stream);
    assertThat(scanner.nextLine(), is(expected));
  }

  private static class MockValueFields implements ValueFields {

    private String name;
    private String textValue;
    private String textValue2;
    private Long longValue;
    private Double doubleValue;
    private byte[] bytes;

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getTextValue() {
      return textValue;
    }

    @Override
    public void setTextValue(String textValue) {
      this.textValue = textValue;
    }

    @Override
    public String getTextValue2() {
      return textValue2;
    }

    @Override
    public void setTextValue2(String textValue2) {
      this.textValue2 = textValue2;
    }

    @Override
    public Long getLongValue() {
      return longValue;
    }

    @Override
    public void setLongValue(Long longValue) {
      this.longValue = longValue;
    }

    @Override
    public Double getDoubleValue() {
      return doubleValue;
    }

    @Override
    public void setDoubleValue(Double doubleValue) {
      this.doubleValue = doubleValue;
    }

    @Override
    public byte[] getByteArrayValue() {
      return bytes;
    }

    @Override
    public void setByteArrayValue(byte[] bytes) {
      this.bytes = bytes;
    }

  }
}
