  "/process-instance": {
    "get":
    <#include "/paths/process-instance/get.ftl">,
    "post":
    <#include "/paths/process-instance/post.ftl">
  },
  "/process-instance/{id}": {
    "delete":
    <#include "/paths/process-instance/{id}/delete.ftl">
  },
  "/process-instance/{id}/activity-instances": {
    "get":
     <#include "/paths/process-instance/{id}/activity-instances/get.ftl">
  },
  "/process-instance/{id}/modification": {
    "post":
     <#include "/paths/process-instance/{id}/modification/post.ftl">
  },
  "/process-instance/{id}/modification-async": {
    "post":
     <#include "/paths/process-instance/{id}/modification-async/post.ftl">
  },
  "/process-instance/{id}/suspended": {
    "put":
    <#include "/paths/process-instance/{id}/suspended/put.ftl">
  },
  "/process-instance/{id}/variables": {
    "get":
    <#include "/paths/process-instance/{id}/variables/get.ftl">,
    "post":
    <#include "/paths/process-instance/{id}/variables/post.ftl">
  },
  "/process-instance/{id}/variables/{varName}": {
    "get":
    <#include "/paths/process-instance/{id}/variables/{varName}/get.ftl">,
    "put":
    <#include "/paths/process-instance/{id}/variables/{varName}/put.ftl">,
    "delete":
    <#include "/paths/process-instance/{id}/variables/{varName}/delete.ftl">
  },
  "/process-instance/{id}/variables/{varName}/data": {
    "get":
    <#include "/paths/process-instance/{id}/variables/{varName}/data/get.ftl">,
    "post":
    <#include "/paths/process-instance/{id}/variables/{varName}/data/post.ftl">
  },
  "/process-instance/count": {
    "get":
    <#include "/paths/process-instance/count/get.ftl">,
    "post":
    <#include "/paths/process-instance/count/post.ftl">
  },
  "/process-instance/delete": {
    "post":
    <#include "/paths/process-instance/delete/post.ftl">
  },
  "/process-instance/delete-historic-query-based": {
    "post":
    <#include "/paths/process-instance/delete-historic-query-based/post.ftl">
  },
  "/process-instance/job-retries": {
    "post":
    <#include "/paths/process-instance/job-retries/post.ftl">
  },
  "/process-instance/job-retries-historic-query-based": {
    "post":
    <#include "/paths/process-instance/job-retries-historic-query-based/post.ftl">
  },
  "/process-instance/suspended": {
    "put":
    <#include "/paths/process-instance/suspended/put.ftl">
  },
  "/process-instance/suspended-async": {
    "post":
    <#include "/paths/process-instance/suspended-async/post.ftl">
  }