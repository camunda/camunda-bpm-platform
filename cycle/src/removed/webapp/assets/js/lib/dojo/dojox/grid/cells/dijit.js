//>>built
define("dojox/grid/cells/dijit",["dojo/_base/kernel","../../main","dojo/_base/declare","dojo/_base/array","dojo/_base/lang","dojo/_base/json","dojo/_base/connect","dojo/_base/sniff","dojo/dom","dojo/dom-attr","dojo/dom-construct","dojo/dom-geometry","dojo/data/ItemFileReadStore","dijit/form/DateTextBox","dijit/form/TimeTextBox","dijit/form/ComboBox","dijit/form/CheckBox","dijit/form/TextBox","dijit/form/NumberSpinner","dijit/form/NumberTextBox","dijit/form/CurrencyTextBox","dijit/form/HorizontalSlider","dijit/form/_TextBoxMixin","dijit/Editor","../util","./_base"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,_10,_11,_12,_13,_14,_15,_16,_17,_18,_19,_1a){
var _1b=_3("dojox.grid.cells._Widget",_1a,{widgetClass:_12,constructor:function(_1c){
this.widget=null;
if(typeof this.widgetClass=="string"){
_1.deprecated("Passing a string to widgetClass is deprecated","pass the widget class object instead","2.0");
this.widgetClass=_5.getObject(this.widgetClass);
}
},formatEditing:function(_1d,_1e){
this.needFormatNode(_1d,_1e);
return "<div></div>";
},getValue:function(_1f){
return this.widget.get("value");
},_unescapeHTML:function(_20){
return (_20&&_20.replace&&this.grid.escapeHTMLInData)?_20.replace(/&lt;/g,"<").replace(/&amp;/g,"&"):_20;
},setValue:function(_21,_22){
if(this.widget&&this.widget.set){
_22=this._unescapeHTML(_22);
if(this.widget.onLoadDeferred){
var _23=this;
this.widget.onLoadDeferred.addCallback(function(){
_23.widget.set("value",_22===null?"":_22);
});
}else{
this.widget.set("value",_22);
}
}else{
this.inherited(arguments);
}
},getWidgetProps:function(_24){
return _5.mixin({dir:this.dir,lang:this.lang},this.widgetProps||{},{constraints:_5.mixin({},this.constraint)||{},required:(this.constraint||{}).required,value:this._unescapeHTML(_24)});
},createWidget:function(_25,_26,_27){
return new this.widgetClass(this.getWidgetProps(_26),_25);
},attachWidget:function(_28,_29,_2a){
_28.appendChild(this.widget.domNode);
this.setValue(_2a,_29);
},formatNode:function(_2b,_2c,_2d){
if(!this.widgetClass){
return _2c;
}
if(!this.widget){
this.widget=this.createWidget.apply(this,arguments);
}else{
this.attachWidget.apply(this,arguments);
}
this.sizeWidget.apply(this,arguments);
this.grid.views.renormalizeRow(_2d);
this.grid.scroller.rowHeightChanged(_2d,true);
this.focus();
return undefined;
},sizeWidget:function(_2e,_2f,_30){
var p=this.getNode(_30),box=_1.contentBox(p);
_1.marginBox(this.widget.domNode,{w:box.w});
},focus:function(_31,_32){
if(this.widget){
setTimeout(_5.hitch(this.widget,function(){
_19.fire(this,"focus");
if(this.focusNode&&this.focusNode.tagName==="INPUT"){
_17.selectInputText(this.focusNode);
}
}),0);
}
},_finish:function(_33){
this.inherited(arguments);
_19.removeNode(this.widget.domNode);
if(_8("ie")){
_9.setSelectable(this.widget.domNode,true);
}
}});
_1b.markupFactory=function(_34,_35){
_1a.markupFactory(_34,_35);
var _36=_5.trim(_a.get(_34,"widgetProps")||"");
var _37=_5.trim(_a.get(_34,"constraint")||"");
var _38=_5.trim(_a.get(_34,"widgetClass")||"");
if(_36){
_35.widgetProps=_6.fromJson(_36);
}
if(_37){
_35.constraint=_6.fromJson(_37);
}
if(_38){
_35.widgetClass=_5.getObject(_38);
}
};
var _10=_3("dojox.grid.cells.ComboBox",_1b,{widgetClass:_10,getWidgetProps:function(_39){
var _3a=[];
_4.forEach(this.options,function(o){
_3a.push({name:o,value:o});
});
var _3b=new _d({data:{identifier:"name",items:_3a}});
return _5.mixin({},this.widgetProps||{},{value:_39,store:_3b});
},getValue:function(){
var e=this.widget;
e.set("displayedValue",e.get("displayedValue"));
return e.get("value");
}});
_10.markupFactory=function(_3c,_3d){
_1b.markupFactory(_3c,_3d);
var _3e=_5.trim(_a.get(_3c,"options")||"");
if(_3e){
var o=_3e.split(",");
if(o[0]!=_3e){
_3d.options=o;
}
}
};
var _e=_3("dojox.grid.cells.DateTextBox",_1b,{widgetClass:_e,setValue:function(_3f,_40){
if(this.widget){
this.widget.set("value",new Date(_40));
}else{
this.inherited(arguments);
}
},getWidgetProps:function(_41){
return _5.mixin(this.inherited(arguments),{value:new Date(_41)});
}});
_e.markupFactory=function(_42,_43){
_1b.markupFactory(_42,_43);
};
var _11=_3("dojox.grid.cells.CheckBox",_1b,{widgetClass:_11,getValue:function(){
return this.widget.checked;
},setValue:function(_44,_45){
if(this.widget&&this.widget.attributeMap.checked){
this.widget.set("checked",_45);
}else{
this.inherited(arguments);
}
},sizeWidget:function(_46,_47,_48){
return;
}});
_11.markupFactory=function(_49,_4a){
_1b.markupFactory(_49,_4a);
};
var _18=_3("dojox.grid.cells.Editor",_1b,{widgetClass:_18,getWidgetProps:function(_4b){
return _5.mixin({},this.widgetProps||{},{height:this.widgetHeight||"100px"});
},createWidget:function(_4c,_4d,_4e){
var _4f=new this.widgetClass(this.getWidgetProps(_4d),_4c);
_4f.onLoadDeferred.then(_5.hitch(this,"populateEditor"));
return _4f;
},formatNode:function(_50,_51,_52){
this.content=_51;
this.inherited(arguments);
if(_8("mozilla")){
var e=this.widget;
e.open();
if(this.widgetToolbar){
_b.place(e.toolbar.domNode,e.editingArea,"before");
}
}
},populateEditor:function(){
this.widget.set("value",this.content);
this.widget.placeCursorAtEnd();
}});
_18.markupFactory=function(_53,_54){
_1b.markupFactory(_53,_54);
var h=_5.trim(_a.get(_53,"widgetHeight")||"");
if(h){
if((h!="auto")&&(h.substr(-2)!="em")){
h=parseInt(h,10)+"px";
}
_54.widgetHeight=h;
}
};
return _2.grid.cells.dijit;
});
