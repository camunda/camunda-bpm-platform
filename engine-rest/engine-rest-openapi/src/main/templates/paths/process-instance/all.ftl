  "/process-instance/{id}": {
    "delete":
    <#include "/paths/process-instance/{id}/delete.ftl">
  },
  "/process-instance/count": {
    "get":
    <#include "/paths/process-instance/count/get.ftl">
  },
  "/process-instance/delete": {
    "post":
    <#include "/paths/process-instance/delete/post.ftl">
  }