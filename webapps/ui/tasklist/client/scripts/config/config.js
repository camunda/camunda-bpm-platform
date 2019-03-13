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

window.camTasklistConf = {
  // change the app name and vendor
  // app: {
  //   name: 'Todos',
  //   vendor: 'Company'
  // },
  //
  // configure the date format
  // "dateFormat": {
  //   "normal": "LLL",
  //   "long":   "LLLL"
  // },
  //
  // "locales": {
  //    "availableLocales": ["en", "de"],
  //    "fallbackLocale": "en"
  //  },
  //
  // // custom libraries and scripts loading and initialization,
  // // see: http://docs.camunda.org/guides/user-guide/#tasklist-customizing-custom-scripts
  // customScripts: {
  //   // AngularJS module names
  //   ngDeps: ['ui.bootstrap'],
  //   // RequireJS configuration for a complete configuration documentation see:
  //   // http://requirejs.org/docs/api.html#config
  //   deps: ['jquery', 'custom-ui'],
  //   paths: {
  //     // if you have a folder called `custom-ui` (in the `scripts` folder)
  //     // with a file called `scripts.js` in it and defining the `custom-ui` AMD module
  //     'custom-ui': 'custom-ui/scripts'
  //   }
  // },

  'shortcuts': {
    'claimTask': {
      'key': 'ctrl+alt+c',
      'description': 'claims the currently selected task'
    },
    'focusFilter': {
      'key': 'ctrl+shift+f',
      'description': 'set the focus to the first filter in the list'
    },
    'focusList': {
      'key': 'ctrl+alt+l',
      'description': 'sets the focus to the first task in the list'
    },
    'focusForm': {
      'key': 'ctrl+alt+f',
      'description': 'sets the focus to the first input field in a task form'
    },
    'startProcess': {
      'key': 'ctrl+alt+p',
      'description': 'opens the start process modal dialog'
    }
  }
};
