module.exports = function(config, lessConfig, pathConfig) {
  'use strict';
  var resolve = require('path').resolve;

  var file = {};
  file[pathConfig.buildTarget+'/styles/styles.css'] = pathConfig.sourceDir + '/styles/styles.less';

  var includePaths = [
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/node_modules/bootstrap/less'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/lib/widgets'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/resources/less'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/resources/css'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/node_modules'),
    resolve(process.cwd(), 'ui/' + pathConfig.appName, 'styles'),
    resolve(process.cwd(), 'ui/' + pathConfig.appName, 'client/scripts'),
    resolve(process.cwd(), 'node_modules/camunda-bpm-webapp/ui/' + pathConfig.appName, 'styles'),
    resolve(process.cwd(), 'node_modules/camunda-bpm-webapp/ui/' + pathConfig.appName, 'client/scripts'),

    // resolve(__dirname, '../../..', 'camunda-bpm-webapp/webapp/src/main/resources-plugin'),
    // resolve(__dirname, '../../..', 'camunda-bpm-platform-ee/webapps/camunda-webapp/plugins/src/main/resources-plugin')
  ];

  lessConfig[pathConfig.appName + '_styles'] = {
    options: {
      paths: includePaths,


      dumpLineNumbers: '<%= buildMode === "prod" ? "" : "comments" %>',
      compress: '<%= buildMode === "prod" ? "true" : "" %>',
      sourceMap: '<%= buildMode === "prod" ? "true" : "" %>',

      sourceMapURL: './styles.css.map',
      sourceMapFilename: pathConfig.buildTarget + '/styles/styles.css.map'
    },
    files: file
  };

};
