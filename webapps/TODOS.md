# TODOs

This file is aimed to list __little__ things to be done, glitches to be fixed
and/or problems we are aware of.

The "_real_" issue queue for this project is the
[camunda Jira](https://app.camunda.com/jira/issues/?jql=project%20%3D%20CAM%20AND%20resolution%20%3D%20Unresolved%20AND%20fixVersion%20%3D%20%227.2.0%22%20AND%20component%20%3D%20tasklist%20AND%20text%20~%20%22tasklist%22%20ORDER%20BY%20assignee%20ASC%2C%20priority%20DESC)


 - [ ] favicon rendering (probably mime type)
 - [ ] fonts rendering in IE (mime type / headers /redirection)
 - [ ] fonts rendering in firefox (headers)
 - [x] problem with "mocks" build
 - __Process start form__
   - [ ] text rendering in process variable type select (only some backends)
   - [x] piles disappear when modal opens
   - [x] processes list replaced by search results
   - [x] sort the processes by name
   - [ ] validate the variable according to its type
   - [x] could use a cancel button at the bottom
   - [x] should notify when process successfully starts
   - [ ] processes start access
   - [ ] add pager
   - [ ] search for process provides results
 - __User authentication__
   - [ ] logout from other app does not reflect on the tasklist
   - [ ] notification messages at logout
   - [ ] add tabindex to the login button
   - [ ] link to other apps does not match access rights
 - __Tests__
  - __Integration__
    - [ ] environmentSpec does not pass (although it actually should)
 - __Docs__
  - [ ] find a solution to document angular concept implementations (directives, services, ...)
