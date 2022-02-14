<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "setIncidentAnnotation"
      tag = "Incident"
      summary = "Set Incident Annotation"
      desc = "Sets the annotation of an incident with given id." />

  "parameters": [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the incident to clear the annotation at." />
  ],
  <@lib.requestBody
      mediaType = "application/json"
      dto = "AnnotationDto"
      examples = [
                  '"example-1": {
                     "summary": "PUT `/incident/7c80cc8f-ef95-11e6-b6e6-34f39ab71d4b/annotation`",
                     "value": {
                                "annotation": "my annotation"
                              }
                   }'
                ] />
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