{
  "operationId": "getProcessInstancesCount",
  "description": "Queries for the number of process instances that fulfill given parameters.",
  "tags": [
    "Process instance"
  ],
  "parameters": [
    <@lib.parameter name="businessKey" location="query" type="string" 
      description="Filter by process instance business key."/>,
    <@lib.parameter name="businessKeyLike" location="query" type="string" description="Filter by process instance business key that the parameter is a substring of."/>,
    <@lib.parameter name="deploymentId" location="query" type="string" description="Filter by the deployment the id belongs to."/>,
    <@lib.parameter name="processInstanceIds" location="query" type="string" required=false/>,
    <@lib.parameter name="active" location="query" type="boolean" typeDefault=true typeDefaultValue=false required=false/>
    <#-- TODO -->
  ],
  "responses": {
    <@lib.response responseCode="200" refDto="CountResultDto" desc="Request successful."/>,
    <@lib.response responseCode="400" refDto="ExceptionDto"
                   desc="Bad Request
Returned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy, or if an invalid operator for variable comparison is used."/>
  }
}