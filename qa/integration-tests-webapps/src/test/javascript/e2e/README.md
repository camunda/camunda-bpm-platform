# Protractor e2e test development

### Debuggin protractor tests in Intellij

1. Install Node.js plugin for Intellij
2. Add a new debug configuration for Node.js
3. Enter the path of the node installation in the field `Node Interpreter`.
4. Enter the path of the camunda BPM platform as `Working Directory`.
5. Enter the path of the protractor.cli file as `JavaScript File`. "..\webapp\node_modules\grunt-protractor-runner\node_modules\protractor\lib\cli.js".
6. Enter the path of the protractor configuration file as `Application Parameter`. For development purpose choose the `develop.conf.js` file.

The `develop.conf.js` configuration file is only for development purpose and has to be adjusted according to the desired webapp:

```
specs: ['WEBAPP/*Spec.js'],
```