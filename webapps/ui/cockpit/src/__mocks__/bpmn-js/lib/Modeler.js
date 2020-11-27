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

class Modeler {
  constructor({ container, bpmnRenderer } = {}) {
    this.canvas = {
      zoom: jest.fn(),
      addMarker: jest.fn(),
      removeMarker: jest.fn(),
      resized: jest.fn(),
      viewbox: jest.fn().mockReturnValue({})
    };
    this.zoomScroll = { stepZoom: jest.fn(), reset: jest.fn() };
    this.container = container;
    this.bpmnRenderer = bpmnRenderer;
    this.elementRegistry = {
      getGraphics: jest.fn(() => ({
        querySelector: jest.fn(() => ({ setAttribute: jest.fn() }))
      })),
      get: jest.fn().mockReturnValue({}),
      forEach: jest.fn()
    };
    this.eventBus = { on: jest.fn() };
    this.overlays = { add: jest.fn(), remove: jest.fn() };
    this.selection = { select: jest.fn() };
  }

  _container = {
    querySelector: () => {
      return {
        querySelector: () => {
          return {
            cloneNode: () => {
              return {
                setAttribute: jest.fn()
              };
            }
          };
        },
        appendChild: jest.fn()
      };
    }
  };

  importXML = jest.fn();

  saveXML = jest.fn().mockReturnValue({ xml: "some xml" });

  attachTo = jest.fn();
  detach = jest.fn();

  get = key => this[key];
}

export default Modeler;
