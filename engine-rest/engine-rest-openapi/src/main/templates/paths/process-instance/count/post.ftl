{
  "operationId" : "queryProcessInstancesCount",
  "description": "Queries for the number of process instances that fulfill the given parameters.
                  This method takes the same message body as the Get Instances (POST) method and
                  therefore it is slightly more powerful than the Get Instance Count method.",
  "tags": [
    "Process instance"
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceQueryDto" />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if an invalid operator for variable comparison is used."/>

  }
}