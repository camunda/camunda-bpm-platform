<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getProcessEngineNames"
      tag = "Engine"
      summary = "Get List"
      desc = "Retrieves the names of all process engines available on your platform.
              **Note**: You cannot prepend `/engine/{name}` to this method." />

  "responses" : {
    <@lib.response
        code = "200"
        dto = "ProcessEngineDto"
        array = true
        last=true
        desc = "Request successful."
        examples = ['"example-1": {
                       "value": [
                         {
                           "name": "default"
                         },
                         {
                           "name": "anotherEngineName"
                         }
                       ]
                     }'] />
  }
}

</#macro>