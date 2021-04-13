<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "resolveIncident"
      tag = "Incident"
      summary = "Resolve Incident"
      desc = "Resolves an incident with given id." />

  "parameters": [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the incident to be resolved." />
  ],
  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Returned if an incident with given id does not exist." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if an incident is not related to any execution or an incident is of type `failedJob` or
               `failedExternalTask`. To resolve such an incident, please refer to the
               [Incident Types](${docsUrl}/user-guide/process-engine/incidents/#incident-types) section." />
    }
}

</#macro>