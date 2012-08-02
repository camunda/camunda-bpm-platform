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
package com.camunda.fox.client.impl.extensions;

import com.camunda.fox.client.impl.ProcessArchiveImpl;
import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.platform.spi.ProcessArchive;

public class MockProcessArchiveSupport extends ProcessArchiveSupport {
  
  @Override
  protected void installProcessArchives() {   
        
    ProcessArchiveXml mockDescriptor1 = new ProcessArchiveXml();
    mockDescriptor1.name = "tenant1";
    ProcessArchiveXml mockDescriptor2 = new ProcessArchiveXml();
    mockDescriptor2.name = "tenant2";
        
    ProcessArchive mockArchive1 = new ProcessArchiveImpl(mockDescriptor1,null,null);
    ProcessArchive mockArchive2 = new ProcessArchiveImpl(mockDescriptor2,null,null);
    
    installedProcessArchives.put(mockArchive1, new MockProcessEngine("tenant1Engine"));
    installedProcessArchives.put(mockArchive2, new MockProcessEngine("tenant2Engine"));    
  }
  
  @Override
  protected void uninstallProcessArchives() {
     installedProcessArchives.clear();
  }

}
