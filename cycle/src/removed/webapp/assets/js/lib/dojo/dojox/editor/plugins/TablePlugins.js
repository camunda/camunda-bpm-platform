//>>built
require({cache:{"url:dojox/editor/plugins/resources/insertTable.html":"<div class=\"dijitDialog\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"${id}_title\">\n\t<div dojoAttachPoint=\"titleBar\" class=\"dijitDialogTitleBar\">\n\t<span dojoAttachPoint=\"titleNode\" class=\"dijitDialogTitle\" id=\"${id}_title\">${insertTableTitle}</span>\n\t<span dojoAttachPoint=\"closeButtonNode\" class=\"dijitDialogCloseIcon\" dojoAttachEvent=\"onclick: onCancel\" title=\"${buttonCancel}\">\n\t\t<span dojoAttachPoint=\"closeText\" class=\"closeText\" title=\"${buttonCancel}\">x</span>\n\t</span>\n\t</div>\n    <div dojoAttachPoint=\"containerNode\" class=\"dijitDialogPaneContent\">\n        <table class=\"etdTable\"><tr>\n            <td>\n                <label>${rows}</label>\n\t\t\t</td><td>\n                <span dojoAttachPoint=\"selectRow\" dojoType=\"dijit.form.TextBox\" value=\"2\"></span>\n            </td><td><table><tr><td class=\"inner\">\n                <label>${columns}</label>\n\t\t\t</td><td class=\"inner\">\n                <span dojoAttachPoint=\"selectCol\" dojoType=\"dijit.form.TextBox\" value=\"2\"></span>\n            </td></tr></table></td></tr>\t\t\n\t\t\t<tr><td>\n                <label>${tableWidth}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectWidth\" dojoType=\"dijit.form.TextBox\" value=\"100\"></span>\n\t\t\t</td><td>\n                <select dojoAttachPoint=\"selectWidthType\" hasDownArrow=\"true\" dojoType=\"dijit.form.FilteringSelect\">\n                  <option value=\"percent\">${percent}</option>\n                  <option value=\"pixels\">${pixels}</option>\n                </select></td></tr>\t\n            <tr><td>\n                <label>${borderThickness}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectBorder\" dojoType=\"dijit.form.TextBox\" value=\"1\"></span>\n            </td><td>\n                ${pixels}\n            </td></tr><tr><td>\n                <label>${cellPadding}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectPad\" dojoType=\"dijit.form.TextBox\" value=\"0\"></span>\n            </td><td class=\"cellpad\"></td></tr><tr><td>\n                <label>${cellSpacing}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectSpace\" dojoType=\"dijit.form.TextBox\" value=\"0\"></span>\n            </td><td class=\"cellspace\"></td></tr></table>\n        <div class=\"dialogButtonContainer\">\n            <div dojoType=\"dijit.form.Button\" dojoAttachEvent=\"onClick: onInsert\">${buttonInsert}</div>\n            <div dojoType=\"dijit.form.Button\" dojoAttachEvent=\"onClick: onCancel\">${buttonCancel}</div>\n        </div>\n\t</div>\n</div>\n","url:dojox/editor/plugins/resources/modifyTable.html":"<div class=\"dijitDialog\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"${id}_title\">\n\t<div dojoAttachPoint=\"titleBar\" class=\"dijitDialogTitleBar\">\n\t<span dojoAttachPoint=\"titleNode\" class=\"dijitDialogTitle\" id=\"${id}_title\">${modifyTableTitle}</span>\n\t<span dojoAttachPoint=\"closeButtonNode\" class=\"dijitDialogCloseIcon\" dojoAttachEvent=\"onclick: onCancel\" title=\"${buttonCancel}\">\n\t\t<span dojoAttachPoint=\"closeText\" class=\"closeText\" title=\"${buttonCancel}\">x</span>\n\t</span>\n\t</div>\n    <div dojoAttachPoint=\"containerNode\" class=\"dijitDialogPaneContent\">\n        <table class=\"etdTable\">\n          <tr><td>\n                <label>${backgroundColor}</label>\n            </td><td colspan=\"2\">\n                <span class=\"colorSwatchBtn\" dojoAttachPoint=\"backgroundCol\"></span>\n            </td></tr><tr><td>\n                <label>${borderColor}</label>\n            </td><td colspan=\"2\">\n                <span class=\"colorSwatchBtn\" dojoAttachPoint=\"borderCol\"></span>\n            </td></tr><tr><td>\n                <label>${align}</label>\n            </td><td colspan=\"2\">\t\n                <select dojoAttachPoint=\"selectAlign\" dojoType=\"dijit.form.FilteringSelect\">\n                  <option value=\"default\">${default}</option>\n                  <option value=\"left\">${left}</option>\n                  <option value=\"center\">${center}</option>\n                  <option value=\"right\">${right}</option>\n                </select>\n            </td></tr>\n            <tr><td>\n                <label>${tableWidth}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectWidth\" dojoType=\"dijit.form.TextBox\" value=\"100\"></span>\n            </td><td>\n                <select dojoAttachPoint=\"selectWidthType\" hasDownArrow=\"true\" dojoType=\"dijit.form.FilteringSelect\">\n                  <option value=\"percent\">${percent}</option>\n                  <option value=\"pixels\">${pixels}</option>\n                </select></td></tr>\t\n            <tr><td>\n                <label>${borderThickness}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectBorder\" dojoType=\"dijit.form.TextBox\" value=\"1\"></span>\n            </td><td>\n                ${pixels}\n            </td></tr><tr><td>\n                <label>${cellPadding}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectPad\" dojoType=\"dijit.form.TextBox\" value=\"0\"></span>\n            </td><td class=\"cellpad\"></td></tr><tr><td>\n                <label>${cellSpacing}</label>\n            </td><td>\n                <span dojoAttachPoint=\"selectSpace\" dojoType=\"dijit.form.TextBox\" value=\"0\"></span>\n            </td><td class=\"cellspace\"></td></tr>\n        </table>\n        <div class=\"dialogButtonContainer\">\n            <div dojoType=\"dijit.form.Button\" dojoAttachEvent=\"onClick: onSet\">${buttonSet}</div>\n            <div dojoType=\"dijit.form.Button\" dojoAttachEvent=\"onClick: onCancel\">${buttonCancel}</div>\n        </div>\n\t</div>\n</div>\n"}});
define("dojox/editor/plugins/TablePlugins",["dojo/_base/declare","dijit/_editor/_Plugin","dijit/_WidgetBase","dijit/_TemplatedMixin","dijit/_WidgetsInTemplateMixin","dijit/Dialog","dijit/Menu","dijit/MenuItem","dijit/MenuSeparator","dojo/text!./resources/insertTable.html","dojo/text!./resources/modifyTable.html","dojo/i18n!./nls/TableDialog","dijit/_base/popup","dijit/popup","dijit/_editor/range","dijit/_editor/selection","dijit/ColorPalette","dojox/widget/ColorPicker","dojo/_base/connect","dijit/TooltipDialog","dijit/form/Button","dijit/form/DropDownButton","dijit/form/TextBox","dijit/form/FilteringSelect"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c){
dojo.experimental("dojox.editor.plugins.TablePlugins");
var _d=_1(_2,{tablesConnected:false,currentlyAvailable:false,alwaysAvailable:false,availableCurrentlySet:false,initialized:false,tableData:null,shiftKeyDown:false,editorDomNode:null,undoEnabled:true,refCount:0,doMixins:function(){
dojo.mixin(this.editor,{getAncestorElement:function(_e){
return this._sCall("getAncestorElement",[_e]);
},hasAncestorElement:function(_f){
return this._sCall("hasAncestorElement",[_f]);
},selectElement:function(_10){
this._sCall("selectElement",[_10]);
},byId:function(id){
return dojo.byId(id,this.document);
},query:function(arg,_11,_12){
var ar=dojo.query(arg,_11||this.document);
return (_12)?ar[0]:ar;
}});
},initialize:function(_13){
this.refCount++;
_13.customUndo=true;
if(this.initialized){
return;
}
this.initialized=true;
this.editor=_13;
this.editor._tablePluginHandler=this;
_13.onLoadDeferred.addCallback(dojo.hitch(this,function(){
this.editorDomNode=this.editor.editNode||this.editor.iframe.document.body.firstChild;
this._myListeners=[];
this._myListeners.push(dojo.connect(this.editorDomNode,"mouseup",this.editor,"onClick"));
this._myListeners.push(dojo.connect(this.editor,"onDisplayChanged",this,"checkAvailable"));
this._myListeners.push(dojo.connect(this.editor,"onBlur",this,"checkAvailable"));
this.doMixins();
this.connectDraggable();
}));
},getTableInfo:function(_14){
if(_14){
this._tempStoreTableData(false);
}
if(this.tableData){
return this.tableData;
}
var tr,trs,td,tds,tbl,_15,_16,_17;
td=this.editor.getAncestorElement("td");
if(td){
tr=td.parentNode;
}
tbl=this.editor.getAncestorElement("table");
tds=dojo.query("td",tbl);
tds.forEach(function(d,i){
if(td==d){
_16=i;
}
});
trs=dojo.query("tr",tbl);
trs.forEach(function(r,i){
if(tr==r){
_17=i;
}
});
_15=tds.length/trs.length;
var o={tbl:tbl,td:td,tr:tr,trs:trs,tds:tds,rows:trs.length,cols:_15,tdIndex:_16,trIndex:_17,colIndex:_16%_15};
this.tableData=o;
this._tempStoreTableData(500);
return this.tableData;
},connectDraggable:function(){
if(!dojo.isIE){
return;
}
this.editorDomNode.ondragstart=dojo.hitch(this,"onDragStart");
this.editorDomNode.ondragend=dojo.hitch(this,"onDragEnd");
},onDragStart:function(){
var e=window.event;
if(!e.srcElement.id){
e.srcElement.id="tbl_"+(new Date().getTime());
}
},onDragEnd:function(){
var e=window.event;
var _18=e.srcElement;
var id=_18.id;
var doc=this.editor.document;
if(_18.tagName.toLowerCase()=="table"){
setTimeout(function(){
var _19=dojo.byId(id,doc);
dojo.removeAttr(_19,"align");
},100);
}
},checkAvailable:function(){
if(this.availableCurrentlySet){
return this.currentlyAvailable;
}
if(!this.editor){
return false;
}
if(this.alwaysAvailable){
return true;
}
this.currentlyAvailable=this.editor.focused?this.editor.hasAncestorElement("table"):false;
if(this.currentlyAvailable){
this.connectTableKeys();
}else{
this.disconnectTableKeys();
}
this._tempAvailability(500);
dojo.publish(this.editor.id+"_tablePlugins",[this.currentlyAvailable]);
return this.currentlyAvailable;
},_prepareTable:function(tbl){
var tds=this.editor.query("td",tbl);
if(!tds[0].id){
tds.forEach(function(td,i){
if(!td.id){
td.id="tdid"+i+this.getTimeStamp();
}
},this);
}
return tds;
},getTimeStamp:function(){
return new Date().getTime();
},_tempStoreTableData:function(_1a){
if(_1a===true){
}else{
if(_1a===false){
this.tableData=null;
}else{
if(_1a===undefined){
console.warn("_tempStoreTableData must be passed an argument");
}else{
setTimeout(dojo.hitch(this,function(){
this.tableData=null;
}),_1a);
}
}
}
},_tempAvailability:function(_1b){
if(_1b===true){
this.availableCurrentlySet=true;
}else{
if(_1b===false){
this.availableCurrentlySet=false;
}else{
if(_1b===undefined){
console.warn("_tempAvailability must be passed an argument");
}else{
this.availableCurrentlySet=true;
setTimeout(dojo.hitch(this,function(){
this.availableCurrentlySet=false;
}),_1b);
}
}
}
},connectTableKeys:function(){
if(this.tablesConnected){
return;
}
this.tablesConnected=true;
var _1c=(this.editor.iframe)?this.editor.document:this.editor.editNode;
this.cnKeyDn=dojo.connect(_1c,"onkeydown",this,"onKeyDown");
this.cnKeyUp=dojo.connect(_1c,"onkeyup",this,"onKeyUp");
this._myListeners.push(dojo.connect(_1c,"onkeypress",this,"onKeyUp"));
},disconnectTableKeys:function(){
dojo.disconnect(this.cnKeyDn);
dojo.disconnect(this.cnKeyUp);
this.tablesConnected=false;
},onKeyDown:function(evt){
var key=evt.keyCode;
if(key==16){
this.shiftKeyDown=true;
}
if(key==9){
var o=this.getTableInfo();
o.tdIndex=(this.shiftKeyDown)?o.tdIndex-1:tabTo=o.tdIndex+1;
if(o.tdIndex>=0&&o.tdIndex<o.tds.length){
this.editor.selectElement(o.tds[o.tdIndex]);
this.currentlyAvailable=true;
this._tempAvailability(true);
this._tempStoreTableData(true);
this.stopEvent=true;
}else{
this.stopEvent=false;
this.onDisplayChanged();
}
if(this.stopEvent){
dojo.stopEvent(evt);
}
}
},onKeyUp:function(evt){
var key=evt.keyCode;
if(key==16){
this.shiftKeyDown=false;
}
if(key==37||key==38||key==39||key==40){
this.onDisplayChanged();
}
if(key==9&&this.stopEvent){
dojo.stopEvent(evt);
}
},onDisplayChanged:function(){
this.currentlyAvailable=false;
this._tempStoreTableData(false);
this._tempAvailability(false);
this.checkAvailable();
},uninitialize:function(_1d){
if(this.editor==_1d){
this.refCount--;
if(!this.refCount&&this.initialized){
if(this.tablesConnected){
this.disconnectTableKeys();
}
this.initialized=false;
dojo.forEach(this._myListeners,function(l){
dojo.disconnect(l);
});
delete this._myListeners;
delete this.editor._tablePluginHandler;
delete this.editor;
}
this.inherited(arguments);
}
}});
var _1e=_1("dojox.editor.plugins.TablePlugins",_2,{iconClassPrefix:"editorIcon",useDefaultCommand:false,buttonClass:dijit.form.Button,commandName:"",label:"",alwaysAvailable:false,undoEnabled:true,onDisplayChanged:function(_1f){
if(!this.alwaysAvailable){
this.available=_1f;
this.button.set("disabled",!this.available);
}
},setEditor:function(_20){
this.editor=_20;
this.editor.customUndo=true;
this.inherited(arguments);
this._availableTopic=dojo.subscribe(this.editor.id+"_tablePlugins",this,"onDisplayChanged");
this.onEditorLoaded();
},onEditorLoaded:function(){
if(!this.editor._tablePluginHandler){
var _21=new _d();
_21.initialize(this.editor);
}else{
this.editor._tablePluginHandler.initialize(this.editor);
}
},selectTable:function(){
var o=this.getTableInfo();
if(o&&o.tbl){
this.editor._sCall("selectElement",[o.tbl]);
}
},_initButton:function(){
this.command=this.commandName;
this.label=this.editor.commands[this.command]=this._makeTitle(this.command);
this.inherited(arguments);
delete this.command;
this.connect(this.button,"onClick","modTable");
this.onDisplayChanged(false);
},modTable:function(cmd,_22){
this.begEdit();
var o=this.getTableInfo();
var sw=(dojo.isString(cmd))?cmd:this.commandName;
var r,c,i;
var _23=false;
if(dojo.isIE){
this.editor.focus();
}
switch(sw){
case "insertTableRowBefore":
r=o.tbl.insertRow(o.trIndex);
for(i=0;i<o.cols;i++){
c=r.insertCell(-1);
c.innerHTML="&nbsp;";
}
break;
case "insertTableRowAfter":
r=o.tbl.insertRow(o.trIndex+1);
for(i=0;i<o.cols;i++){
c=r.insertCell(-1);
c.innerHTML="&nbsp;";
}
break;
case "insertTableColumnBefore":
o.trs.forEach(function(r){
c=r.insertCell(o.colIndex);
c.innerHTML="&nbsp;";
});
_23=true;
break;
case "insertTableColumnAfter":
o.trs.forEach(function(r){
c=r.insertCell(o.colIndex+1);
c.innerHTML="&nbsp;";
});
_23=true;
break;
case "deleteTableRow":
o.tbl.deleteRow(o.trIndex);
break;
case "deleteTableColumn":
o.trs.forEach(function(tr){
tr.deleteCell(o.colIndex);
});
_23=true;
break;
case "modifyTable":
break;
case "insertTable":
break;
}
if(_23){
this.makeColumnsEven();
}
this.endEdit();
},begEdit:function(){
if(this.editor._tablePluginHandler.undoEnabled){
if(this.editor.customUndo){
this.editor.beginEditing();
}else{
this.valBeforeUndo=this.editor.getValue();
}
}
},endEdit:function(){
if(this.editor._tablePluginHandler.undoEnabled){
if(this.editor.customUndo){
this.editor.endEditing();
}else{
var _24=this.editor.getValue();
this.editor.setValue(this.valBeforeUndo);
this.editor.replaceValue(_24);
}
this.editor.onDisplayChanged();
}
},makeColumnsEven:function(){
setTimeout(dojo.hitch(this,function(){
var o=this.getTableInfo(true);
var w=Math.floor(100/o.cols);
o.tds.forEach(function(d){
dojo.attr(d,"width",w+"%");
});
}),10);
},getTableInfo:function(_25){
return this.editor._tablePluginHandler.getTableInfo(_25);
},_makeTitle:function(str){
var ns=[];
dojo.forEach(str,function(c,i){
if(c.charCodeAt(0)<91&&i>0&&ns[i-1].charCodeAt(0)!=32){
ns.push(" ");
}
if(i===0){
c=c.toUpperCase();
}
ns.push(c);
});
return ns.join("");
},getSelectedCells:function(){
var _26=[];
var tbl=this.getTableInfo().tbl;
this.editor._tablePluginHandler._prepareTable(tbl);
var e=this.editor;
var _27=e._sCall("getSelectedHtml",[null]);
var str=_27.match(/id="*\w*"*/g);
dojo.forEach(str,function(a){
var id=a.substring(3,a.length);
if(id.charAt(0)=="\""&&id.charAt(id.length-1)=="\""){
id=id.substring(1,id.length-1);
}
var _28=e.byId(id);
if(_28&&_28.tagName.toLowerCase()=="td"){
_26.push(_28);
}
},this);
if(!_26.length){
var sel=dijit.range.getSelection(e.window);
if(sel.rangeCount){
var r=sel.getRangeAt(0);
var _29=r.startContainer;
while(_29&&_29!=e.editNode&&_29!=e.document){
if(_29.nodeType===1){
var tg=_29.tagName?_29.tagName.toLowerCase():"";
if(tg==="td"){
return [_29];
}
}
_29=_29.parentNode;
}
}
}
return _26;
},updateState:function(){
if(this.button){
if((this.available||this.alwaysAvailable)&&!this.get("disabled")){
this.button.set("disabled",false);
}else{
this.button.set("disabled",true);
}
}
},destroy:function(){
this.inherited(arguments);
dojo.unsubscribe(this._availableTopic);
this.editor._tablePluginHandler.uninitialize(this.editor);
}});
var _2a=_1(_1e,{constructor:function(){
this.connect(this,"setEditor",function(_2b){
_2b.onLoadDeferred.addCallback(dojo.hitch(this,function(){
this._createContextMenu();
}));
this.button.domNode.style.display="none";
});
},destroy:function(){
if(this.menu){
this.menu.destroyRecursive();
delete this.menu;
}
this.inherited(arguments);
},_initButton:function(){
this.inherited(arguments);
if(this.commandName=="tableContextMenu"){
this.button.domNode.display="none";
}
},_createContextMenu:function(){
var _2c=new _7({targetNodeIds:[this.editor.iframe]});
var _2d=_c;
_2c.addChild(new _8({label:_2d.selectTableLabel,onClick:dojo.hitch(this,"selectTable")}));
_2c.addChild(new _9());
_2c.addChild(new _8({label:_2d.insertTableRowBeforeLabel,onClick:dojo.hitch(this,"modTable","insertTableRowBefore")}));
_2c.addChild(new _8({label:_2d.insertTableRowAfterLabel,onClick:dojo.hitch(this,"modTable","insertTableRowAfter")}));
_2c.addChild(new _8({label:_2d.insertTableColumnBeforeLabel,onClick:dojo.hitch(this,"modTable","insertTableColumnBefore")}));
_2c.addChild(new _8({label:_2d.insertTableColumnAfterLabel,onClick:dojo.hitch(this,"modTable","insertTableColumnAfter")}));
_2c.addChild(new _9());
_2c.addChild(new _8({label:_2d.deleteTableRowLabel,onClick:dojo.hitch(this,"modTable","deleteTableRow")}));
_2c.addChild(new _8({label:_2d.deleteTableColumnLabel,onClick:dojo.hitch(this,"modTable","deleteTableColumn")}));
this.menu=_2c;
}});
var _2e=_1("dojox.editor.plugins.InsertTable",_1e,{alwaysAvailable:true,modTable:function(){
var w=new dojox.editor.plugins.EditorTableDialog({});
w.show();
var c=dojo.connect(w,"onBuildTable",this,function(obj){
dojo.disconnect(c);
var res=this.editor.execCommand("inserthtml",obj.htmlText);
});
}});
var _2f=_1("dojox.editor.plugins.ModifyTable",_1e,{modTable:function(){
if(!this.editor._tablePluginHandler.checkAvailable()){
return;
}
var o=this.getTableInfo();
var w=new _30({table:o.tbl});
w.show();
this.connect(w,"onSetTable",function(_31){
var o=this.getTableInfo();
dojo.attr(o.td,"bgcolor",_31);
});
}});
var _32=_1([_3,_4,_5],{templateString:"<div style='display: none; position: absolute; top: -10000; z-index: -10000'>"+"<div dojoType='dijit.TooltipDialog' dojoAttachPoint='dialog' class='dojoxEditorColorPicker'>"+"<div dojoType='dojox.widget.ColorPicker' dojoAttachPoint='_colorPicker'></div>"+"<div style='margin: 0.5em 0em 0em 0em'>"+"<button dojoType='dijit.form.Button' type='button' dojoAttachPoint='_setButton'>${buttonSet}</button>"+"&nbsp;"+"<button dojoType='dijit.form.Button' type='button' dojoAttachPoint='_cancelButton'>${buttonCancel}</button>"+"</div>"+"</div>"+"</div>",widgetsInTemplate:true,constructor:function(){
dojo.mixin(this,_c);
},startup:function(){
if(!this._started){
this.inherited(arguments);
this.connect(this._setButton,"onClick",function(){
this.onChange(this.get("value"));
});
this.connect(this._cancelButton,"onClick",function(){
dijit.popup.close(this.dialog);
this.onCancel();
});
dojo.style(this.domNode,"display","block");
}
},_setValueAttr:function(_33,_34){
this._colorPicker.set("value",_33,_34);
},_getValueAttr:function(){
return this._colorPicker.get("value");
},setColor:function(_35){
this._colorPicker.setColor(_35,false);
},onChange:function(_36){
},onCancel:function(){
}});
var _37=_1("dojox.editor.plugins.ColorTableCell",_1e,{constructor:function(){
this.closable=true;
this.buttonClass=dijit.form.DropDownButton;
var _38=new _32();
dojo.body().appendChild(_38.domNode);
_38.startup();
this.dropDown=_38.dialog;
this.connect(_38,"onChange",function(_39){
this.modTable(null,_39);
this.editor.focus();
});
this.connect(_38,"onCancel",function(_3a){
this.editor.focus();
});
this.connect(_38.dialog,"onOpen",function(){
var o=this.getTableInfo(),tds=this.getSelectedCells(o.tbl);
if(tds&&tds.length>0){
var t=tds[0]==this.lastObject?tds[0]:tds[tds.length-1],_3b;
while(t&&((_3b=dojo.style(t,"backgroundColor"))=="transparent"||_3b.indexOf("rgba")==0)){
t=t.parentNode;
}
_3b=dojo.style(t,"backgroundColor");
if(_3b!="transparent"&&_3b.indexOf("rgba")!=0){
_38.setColor(_3b);
}
}
});
this.connect(this,"setEditor",function(_3c){
_3c.onLoadDeferred.addCallback(dojo.hitch(this,function(){
this.connect(this.editor.editNode,"onmouseup",function(evt){
this.lastObject=evt.target;
});
}));
});
},_initButton:function(){
this.command=this.commandName;
this.label=this.editor.commands[this.command]=this._makeTitle(this.command);
this.inherited(arguments);
delete this.command;
this.onDisplayChanged(false);
},modTable:function(cmd,_3d){
this.begEdit();
var o=this.getTableInfo();
var tds=this.getSelectedCells(o.tbl);
dojo.forEach(tds,function(td){
dojo.style(td,"backgroundColor",_3d);
});
this.endEdit();
}});
_1("dojox.editor.plugins.EditorTableDialog",[_6,_4,_5],{baseClass:"EditorTableDialog",templateString:_a,postMixInProperties:function(){
dojo.mixin(this,_c);
this.inherited(arguments);
},postCreate:function(){
dojo.addClass(this.domNode,this.baseClass);
this.inherited(arguments);
},onInsert:function(){
var _3e=this.selectRow.get("value")||1,_3f=this.selectCol.get("value")||1,_40=this.selectWidth.get("value"),_41=this.selectWidthType.get("value"),_42=this.selectBorder.get("value"),pad=this.selectPad.get("value"),_43=this.selectSpace.get("value"),_44="tbl_"+(new Date().getTime()),t="<table id=\""+_44+"\"width=\""+_40+((_41=="percent")?"%":"")+"\" border=\""+_42+"\" cellspacing=\""+_43+"\" cellpadding=\""+pad+"\">\n";
for(var r=0;r<_3e;r++){
t+="\t<tr>\n";
for(var c=0;c<_3f;c++){
t+="\t\t<td width=\""+(Math.floor(100/_3f))+"%\">&nbsp;</td>\n";
}
t+="\t</tr>\n";
}
t+="</table><br />";
this.onBuildTable({htmlText:t,id:_44});
var cl=dojo.connect(this,"onHide",function(){
dojo.disconnect(cl);
var _45=this;
setTimeout(function(){
_45.destroyRecursive();
},10);
});
this.hide();
},onCancel:function(){
var c=dojo.connect(this,"onHide",function(){
dojo.disconnect(c);
var _46=this;
setTimeout(function(){
_46.destroyRecursive();
},10);
});
},onBuildTable:function(_47){
}});
var _30=_1([_6,_4,_5],{baseClass:"EditorTableDialog",table:null,tableAtts:{},templateString:_b,postMixInProperties:function(){
dojo.mixin(this,_c);
this.inherited(arguments);
},postCreate:function(){
dojo.addClass(this.domNode,this.baseClass);
this.inherited(arguments);
this._cleanupWidgets=[];
var w1=new dijit.ColorPalette({});
this.connect(w1,"onChange",function(_48){
dijit.popup.close(w1);
this.setBrdColor(_48);
});
this.connect(w1,"onBlur",function(){
dijit.popup.close(w1);
});
this.connect(this.borderCol,"click",function(){
dijit.popup.open({popup:w1,around:this.borderCol});
w1.focus();
});
var w2=new dijit.ColorPalette({});
this.connect(w2,"onChange",function(_49){
dijit.popup.close(w2);
this.setBkColor(_49);
});
this.connect(w2,"onBlur",function(){
dijit.popup.close(w2);
});
this.connect(this.backgroundCol,"click",function(){
dijit.popup.open({popup:w2,around:this.backgroundCol});
w2.focus();
});
this._cleanupWidgets.push(w1);
this._cleanupWidgets.push(w2);
this.setBrdColor(dojo.attr(this.table,"bordercolor"));
this.setBkColor(dojo.attr(this.table,"bgcolor"));
var w=dojo.attr(this.table,"width");
if(!w){
w=this.table.style.width;
}
var p="pixels";
if(dojo.isString(w)&&w.indexOf("%")>-1){
p="percent";
w=w.replace(/%/,"");
}
if(w){
this.selectWidth.set("value",w);
this.selectWidthType.set("value",p);
}else{
this.selectWidth.set("value","");
this.selectWidthType.set("value","percent");
}
this.selectBorder.set("value",dojo.attr(this.table,"border"));
this.selectPad.set("value",dojo.attr(this.table,"cellPadding"));
this.selectSpace.set("value",dojo.attr(this.table,"cellSpacing"));
this.selectAlign.set("value",dojo.attr(this.table,"align"));
},setBrdColor:function(_4a){
this.brdColor=_4a;
dojo.style(this.borderCol,"backgroundColor",_4a);
},setBkColor:function(_4b){
this.bkColor=_4b;
dojo.style(this.backgroundCol,"backgroundColor",_4b);
},onSet:function(){
dojo.attr(this.table,"borderColor",this.brdColor);
dojo.attr(this.table,"bgColor",this.bkColor);
if(this.selectWidth.get("value")){
dojo.style(this.table,"width","");
dojo.attr(this.table,"width",(this.selectWidth.get("value")+((this.selectWidthType.get("value")=="pixels")?"":"%")));
}
dojo.attr(this.table,"border",this.selectBorder.get("value"));
dojo.attr(this.table,"cellPadding",this.selectPad.get("value"));
dojo.attr(this.table,"cellSpacing",this.selectSpace.get("value"));
dojo.attr(this.table,"align",this.selectAlign.get("value"));
var c=dojo.connect(this,"onHide",function(){
dojo.disconnect(c);
var _4c=this;
setTimeout(function(){
_4c.destroyRecursive();
},10);
});
this.hide();
},onCancel:function(){
var c=dojo.connect(this,"onHide",function(){
dojo.disconnect(c);
var _4d=this;
setTimeout(function(){
_4d.destroyRecursive();
},10);
});
},onSetTable:function(_4e){
},destroy:function(){
this.inherited(arguments);
dojo.forEach(this._cleanupWidgets,function(w){
if(w&&w.destroy){
w.destroy();
}
});
delete this._cleanupWidgets;
}});
dojo.subscribe(dijit._scopeName+".Editor.getPlugin",null,function(o){
if(o.plugin){
return;
}
if(o.args&&o.args.command){
var cmd=o.args.command.charAt(0).toLowerCase()+o.args.command.substring(1,o.args.command.length);
switch(cmd){
case "insertTableRowBefore":
case "insertTableRowAfter":
case "insertTableColumnBefore":
case "insertTableColumnAfter":
case "deleteTableRow":
case "deleteTableColumn":
o.plugin=new _1e({commandName:cmd});
break;
case "colorTableCell":
o.plugin=new _37({commandName:cmd});
break;
case "modifyTable":
o.plugin=new _2f({commandName:cmd});
break;
case "insertTable":
o.plugin=new _2e({commandName:cmd});
break;
case "tableContextMenu":
o.plugin=new _2a({commandName:cmd});
break;
}
}
});
return _1e;
});
