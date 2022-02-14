<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/user-operation-log/clear-annotation/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "clearAnnotationUserOperationLog"
      tag = "Historic User Operation Log"
      summary = "Clear Annotation of an User Operation Log (Historic)"
      desc = "Clear the annotation which was previously set for auditing reasons."
  />

  "parameters" : [

      <@lib.parameter
          name = "operationId"
          location = "path"
          type = "string"
          required = true
          desc = "The operation id of the operation log to be updated."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
        examples = ['"example-1": {
                       "summary": "Status 204",
                       "description": "PUT `/history/user-operation/a02a5890-ad41-11e9-8609-c6bbb7c7e9e3/clear-annotation`",
                       "value": "No content."
                     }'
        ]
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the request parameters are invalid, for example if the
                `operationId` path parameter value does not exists. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>