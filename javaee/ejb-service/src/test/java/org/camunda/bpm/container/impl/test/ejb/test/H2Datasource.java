package org.camunda.bpm.container.impl.test.ejb.test;

import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;

@DataSourceDefinitions({
 @DataSourceDefinition(
   name="java:global/test/FoxEngine",
   className="org.h2.Driver",
   url="jdbc:h2:mem:foxEngine;DB_CLOSE_DELAY=-1;MVCC=TRUE",  
   user="sa",
   password="sa")
})
public class H2Datasource {

}
