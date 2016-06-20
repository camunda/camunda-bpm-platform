module.exports = function(config, lessConfig, pathConfig) {
  'use strict';
  var path = require('path');

  var file = {};
  if(pathConfig.plugin) {
    file[pathConfig.buildTarget+'/plugin.css'] = pathConfig.sourceDir + '/styles.less';
  } else {
    file[pathConfig.buildTarget+'/styles/styles.css'] = pathConfig.sourceDir + '/styles/styles.less';
  }

  var ee = config.pkg.name === 'camunda-bpm-webapp-ee';
  var eePrefix = ee ? 'node_modules/camunda-bpm-webapp/' : '';
  var includePaths = [
    '<%= pkg.gruntConfig.commonsUiDir %>/lib/widgets',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/less',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/css',
    '<%= pkg.gruntConfig.commonsUiDir %>/node_modules',
    eePrefix + 'ui/common/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/scripts'
  ];

  var outputFilepath = Object.keys(file)[0];

  lessConfig[pathConfig.appName + (pathConfig.plugin ? '_plugin' : '') + '_styles'] = {
    options: {
      paths: includePaths,

      compress: false,

      sourceMap: true,
      sourceMapFilename: outputFilepath + '.map',
      sourceMapURL: './' + (pathConfig.plugin ? 'plugin' : 'styles') + '.css.map',
      sourceMapFileInline: true
    },
    files: file
  };

  if (pathConfig.appName === 'cockpit' && !pathConfig.plugin) {
    file = {};
    outputFilepath = pathConfig.buildTarget + '/styles/styles-components.css';
    file[outputFilepath] = pathConfig.sourceDir + '/styles/styles-components.less';

    lessConfig.cockpit_styles_components = {
      options: {
        paths: includePaths,

        compress: false,

        sourceMap: true,
        sourceMapFilename: outputFilepath + '.map',
        sourceMapURL: './styles-components.css.map',
        sourceMapFileInline: true
      },
      files: file
    };
  }
};
