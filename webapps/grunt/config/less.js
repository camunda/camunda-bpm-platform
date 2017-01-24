module.exports = function(config, lessConfig, pathConfig) {
  'use strict';

  var path = require('path');

  var file = {};
  var source = pathConfig.sourceDir + '/styles/styles.less';
  var destination = pathConfig.buildTarget+'/styles/styles.css';

  if(pathConfig.plugin) {
    source = pathConfig.sourceDir + '/styles.less';
    destination = pathConfig.buildTarget+'/plugin.css';
  }

  file[destination] = source;

  var ee = config.pkg.name === 'camunda-bpm-webapp-ee';
  var eePrefix = ee ? 'node_modules/camunda-bpm-webapp/' : '';
  var includePaths = [
    '<%= pkg.gruntConfig.commonsUiDir %>/lib/widgets',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/less',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/css',
    'node_modules',
    eePrefix + 'ui/common/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/scripts'
  ];

  lessConfig[pathConfig.appName + (pathConfig.plugin ? '_plugin' : '') + '_styles'] = {
    options: {
      paths: includePaths,

      compress: true,
      sourceMap: true,
      sourceMapURL: './' + path.basename(destination) + '.map',
      sourceMapFilename: destination + '.map'
    },
    files: file
  };

  if (pathConfig.appName === 'cockpit' && !pathConfig.plugin) {
    source = pathConfig.sourceDir + '/styles/styles-components.less';
    destination = pathConfig.buildTarget+'/styles/styles-components.css';
    file = {};
    file[destination] = source;

    lessConfig.cockpit_styles_components = {
      options: {
        paths: includePaths,

        compress: true,
        sourceMap: true,
        sourceMapURL: './' + path.basename(destination) + '.map',
        sourceMapFilename: destination + '.map'
      },
      files: file
    };
  }
};
