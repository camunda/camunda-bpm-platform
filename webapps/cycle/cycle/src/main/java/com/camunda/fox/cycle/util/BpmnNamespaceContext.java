/**
 * Copyright (C) 2011 camunda services GmbH (www.camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.camunda.fox.cycle.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;

public class BpmnNamespaceContext implements javax.xml.namespace.NamespaceContext {
  
  public static final String BPMN = "bpmn";
  public static final String BPMNDI = "bpmndi";
  public static final String OMGDC = "omgdc";
  public static final String OMGDI = "omgdi";
  
  public static final String BPMN_NAMESPACE_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String BPMN_DI_NAMESPACE_URI = "http://www.omg.org/spec/BPMN/20100524/DI";
  public static final String OMG_DC_NAMESPACE_URI = "http://www.omg.org/spec/DD/20100524/DC";
  public static final String OMG_DI_NAMESPACE_URI = "http://www.omg.org/spec/DD/20100524/DI";
  
  protected Map<String,String> namespaces = new HashMap<String, String>();
  
  public BpmnNamespaceContext() {
    namespaces.put(BPMN, BPMN_NAMESPACE_URI);
    namespaces.put(BPMNDI, BPMN_DI_NAMESPACE_URI);
    namespaces.put(OMGDC, OMG_DC_NAMESPACE_URI);
    namespaces.put(OMGDI, OMG_DI_NAMESPACE_URI);
  }
  
  public String getNamespaceURI(String prefix) {
		if (prefix.equals(BPMN)){
			return BPMN_NAMESPACE_URI;
		}
		else if(prefix.equals(BPMNDI)){
			return BPMN_DI_NAMESPACE_URI;
		}
		else if(prefix.equals(OMGDC)){
			return OMG_DC_NAMESPACE_URI;
		}
		else if(prefix.equals(OMGDI)){
			return OMG_DI_NAMESPACE_URI;
		}
		else{
			return XMLConstants.NULL_NS_URI;
		}
	}

	public String getPrefix(String namespace) {
		if (namespace.equals(BPMN_NAMESPACE_URI)){
			return BPMN;
		}
		else if(namespace.equals(BPMN_DI_NAMESPACE_URI)){
			return BPMNDI;
		}
		else if(namespace.equals(OMG_DC_NAMESPACE_URI)){
			return OMGDC;
		}
		else if(namespace.equals(OMG_DI_NAMESPACE_URI)){
			return OMGDI;
		}
		else{
			return null;
		}
	}

	public Iterator getPrefixes(String namespace) {
		return null;
	}
	
  public Map<String, String> getNamespaces() {
    return namespaces;
  }
}
