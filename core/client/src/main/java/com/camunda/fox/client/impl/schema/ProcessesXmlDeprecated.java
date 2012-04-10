/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Daniel Meyer
 */
@XmlRootElement(name = "processArchive")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessesXmlDeprecated {

  public String name = null;

  @XmlElement(name = "configuration")
  public Configuration configuration = new Configuration();

  @XmlElements(@XmlElement(name = "process", type = Process.class))
  public List<Process> processes = new ArrayList<ProcessesXmlDeprecated.Process>();

  public static class Configuration {

    @XmlElement(name = "undeployment")
    public Undeployment undeployment = new Undeployment();

    public static class Undeployment {

      @XmlAttribute(required = true)
      public boolean delete = false;

    }
  }

  public static class Process {

    @XmlAttribute(required = true)
    public String resourceName;
  }

}
