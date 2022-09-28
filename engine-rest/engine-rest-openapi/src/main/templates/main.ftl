<#import "/lib/utils.ftl" as lib>

<#assign docsUrl = "https://docs.camunda.org/manual/${docsVersion}">
{
  "openapi": "3.0.2",
  "info": {
    "title": "Camunda Platform REST API",
    "description": "OpenApi Spec for Camunda Platform REST API.",
    "version": "${cambpmVersion}",
    "license": {
      "name": "Apache License 2.0",
      "url": "http://www.apache.org/licenses/LICENSE-2.0.html"
    }
  },
  "externalDocs": {
    "description": "Find out more about Camunda Rest API",
    "url": "${docsUrl}/reference/rest/overview/"
  },
  "servers": [

  <@lib.server
      url = "http://{host}:{port}/{contextPath}"
      variables = {"host": "localhost", "port": "8080", "contextPath": "engine-rest"}
      desc = "The API server for the default process engine" />

  <@lib.server
      url = "http://{host}:{port}/{contextPath}/engine/{engineName}"
      variables = {"host": "localhost", "port": "8080", "contextPath": "engine-rest", "engineName": "default"}
      desc = "The API server for a named process engine"/>

  <@lib.server
      url = "{url}"
      variables = {"url": ""}
      desc = "The API server with a custom url"
      last = true />

  ],
  "tags": [
    {"name": "Authorization"},
    {"name": "Batch"},
    {"name": "Condition"},
    {"name": "Decision Definition"},
    {"name": "Decision Requirements Definition"},
    {"name": "Deployment"},
    {"name": "Engine"},
    {"name": "Event Subscription"},
    {"name": "Execution"},
    {"name": "External Task"},
    {"name": "Filter"},
    {"name": "Group"},
    {"name": "Historic Activity Instance"},
    {"name": "Historic Batch"},
    {"name": "Historic Decision Definition"},
    {"name": "Historic Decision Instance"},
    {"name": "Historic Decision Requirements Definition"},
    {"name": "Historic Detail"},
    {"name": "Historic External Task Log"},
    {"name": "Historic Identity Link Log"},
    {"name": "Historic Incident"},
    {"name": "Historic Job Log"},
    {"name": "Historic Process Definition"},
    {"name": "Historic Process Instance"},
    {"name": "Historic Task Instance"},
    {"name": "Historic User Operation Log"},
    {"name": "Historic Variable Instance"},
    {"name": "History Cleanup"},
    {"name": "Identity"},
    {"name": "Incident"},
    {"name": "Job"},
    {"name": "Job Definition"},
    {"name": "Message"},
    {"name": "Metrics"},
    {"name": "Migration"},
    {"name": "Modification"},
    {"name": "Process Definition"},
    {"name": "Process Instance"},
    {"name": "Signal"},
    {"name": "Schema Log"},
    {"name": "Task"},
    {"name": "Task Attachment"},
    {"name": "Task Comment"},
    {"name": "Task Identity Link"},
    {"name": "Task Local Variable"},
    {"name": "Task Variable"},
    {"name": "Telemetry"},
    {"name": "Tenant"},
    {"name": "User"},
    {"name": "Variable Instance"},
    {"name": "Version"}
  ],
  "paths": {

    <#list endpoints as path, methods>
        "${path}": {
            <#list methods as method>
                <#import "/paths${path}/${method}.ftl" as endpoint>
                "${method}":
                <@endpoint.endpoint_macro docsUrl=docsUrl/><#sep>,
            </#list>
        }<#sep>,
    </#list>

  },
  "security": [ {"basicAuth": []} ],
  "components": {
    "securitySchemes": {
      "basicAuth": {
        "type": "http",
        "scheme": "basic"
      }
    },
    "schemas": {

    <#list models as name, package>
        <#import "/models/${package}/${name}.ftl" as schema>
        "${name}": <@schema.dto_macro docsUrl=docsUrl/><#sep>,
    </#list>

    }
  }
}
