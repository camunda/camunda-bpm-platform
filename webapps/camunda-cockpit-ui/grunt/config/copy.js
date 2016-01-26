'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;

module.exports = function(config, copyConf) {
  var grunt = config.grunt;

  function fileProcessing(content, srcpath) {
    if(grunt.config('buildMode') === 'prod') {
      // removes the template comments
      content = content
                .split('\n').filter(function(line) {
                  return !commentLineExp.test(line);
                }).join('\n');

      var date = new Date();
      var cacheBuster = [date.getFullYear(), date.getMonth(), date.getDate()].join('-');
      content = content
                .replace(/\/\* cache-busting /, '/* cache-busting */')
                .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

      return content;
    }
    else {
      content = content
                .replace(/\/\* cache-busting/, '/* cache-busting */')
                .replace(/CACHE_BUSTER/g, (new Date()).getTime());

      return content;
    }
  }

  copyConf.cockpit_index = {
      options: {
        process: function() {
          return fileProcessing.apply(grunt, arguments);
        }
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
      process: function(content, srcpath) {
        grunt.log.ok('Copy '+ srcpath);
        return content;
      },
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
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/../../camunda-commons-ui/node_modules/bootstrap/fonts',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },
        // bpmn fonts
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/../../camunda-commons-ui/node_modules/bpmn-font/dist/font',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },
        // open sans and dmn fonts
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/../../camunda-commons-ui/vendor/fonts',
          src: ['*.{eot,svg,ttf,woff,woff2}'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/fonts/'
        },

        // placeholder shims
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.cockpitSourceDir %>/../../camunda-commons-ui/vendor',
          src: ['placeholders.*'],
          dest: '<%= pkg.gruntConfig.cockpitBuildTarget %>/scripts/'
        }
      ]
    };
};
