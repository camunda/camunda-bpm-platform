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
package org.camunda.bpm.engine.rest.mapper;

import org.apache.commons.fileupload.FileItemStream;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.exception.RestException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Custom implementation of Multipart Form Data which can be used for handling requests.
 *<p>
 * Provides access to the form parts via {@link #getNamedPart(String)}.
 *
 * @author Daniel Meyer
 *
 */
public class MultipartFormData {

  protected Map<String, FormPart> formParts = new HashMap<String, FormPart>();

  public void addPart(FormPart formPart) {
    formParts.put(formPart.getFieldName(), formPart);
  }

  public FormPart getNamedPart(String name) {
    return  formParts.get(name);
  }

  public Set<String> getPartNames() {
    return formParts.keySet();
  }

  /**
   * Dto representing a part in a multipart form.
   *
   */
  public static class FormPart {

    protected String fieldName;
    protected String contentType;
    protected String textContent;
    protected String fileName;
    protected byte[] binaryContent;

    public FormPart(FileItemStream stream) {
      fieldName = stream.getFieldName();
      contentType = stream.getContentType();
      binaryContent = readBinaryContent(stream);
      fileName = stream.getName();

      if(contentType == null || contentType.contains(MediaType.TEXT_PLAIN)) {
        textContent = new String(binaryContent);
      }
    }

    public FormPart() {
    }

    protected byte[] readBinaryContent(FileItemStream stream) {
      InputStream inputStream = getInputStream(stream);
      return IoUtil.readInputStream(inputStream, stream.getFieldName());
    }

    protected InputStream getInputStream(FileItemStream stream) {
      try {
        return stream.openStream();
      } catch (IOException e) {
        throw new RestException(Status.INTERNAL_SERVER_ERROR, e);
      }
    }

    public String getFieldName() {
      return fieldName;
    }

    public String getContentType() {
      return contentType;
    }

    public String getTextContent() {
      return textContent;
    }

    public byte[] getBinaryContent() {
      return binaryContent;
    }

    public String getFileName() {
      return fileName;
    }

  }

}
