<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "clearIncidentAnnotation"
      tag = "Incident"
      summary = "Clear Incident Annotation"
      desc = "Clears the annotation of an incident with given id." />

  "parameters": [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the incident to clear the annotation at." />
  ],
  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if no incident can be found for the given id." />
    }
}

</#macro>