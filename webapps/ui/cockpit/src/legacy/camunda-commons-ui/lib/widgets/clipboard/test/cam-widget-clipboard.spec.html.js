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

module.exports = `<html>
  <head>
    <title>Camunda commons UI library</title>
    <base href="/" />
    <!--[if IE]><script type="text/javascript">
        // Fix for IE ignoring relative base tags.
        // See http://stackoverflow.com/questions/3926197/html-base-tag-and-local-folder-path-with-internet-explorer
        (function() {
            var baseTag = document.getElementsByTagName('base');
            if (baseTag[0]) { baseTag[0].href = baseTag[0].href; }
        })();
    </script><![endif]-->
    <link rel="icon" href="resources/img/favicon.ico" />
    <link href="styles.css" rel="stylesheet" />
    <link href="test-styles.css" rel="stylesheet" />
  </head>
  <body class="cam-widget-clipboard-test-page">
    <!-- gh-pages-menu -->

    <header>
      <div>
        <h1>Clipboard</h1>
      </div>
    </header>

        <section class="widget-description">
      <header>
        <h2>Description</h2>
      </header>
      <p>Adds a button allowing to easily copy a value to the clipboard.</p>
    </section>

    <section class="widget-reference">
      <header>
        <h2>Usage</h2>
      </header>

      <h3>Options</h3>
      <dl>
        <dt><span class="badge">=</span> cam-widget-clipboard</dt>
        <dd>determines the value to be copied to the clipboard</dd>

        <dt><span class="badge">?</span> no-tooltip</dt>
        <dd>if this attribute is present, the default tooltip is not added</dd>

        <dt><span class="badge">?</span> link-before</dt>
        <dd>if this attribute is present, copy link/icon is placed before transcluded content</dd>
      </dl>
    </section>

    <section class="widget-examples">
      <header>
        <h2>Examples</h2>
      </header>

      <div class="widget-example"
           id="default"
           ng-controller="testController">
        <h3>Basic usage</h3>
        <pre ng-non-bindable>&lt;input ng-model="obj.varToCopy"
       class="form-control"
       type="text"/&gt;
&lt;span cam-widget-clipboard="varToCopy"&gt;With some transcluded content&lt;/span&gt;</pre>
        <div class="test-container">
          <input ng-model="obj.varToCopy"
                 class="form-control"
                 type="text"/>
          <span cam-widget-clipboard="obj.varToCopy">Hover this ;)</span>
        </div><!-- /.test-container -->
      </div><!-- /.widget-example -->
    </section>

    <!-- gh-pages-footer -->

    <script src="lib/widgets/clipboard/test/cam-widget-clipboard.build.js"></script>
  </body>
</html>
`;
