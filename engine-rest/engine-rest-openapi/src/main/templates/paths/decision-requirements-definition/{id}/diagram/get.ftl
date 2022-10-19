<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-diagram/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitionDiagramById"
      tag = "Decision Requirements Definition"
      summary = "Get Decision Requirements Diagram by ID"
      desc = "Retrieves the diagram of a decision requirements definition."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the decision requirements definition."
          last = true
      />

  ],

  "responses": {

    <@lib.multiTypeResponse
        code = "200"
        types = [
          {
            "binary": true,
            "mediaType": "image/*",
            "examples": ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/diagram`",
                       "description": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/diagram`",
                       "value": ""
                     }']
          },
          {
            "binary": true,
            "mediaType": "application/octet-stream",
            "examples": ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/diagram`",
                       "description": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c/diagram`",
                       "value": ""
                     }']
          }
        ]
        desc = "The image diagram of the decision requirements definition."
        
    />

    <@lib.response
        code = "204"
        desc = "The decision requirements definition doesn't have an associated diagram. This
                method returns no content."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Decision requirements definition with given id or key does not
                exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>