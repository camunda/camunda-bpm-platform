/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

describe('Renderer tests', function() {

	it('should render none-startEvent', function() {

		var processDefinition = CAM.transform(
		    '<?xml version="1.0" encoding="UTF-8"?>' +
		    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"   xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:signavio="http://www.signavio.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exporter="Signavio Process Editor, http://www.signavio.com" exporterVersion="6.5.0" expressionLanguage="http://www.w3.org/1999/XPath" id="sid-c921f33b-76e6-41cc-a3b9-a995d90bc725" name="saassd" targetNamespace="http://www.signavio.com/bpmn20" typeLanguage="http://www.w3.org/2001/XMLSchema" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">' +
		    
		      '<process id="theProcess">' +
		    
		        '<startEvent id="theStart" />'+		       
		      
		      '</process>'+

		   	  '<bpmndi:BPMNDiagram id="sid-bcc9a5d3-f737-43ee-84ae-e3177a3e5a6d">' +
      			'<bpmndi:BPMNPlane bpmnElement="theProcess" id="sid-7f7a6c88-8fd5-41dd-93d0-045a40e0b253">' +

					'<bpmndi:BPMNShape bpmnElement="theStart" id="sid-6D7C53C1-51E4-44F2-B976-B893A4FF48C7_gui">' +
            			'<omgdc:Bounds height="30.0" width="30.0" x="0" y="0"/>' +
         			'</bpmndi:BPMNShape>' +

		    	'</bpmndi:BPMNPlane>' +
		      '</bpmndi:BPMNDiagram>' +

		    '</definitions>')[0];


		var elementRenderer = new CAM.BpmnElementRenderer(processDefinition);
		elementRenderer.performLayout();

		// make sure it rendered a circle for the startEvent
		var svgCanvas = elementRenderer.canvas.canvas;
		expect(svgCanvas.getElementsByTagName("circle").length).toBe(1);

		var circle = svgCanvas.getElementsByTagName("circle")[0];

		expect(circle.getAttribute("cx")).toBe('0');
		expect(circle.getAttribute("cy")).toBe('0');
		expect(circle.getAttribute("r")).toBe('15');


		<!-- the second time around the start event coordinates are different -->
		var processDefinition2 = CAM.transform(
		    '<?xml version="1.0" encoding="UTF-8"?>' +
		    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"   xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:signavio="http://www.signavio.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exporter="Signavio Process Editor, http://www.signavio.com" exporterVersion="6.5.0" expressionLanguage="http://www.w3.org/1999/XPath" id="sid-c921f33b-76e6-41cc-a3b9-a995d90bc725" name="saassd" targetNamespace="http://www.signavio.com/bpmn20" typeLanguage="http://www.w3.org/2001/XMLSchema" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">' +
		    
		      '<process id="theProcess">' +
		    
		        '<startEvent id="theStart" />'+		       
		      
		      '</process>'+

		   	  '<bpmndi:BPMNDiagram id="sid-bcc9a5d3-f737-43ee-84ae-e3177a3e5a6d">' +
      			'<bpmndi:BPMNPlane bpmnElement="theProcess" id="sid-7f7a6c88-8fd5-41dd-93d0-045a40e0b253">' +

					'<bpmndi:BPMNShape bpmnElement="theStart" id="sid-6D7C53C1-51E4-44F2-B976-B893A4FF48C7_gui">' +
            			'<omgdc:Bounds height="30.0" width="36.0" x="10" y="10"/>' +
         			'</bpmndi:BPMNShape>' +

		    	'</bpmndi:BPMNPlane>' +
		      '</bpmndi:BPMNDiagram>' +
		      
		    '</definitions>')[0];


		elementRenderer = new CAM.BpmnElementRenderer(processDefinition2);
		elementRenderer.performLayout();

		// make sure it rendered a circle for the startEvent
		svgCanvas = elementRenderer.canvas.canvas;
		expect(svgCanvas.getElementsByTagName("circle").length).toBe(1);

		circle = svgCanvas.getElementsByTagName("circle")[0];

		expect(circle.getAttribute("cx")).toBe('10');
		expect(circle.getAttribute("cy")).toBe('10');
		expect(circle.getAttribute("r")).toBe('18');

	});

	it('should render message-startEvent', function() {

		var processDefinition = CAM.transform(
		    '<?xml version="1.0" encoding="UTF-8"?>' +
		    '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"   xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:signavio="http://www.signavio.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exporter="Signavio Process Editor, http://www.signavio.com" exporterVersion="6.5.0" expressionLanguage="http://www.w3.org/1999/XPath" id="sid-c921f33b-76e6-41cc-a3b9-a995d90bc725" name="saassd" targetNamespace="http://www.signavio.com/bpmn20" typeLanguage="http://www.w3.org/2001/XMLSchema" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">' +
		    
		      '<process id="theProcess">' +
		    
		        '<startEvent id="theStart">'+		       
		        	'<messageEventDefinition />'+
		        '</startEvent>'+
		      
		      '</process>'+

		   	  '<bpmndi:BPMNDiagram id="sid-bcc9a5d3-f737-43ee-84ae-e3177a3e5a6d">' +
      			'<bpmndi:BPMNPlane bpmnElement="theProcess" id="sid-7f7a6c88-8fd5-41dd-93d0-045a40e0b253">' +

					'<bpmndi:BPMNShape bpmnElement="theStart" id="sid-6D7C53C1-51E4-44F2-B976-B893A4FF48C7_gui">' +
            			'<omgdc:Bounds height="30.0" width="30.0" x="0" y="0"/>' +
         			'</bpmndi:BPMNShape>' +

		    	'</bpmndi:BPMNPlane>' +
		      '</bpmndi:BPMNDiagram>' +

		    '</definitions>')[0];

  	dump(formatter.formatJson(JSON.stringify(processDefinition)));


		var elementRenderer = new CAM.BpmnElementRenderer(processDefinition);
		elementRenderer.performLayout();

		// make sure it rendered a circle for the startEvent
		var svgCanvas = elementRenderer.canvas.canvas;
		expect(svgCanvas.getElementsByTagName("circle").length).toBe(1);

		var circle = svgCanvas.getElementsByTagName("circle")[0];

		expect(circle.getAttribute("cx")).toBe('0');
		expect(circle.getAttribute("cy")).toBe('0');
		expect(circle.getAttribute("r")).toBe('15');

	});

});