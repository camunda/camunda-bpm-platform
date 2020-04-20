{

  <@lib.endpointInfo
      id = "getProcessDefinitionBpmn20Xml"
      tag = "Process Definition"
      desc = "Retrieves the BPMN 2.0 XML of a process definition." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition."/>
  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ProcessDefinitionDiagramDto"
        desc = "Request successful."
        examples = ['"example-1": {
                     "summary": "GET `/process-definition/id/aProcessDefinitionId/xml`",
                     "value": {
                       "id": "anProcessDefinitionId",
                       "bpmn20Xml": "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?>\n<definitions
                         xmlns=\\"http://www.omg.org/spec/BPMN/20100524/MODEL\\"
                         xmlns:camunda=\\"http://camunda.org/schema/1.0/bpmn\\"
                         targetNamespace=\\"Examples\\">
                         <process id=\\"oneTaskProcess\\" isExecutable=\\"true\\">
                           <startEvent id=\\"theStart\\" />
                           <sequenceFlow id=\\"flow1\\" sourceRef=\\"theStart\\" targetRef=\\"theEnd\\" />
                           <endEvent id=\\"theEnd\\" />
                         </process>
                       </definitions>"
                     }
                   }'
                 ] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
