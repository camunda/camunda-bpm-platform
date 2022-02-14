/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var utils = module.exports = {};

utils.copy = {};


utils.copy.removeAtDist = function(mode) {
  mode = mode || 'dist';
  var removeAtDistExp = /<!-- #production-remove.*\/production-remove -->/igm;

  return function(content, srcpath) {
    if (mode !== 'dist') { return content; }

    return content.replace(removeAtDistExp, '');
  };
};


utils.copy.livereloadPort = function(mode) {
  mode = mode || 'dist';
  return function(content, srcpath) {
    if (mode === 'dist') { return content; }

    return content.replace('LIVERELOAD_PORT', config.livereloadPort);
  };
};


utils.copy.cacheBusting = function(mode) {
  mode = mode || 'dist';

  return function(content, srcpath) {
    var date = new Date();
    var cacheBuster = mode === 'dist' ?
      [date.getFullYear(), date.getMonth(), date.getDate()].join('-') :
      (new Date()).getTime();

    content = content
      .replace(/\/\* cache-busting /, '/* cache-busting */')
      .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

    return content;
  };
};


utils.copy.templateComments = function(mode) {
  mode = mode || 'dist';
  var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;

  return function(content, srcpath) {
    if (mode !== 'dist' || srcpath.slice(-4) !== 'html') {
      return content;
    }

    content = content
      .split('\n').filter(function(line) {
        return !commentLineExp.test(line);
      }).join('\n');

    return content;
  };
};
