module.exports = function(config, lessConfig, pathConfig) {
  'use strict';
  var resolve = require('path').resolve;

  var file = {};
  if(pathConfig.plugin) {
    file[pathConfig.buildTarget+'/plugin.css'] = pathConfig.sourceDir + '/styles.less';
  } else {
    file[pathConfig.buildTarget+'/styles/styles.css'] = pathConfig.sourceDir + '/styles/styles.less';
  }
  if (pathConfig.appName === 'cockpit' && !pathConfig.plugin) {
    file[pathConfig.buildTarget+'/styles/styles-components.css'] = pathConfig.sourceDir + '/styles/styles-components.less';
  }

  var includePaths = [
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/node_modules/bootstrap/less'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/lib/widgets'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/resources/less'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/resources/css'),
    resolve(process.cwd(), '<%= pkg.gruntConfig.commonsUiDir %>/node_modules'),
    resolve(process.cwd(), 'ui/common/styles'),
    resolve(process.cwd(), 'ui/' + pathConfig.appName, 'styles'),
    resolve(process.cwd(), 'ui/' + pathConfig.appName, 'client/scripts'),
    resolve(process.cwd(), 'node_modules/camunda-bpm-webapp/ui/common/styles'),
    resolve(process.cwd(), 'node_modules/camunda-bpm-webapp/ui/' + pathConfig.appName, 'styles'),
    resolve(process.cwd(), 'node_modules/camunda-bpm-webapp/ui/' + pathConfig.appName, 'client/scripts'),
  ];

  lessConfig[pathConfig.appName + (pathConfig.plugin ? '_plugin' : '') + '_styles'] = {
    options: {
      paths: includePaths,

      dumpLineNumbers: '<%= buildMode === "prod" ? "" : "comments" %>',
      compress: '<%= buildMode === "prod" ? "true" : "" %>',
      sourceMap: '<%= buildMode === "prod" ? "true" : "" %>',

      sourceMapURL: './styles.css.map',
      sourceMapFilename: pathConfig.plugin ? pathConfig.buildTarget+'/plugin.css.map' : pathConfig.buildTarget + '/styles/styles.css.map'
    },
    files: file
  };

};
