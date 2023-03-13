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

import Manager from 'dmn-js-shared/lib/base/Manager';

import DrdViewer from 'dmn-js-drd/lib/NavigatedViewer';
import DecisionTableViewer from 'dmn-js-decision-table/lib/Viewer';
import LiteralExpressionViewer from 'dmn-js-literal-expression/lib/Viewer';

import {is} from 'dmn-js-shared/lib/util/ModelUtil';
import {containsDi} from 'dmn-js-shared/lib/util/DiUtil';

/**
 * The dmn viewer.
 */
export default class Viewer extends Manager {
  constructor(options = {}) {
    super(options);
    this.options = options;
  }

  _getViewProviders() {
    let providers = [
      {
        id: 'literalExpression',
        constructor: LiteralExpressionViewer,
        opens(element) {
          return (
            is(element, 'dmn:Decision') &&
            is(element.decisionLogic, 'dmn:LiteralExpression')
          );
        }
      },
      {
        id: 'decisionTable',
        constructor: DecisionTableViewer,
        opens(element) {
          return (
            is(element, 'dmn:Decision') &&
            is(element.decisionLogic, 'dmn:DecisionTable')
          );
        }
      }
    ];

    const {tableViewOnly} = this.options;
    if (!tableViewOnly) {
      providers.push({
        id: 'drd',
        constructor: DrdViewer,
        opens(element) {
          return is(element, 'dmn:Definitions') && containsDi(element);
        }
      });
    }

    return providers;
  }
}
