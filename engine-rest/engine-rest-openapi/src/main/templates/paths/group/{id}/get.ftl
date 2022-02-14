<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getGroup"
      tag = "Group"
      summary = "Get Group"
      desc = "Retrieves a group by id." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the group to be retrieved."/>
  ],

  "responses" : {

     <@lib.response
        code = "200"
        dto = "GroupDto"
        desc = "Request successful."
        examples = ['"example-1": {
                          "summary": "Status 200.",
                          "description": "GET `/group/sales`",
                          "value": {
                            "id": "sales",
                            "name": "Sales",
                            "type": "Organizational Unit"
                          }
                        }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Group with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>