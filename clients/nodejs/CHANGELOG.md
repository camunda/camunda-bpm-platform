# Changelog

## 3.1.0
### Features

- Update dependencies ([#3698](https://github.com/camunda/camunda-bpm-platform/issues/3698),[#3906](https://github.com/camunda/camunda-bpm-platform/issues/3906))
- Add sort by create time capability ([#3928](https://github.com/camunda/camunda-bpm-platform/issues/3928))

## 3.0.1
### Features
- Update got package ([#3135](https://github.com/camunda/camunda-bpm-platform/issues/3135))
- Rethrow errors when calling TaskService api ([#3222](https://github.com/camunda/camunda-bpm-platform/issues/3222))

## 3.0.0
### Features
- Bump json5 from 2.2.1 to 2.2.3 ([#3091](https://github.com/camunda/camunda-bpm-platform/issues/3091))

## 3.0.0-alpha1
### Features
- Add extension property support ([#264](https://github.com/camunda/camunda-external-task-client-js/pull/264))
- Update dependencies to latest version & update project to ECMAScript modules ([#265](https://github.com/camunda/camunda-external-task-client-js/pull/265))
- Update path-parse dependency ([#268](https://github.com/camunda/camunda-external-task-client-js/pull/268))

## 2.3.0
### Features
- Expose exception error code ([#257](https://github.com/camunda/camunda-external-task-client-js/pull/257))

## 2.2.0
### Features
- Support setting transient variables via API ([#244](https://github.com/camunda/camunda-external-task-client-js/pull/244))

## 2.1.1
### Bug Fixes
- Fix loading file variables ([#208](https://github.com/camunda/camunda-external-task-client-js/pull/208))

### Dependency Updates
- Bump lodash to version 4.17.21
- Bump ws to 5.2.3
- Bump normalize-url to 4.5.1
- Bump y18n to 3.2.2

## 2.1.0
### Features
- Allow manual locking of a Task

## 2.0.0
### Features
- Support for Keycloak auth secured rest API

### Deprecations
- Removed support for Node v8 and v9. Please use node version 10 or higher.

## 1.3.1
### Features
- support localVariables when fetching Tasks

### Changes to the logging behavior
- Not every Action will be logged by default. For example, polling will no longer be logged if it is successful. 
You can define the log level as described in the [docs](https://github.com/camunda/camunda-external-task-client-js/blob/master/docs/logger.md#loggerlevelloglevel). To emulate >=1.3.0 bahaviour, use `logger.level('debug')`

## 1.3.0
### Features
- Use priority when fetching Tasks
- Filter tasks by version tag

## 1.2.0
### Features
- Set maximum number of executed tasks using `maxParallelExecutions`

## 1.1.1
### Features
- Filter tasks by tenant
- Filter tasks by process definition

## 1.1.0-alpha1
### Features
- Make it possible to pass error message and variables when handling a bpmn error.

## 1.0.0
### Features
- Filter tasks by business key

### Bug Fixes
- Setting typed date variable with a string value causes serialization issue

## 0.2.0
### Features
- Setting Local Variables
- Support for File & Date Variables

## 0.1.1

### Features
- Exchange Process Variables

## 0.1.0

### Features
- Fetch and Lock
- Complete
- Handle Failure
- Handle BPMN Error
- Extend Lock
- Unlock
