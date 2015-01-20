module.exports = function(config) {
  'use strict';

  var deps = [
    'angular'
  ];

  var rConf = {
    options: {
      stubModules: ['text'],

      optimize: '<%= (mode === "prod") ? "uglify2" : "none" %>',
      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: '<%= pkg.gruntConfig.pluginDir %>/',

      paths: {
        'angular': 'empty:',
        'text': '<%= pkg.gruntConfig.pluginDir.split("/").map(function () { return ".." }).join("/") %>/node_modules/requirejs-text/text'
      }
    },

    core: {
      options: {
        out: '<%= pkg.gruntConfig.buildTarget %>/base/app/plugin.js',
        include: ['base/app/plugin'],
        exclude: ['text'],
        insertRequire: ['base/app/plugin']
      }
    },
    jobDefinition: {
      options: {
        out: '<%= pkg.gruntConfig.buildTarget %>/jobDefinition/app/plugin.js',
        include: ['jobDefinition/app/plugin'],
        exclude: ['text'],
        insertRequire: ['jobDefinition/app/plugin']
      }
    },
    standaloneTask: {
	  options: {
	    out: '<%= pkg.gruntConfig.buildTarget %>/standaloneTask/app/plugin.js',
	    include: ['standaloneTask/app/plugin'],
	    exclude: ['text'],
	    insertRequire: ['standaloneTask/app/plugin']
	  }
	}

  };

  return rConf;
};
