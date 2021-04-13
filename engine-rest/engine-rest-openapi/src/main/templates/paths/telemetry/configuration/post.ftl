<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "configureTelemetry"
      tag = "Telemetry"
      summary = "Configure Telemetry"
      desc = "Configures whether Camunda receives data collection of the process engine setup and usage." />

  "parameters" : [],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TelemetryConfigurationDto"
      examples = ['"examle-1": {
                     "summary": "POST /telemetry/configuration",
                     "description": "The content of the Request Body",
                     "value": {
                         "enableTelemetry": true
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "401"
        dto = "ExceptionDto"
        last = true
        desc = "If the user who perform the operation is not a <b>camunda-admin</b> user." />
  }
}
</#macro>