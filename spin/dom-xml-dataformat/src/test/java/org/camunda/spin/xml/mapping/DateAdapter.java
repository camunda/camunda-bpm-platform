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
package org.camunda.spin.xml.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stefan Hentschel.
 */
public class DateAdapter extends XmlAdapter<String, Date> {

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

  @Override
  public Date unmarshal(String v) throws Exception {
    return dateFormat.parse(v);
  }

  @Override
  public String marshal(Date v) throws Exception {
    return dateFormat.format(v);
  }
}
