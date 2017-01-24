module.exports = function(config, browserifyConfig) {
  'use strict';

  browserifyConfig.cockpit_scripts = {
    options: {
      browserifyOptions: {
        standalone: 'CamundaCockpitUi',
        debug: true
      },
      watch: true,
      postBundleCB: function(err, src, next) {

        console.log('post bundling', err);

        var buildMode = config.grunt.config('buildMode');
        var livereloadPort = config.grunt.config('pkg.gruntConfig.livereloadPort');
        if (buildMode !== 'prod' && livereloadPort) {
          config.grunt.log.writeln('Enabling livereload for cockpit on port: ' + livereloadPort);
          //var contents = grunt.file.read(data.path);
          var contents = src.toString();

          contents = contents
                      .replace(/\/\* live-reload/, '/* live-reload */')
                      .replace(/LIVERELOAD_PORT/g, livereloadPort);

          next(err, new Buffer(contents));
        } else {
          next(err, src);
        }

      }
    },
    src: ['./<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/camunda-cockpit-ui.js'],
    dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/camunda-cockpit-ui.js'
  };

  browserifyConfig.cockpit_plugins = {
    options: {
      watch: true,
      transform: [
        [ 'exposify',
            {
              expose: {
               'angular': 'angular',
               'jquery': 'jquery',
               'camunda-commons-ui': 'camunda-commons-ui',
               'camunda-bpm-sdk-js': 'camunda-bpm-sdk-js',
               'angular-data-depend': 'angular-data-depend',
               'moment': 'moment',
               'events': 'events',
               'cam-common': 'cam-common'
              }
            }
        ]
      ],
      browserifyOptions: {
        standalone: 'CockpitPlugins',
        debug: true
      }
    },
    src: ['./<%= pkg.gruntConfig.pluginSourceDir %>/cockpit/plugins/cockpitPlugins.js'],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/cockpit/app/plugin.js'
  };
};
