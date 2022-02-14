<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getSchemaLog"
      tag = "Schema Log"
      summary = "Get List"
      desc = "Queries for schema log entries that fulfill given parameters." />

  "parameters" : [
    <@lib.parameter
        name = "version"
        location = "query"
        type = "string"
        desc = "Only return schema log entries with a specific version."/>

    <#assign sortByValues = [
      '"timestamp"'
    ]>
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "SchemaLogEntryDto"
        array = true
        last = true
        desc = "Request successful.
                **Note**: In order to get any results a user of group `camunda-admin` must
                be authenticated."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "The Response content of a status 200",
                       "value": [
                         {
                           "id": "0",
                           "version": "7.11.0",
                           "timestamp": "2019-05-13T09:07:11.751+0200"
                         },
                         {
                           "id": "1",
                           "version": "7.11.1",
                           "timestamp": "2019-06-1T17:22:05.123+0200"
                         }
                       ]
                     }'] />
  }
}

</#macro>