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
package org.camunda.bpm.container.impl.metadata;

import org.camunda.bpm.application.impl.metadata.ProcessesXmlParse;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.util.xml.Parser;


/**
 * <p>A SAX Parser for the bpm-platform.xml file</p>
 *
 * @author Daniel Meyer
 *
 */
public class BpmPlatformXmlParser extends Parser {

  /**
   * The bpm platform namespace
   */
  public static final String BPM_PLATFORM_NS = "http://www.camunda.org/schema/1.0/BpmPlatform";

  /**
   * The location of the XSD file in the classpath.
   */
  public static final String BPM_PLATFORM_XSD = "BpmPlatform.xsd";

  /**
   * create an configure the {@link ProcessesXmlParse} object.
   */
  public BpmPlatformXmlParse createParse() {
    BpmPlatformXmlParse parse = new BpmPlatformXmlParse(this);
    parse.setSchemaResource(ReflectUtil.getResourceUrlAsString(BPM_PLATFORM_XSD));
    return parse;
  }

}
