<#import "/lib/utils.ftl" as lib>
{
  "openapi": "3.0.2",
  "info": {
    "title": "Camunda BPM REST API",
    "description": "OpenApi Spec for Camunda BPM REST API.",
    "version": "${cambpmVersion}",
    "license": {
      "name": "Apache License 2.0",
      "url": "http://www.apache.org/licenses/LICENSE-2.0.html"
    }
  },
  "externalDocs": {
    "description": "Find out more about Camunda Rest API",
    "url": "https://docs.camunda.org/manual/${docsVersion}/reference/rest/overview/"
  },
  "servers": [

  <@lib.server
      url = "http://{host}:{port}/{contextPath}"
      variables = {"host": "localhost", "port": "8080", "contextPath": "engine-rest"}
      description = "The default API server" />

  <@lib.server
      url = "http://{host}:{port}/{contextPath}/engine/{engineName}"
      variables = {"host": "localhost", "port": "8080", "contextPath": "engine-rest", "engineName": "default"}
      description = "The Named Process Engine API server"
      last = true />

  ],
  "tags": [
    {"name": "Process instance"},
    {"name": "Deployment"}
  ],
  "paths": {

    <#list endpoints as path, methods>
        "${path}": {
            <#list methods as method>
                "${method}":
                <#include "/paths${path}/${method}.ftl"><#sep>,
            </#list>
        }<#sep>,
    </#list>

  },
  "components": {
    "schemas": {

    <#list models as name, package>
        "${name}": <#include "/models/${package}/${name}.ftl"><#sep>,
    </#list>

    }
  }
}
