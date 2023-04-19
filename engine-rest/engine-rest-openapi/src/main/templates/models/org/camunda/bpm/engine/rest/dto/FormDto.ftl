<#macro dto_macro docsUrl="">
{
  "title": "FormDto",
  "type": "object",
  "properties": {

    <@lib.property
        name = "key"
        type = "string"
        desc = "The form key." />

    <@lib.property
        name = "camundaFormRef"
        type = "ref"
        dto = "CamundaFormRef"
        desc = "A reference to a specific version of a Camunda Form." />

    <@lib.property
        name = "contextPath"
        type = "string"
        last = true
        desc = "The context path of the process application. If the task (or the process definition) does not
                belong to a process application deployment or a process definition at all, this
                property is not set." />

  }
}
</#macro>
