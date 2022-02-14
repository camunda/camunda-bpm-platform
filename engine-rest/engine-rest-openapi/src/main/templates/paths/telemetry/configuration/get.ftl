<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getTelemetryConfiguration"
      tag = "Telemetry"
      summary = "Fetch Telemetry Configuration"
      desc = "Fetches Telemetry Configuration." />

  "parameters" : [],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "TelemetryConfigurationDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "The Response content of a status 200",
                       "value": {
                           "enableTelemetry": true
                         }
                     }'] />

    <@lib.response
        code = "401"
        dto = "ExceptionDto"
        last = true
        desc = "If the user who perform the operation is not a <b>camunda-admin</b> user." />


  }
}
</#macro>