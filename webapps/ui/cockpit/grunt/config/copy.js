'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;

module.exports = function(config, copyConf) {
  var grunt = config.grunt;

  var path = require('path');
  var now = (new Date()).getTime();
  var version = grunt.file.readJSON(path.resolve(__dirname, '../../../../package.json')).version;
  version = (version.indexOf('-SNAPSHOT') > -1 ? (version +'-'+ now) : version);


  function prod () {
    return grunt.config('buildMode') === 'prod';
  }

  function cacheBust(content, srcpath) {
    if (srcpath.slice(-4) !== 'html') { return content; }
    return content.split('$GRUNT_CACHE_BUST').join(prod() ? version : now);
  }

  function fileProcessing(content, srcpath) {
    if(prod()) {
      // removes the template comments
      content = content
                .split('\n').filter(function(line) {
                  return !commentLineExp.test(line);
                }).join('\n');
    }

    content = cacheBust(content, srcpath);

    return content;
  }

  copyConf.cockpit_index = {
      options: {
        process: fileProcessing
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts/',
          src: [
            'index.html',
            'camunda-cockpit-bootstrap.js'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/'
        }
      ]
  };

  copyConf.cockpit_assets = {
      files: [
        // custom styles and/or other css files
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/styles',
          src: ['*.css'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/styles/'
        },

        // images, fonts & stuff
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/',
          src:  [
            '{fonts,images}/**/*.*'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/assets'
        },

        // commons-ui images
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/resources/img/',
          src:  [
            '*.*'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/assets/images'
        },

        // dojo & dojox
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/vendor/dojo',
          src:  [
            '**/*.*'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/assets/vendor'
        },

        // bootstrap fonts
        {
          expand: true,
          cwd: 'node_modules/bootstrap/fonts',
          src: [
            '*.{eot,ttf,svg,woff,woff2}'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },
        // bpmn fonts
        {
          expand: true,
          cwd: 'node_modules/bpmn-font/dist/font',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },
        // open sans
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor/fonts',
          src: ['*.{eot,svg,ttf,woff,woff2}'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },
        // dmn
        {
          expand: true,
          cwd: 'node_modules/dmn-js/fonts',
          src: ['*.{eot,svg,ttf,woff,woff2}'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },

        // placeholder shims
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.commonsUiDir %>/vendor',
          src: ['placeholders.*'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/'
        }
      ]
    };

  copyConf.cockpit_config = {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/scripts',
          src: ['config.js'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/'
        }
      ]
  };
};
