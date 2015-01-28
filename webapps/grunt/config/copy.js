'use strict';

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;

module.exports = function(config) {
  var grunt = config.grunt;

  function fileProcessing(content, srcpath) {
    if(grunt.config('buildTarget') === 'dist') {
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

  return {
    options: {},

    index: {
      options: {
        process: function() {
          return fileProcessing.apply(grunt, arguments);
        }
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            'index.html'
          ],
          dest: '<%= buildTarget %>/'
        }
      ]
    },

    assets: {
      process: function(content, srcpath) {
        grunt.log.ok('Copy '+ srcpath);
        return content;
      },
      files: [
        // custom styles and/or other css files
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/styles',
          src: ['*.css'],
          dest: '<%= buildTarget %>/styles/'
        },

        // images, fonts & stuff
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/',
          src:  [
            '{fonts,images}/**/*.*'
          ],
          dest: '<%= buildTarget %>/assets'
        },

        // dojo & dojox
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/vendor/dojo',
          src:  [
            '**/*.*'
          ],
          dest: '<%= buildTarget %>/assets/vendor'
        },

        // bootstrap fonts
        {
          expand: true,
          cwd: 'node_modules/camunda-commons-ui/node_modules/bootstrap/fonts',
          src: [
            '*.{eot,ttf,svg,woff}'
          ],
          dest: '<%= buildTarget %>/fonts/'
        }
      ]
    },
  };
};
