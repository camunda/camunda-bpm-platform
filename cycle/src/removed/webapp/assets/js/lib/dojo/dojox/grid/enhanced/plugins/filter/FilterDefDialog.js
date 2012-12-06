//>>built
define("dojox/grid/enhanced/plugins/filter/FilterDefDialog",["dojo/_base/declare","dojo/_base/array","dojo/_base/connect","dojo/_base/lang","dojo/_base/event","dojo/_base/html","dojo/_base/sniff","dojo/cache","dojo/keys","dojo/string","dojo/window","dojo/date/locale","./FilterBuilder","../Dialog","dijit/form/ComboBox","dijit/form/TextBox","dijit/form/NumberTextBox","dijit/form/DateTextBox","dijit/form/TimeTextBox","dijit/form/Button","dijit/layout/AccordionContainer","dijit/layout/ContentPane","dijit/_Widget","dijit/_TemplatedMixin","dijit/_WidgetsInTemplateMixin","dijit/focus","dojox/html/metrics","dijit/a11y","dijit/Tooltip","dijit/form/Select","dijit/form/RadioButton","dojox/html/ellipsis","../../../cells/dijit"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,_10,_11,_12,_13,_14,_15,_16,_17,_18,_19,_1a,_1b,_1c){
var _1d={relSelect:60,accordionTitle:70,removeCBoxBtn:-1,colSelect:90,condSelect:95,valueBox:10,addCBoxBtn:20,filterBtn:30,clearBtn:40,cancelBtn:50};
var _1e=_1("dojox.grid.enhanced.plugins.filter.AccordionContainer",_15,{nls:null,addChild:function(_1f,_20){
var _21=arguments[0]=_1f._pane=new _16({content:_1f});
this.inherited(arguments);
this._modifyChild(_21);
},removeChild:function(_22){
var _23=_22,_24=false;
if(_22._pane){
_24=true;
_23=arguments[0]=_22._pane;
}
this.inherited(arguments);
if(_24){
this._hackHeight(false,this._titleHeight);
var _25=this.getChildren();
if(_25.length===1){
_6.style(_25[0]._removeCBoxBtn.domNode,"display","none");
}
}
_23.destroyRecursive();
},selectChild:function(_26){
if(_26._pane){
arguments[0]=_26._pane;
}
this.inherited(arguments);
},resize:function(){
this.inherited(arguments);
_2.forEach(this.getChildren(),this._setupTitleDom);
},startup:function(){
if(this._started){
return;
}
this.inherited(arguments);
if(parseInt(_7("ie"),10)==7){
_2.some(this._connects,function(_27){
if((_27[0]||{})[1]=="onresize"){
this.disconnect(_27);
return true;
}
},this);
}
_2.forEach(this.getChildren(),function(_28){
this._modifyChild(_28,true);
},this);
},_onKeyPress:function(e,_29){
if(this.disabled||e.altKey||!(_29||e.ctrlKey)){
return;
}
var k=_9,c=e.charOrCode,ltr=_6._isBodyLtr(),_2a=null;
if((_29&&c==k.UP_ARROW)||(e.ctrlKey&&c==k.PAGE_UP)){
_2a=false;
}else{
if((_29&&c==k.DOWN_ARROW)||(e.ctrlKey&&(c==k.PAGE_DOWN||c==k.TAB))){
_2a=true;
}else{
if(c==(ltr?k.LEFT_ARROW:k.RIGHT_ARROW)){
_2a=this._focusOnRemoveBtn?null:false;
this._focusOnRemoveBtn=!this._focusOnRemoveBtn;
}else{
if(c==(ltr?k.RIGHT_ARROW:k.LEFT_ARROW)){
_2a=this._focusOnRemoveBtn?true:null;
this._focusOnRemoveBtn=!this._focusOnRemoveBtn;
}else{
return;
}
}
}
}
if(_2a!==null){
this._adjacent(_2a)._buttonWidget._onTitleClick();
}
_5.stop(e);
_b.scrollIntoView(this.selectedChildWidget._buttonWidget.domNode.parentNode);
if(_7("ie")){
this.selectedChildWidget._removeCBoxBtn.focusNode.setAttribute("tabIndex",this._focusOnRemoveBtn?_1d.accordionTitle:-1);
}
_1a.focus(this.selectedChildWidget[this._focusOnRemoveBtn?"_removeCBoxBtn":"_buttonWidget"].focusNode);
},_modifyChild:function(_2b,_2c){
if(!_2b||!this._started){
return;
}
_6.style(_2b.domNode,"overflow","hidden");
_2b._buttonWidget.connect(_2b._buttonWidget,"_setSelectedAttr",function(){
this.focusNode.setAttribute("tabIndex",this.selected?_1d.accordionTitle:"-1");
});
var _2d=this;
_2b._buttonWidget.connect(_2b._buttonWidget.domNode,"onclick",function(){
_2d._focusOnRemoveBtn=false;
});
(_2b._removeCBoxBtn=new _14({label:this.nls.removeRuleButton,showLabel:false,iconClass:"dojoxGridFCBoxRemoveCBoxBtnIcon",tabIndex:_1d.removeCBoxBtn,onClick:_4.hitch(_2b.content,"onRemove"),onKeyPress:function(e){
_2d._onKeyPress(e,_2b._buttonWidget.contentWidget);
}})).placeAt(_2b._buttonWidget.domNode);
var i,_2e=this.getChildren();
if(_2e.length===1){
_2b._buttonWidget.set("selected",true);
_6.style(_2b._removeCBoxBtn.domNode,"display","none");
}else{
for(i=0;i<_2e.length;++i){
if(_2e[i]._removeCBoxBtn){
_6.style(_2e[i]._removeCBoxBtn.domNode,"display","");
}
}
}
this._setupTitleDom(_2b);
if(!this._titleHeight){
for(i=0;i<_2e.length;++i){
if(_2e[i]!=this.selectedChildWidget){
this._titleHeight=_6.marginBox(_2e[i]._buttonWidget.domNode.parentNode).h;
break;
}
}
}
if(!_2c){
this._hackHeight(true,this._titleHeight);
}
},_hackHeight:function(_2f,_30){
var _31=this.getChildren(),dn=this.domNode,h=_6.style(dn,"height");
if(!_2f){
dn.style.height=(h-_30)+"px";
}else{
if(_31.length>1){
dn.style.height=(h+_30)+"px";
}else{
return;
}
}
this.resize();
},_setupTitleDom:function(_32){
var w=_6.contentBox(_32._buttonWidget.titleNode).w;
if(_7("ie")<8){
w-=8;
}
_6.style(_32._buttonWidget.titleTextNode,"width",w+"px");
}});
var _33=_1("dojox.grid.enhanced.plugins.filter.FilterDefPane",[_17,_18,_19],{templateString:_8("dojox.grid","enhanced/templates/FilterDefPane.html"),widgetsInTemplate:true,dlg:null,postMixInProperties:function(){
this.plugin=this.dlg.plugin;
var nls=this.plugin.nls;
this._addRuleBtnLabel=nls.addRuleButton;
this._cancelBtnLabel=nls.cancelButton;
this._clearBtnLabel=nls.clearButton;
this._filterBtnLabel=nls.filterButton;
this._relAll=nls.relationAll;
this._relAny=nls.relationAny;
this._relMsgFront=nls.relationMsgFront;
this._relMsgTail=nls.relationMsgTail;
},postCreate:function(){
this.inherited(arguments);
this.connect(this.domNode,"onkeypress","_onKey");
(this.cboxContainer=new _1e({nls:this.plugin.nls})).placeAt(this.criteriaPane);
this._relSelect.set("tabIndex",_1d.relSelect);
this._addCBoxBtn.set("tabIndex",_1d.addCBoxBtn);
this._cancelBtn.set("tabIndex",_1d.cancelBtn);
this._clearFilterBtn.set("tabIndex",_1d.clearBtn);
this._filterBtn.set("tabIndex",_1d.filterBtn);
var nls=this.plugin.nls;
this._relSelect.domNode.setAttribute("aria-label",nls.waiRelAll);
this._addCBoxBtn.domNode.setAttribute("aria-label",nls.waiAddRuleButton);
this._cancelBtn.domNode.setAttribute("aria-label",nls.waiCancelButton);
this._clearFilterBtn.domNode.setAttribute("aria-label",nls.waiClearButton);
this._filterBtn.domNode.setAttribute("aria-label",nls.waiFilterButton);
this._relSelect.set("value",this.dlg._relOpCls==="logicall"?"0":"1");
},uninitialize:function(){
this.cboxContainer.destroyRecursive();
this.plugin=null;
this.dlg=null;
},_onRelSelectChange:function(val){
this.dlg._relOpCls=val=="0"?"logicall":"logicany";
this._relSelect.domNode.setAttribute("aria-label",this.plugin.nls[val=="0"?"waiRelAll":"waiRelAny"]);
},_onAddCBox:function(){
this.dlg.addCriteriaBoxes(1);
},_onCancel:function(){
this.dlg.onCancel();
},_onClearFilter:function(){
this.dlg.onClearFilter();
},_onFilter:function(){
this.dlg.onFilter();
},_onKey:function(e){
if(e.keyCode==_9.ENTER){
this.dlg.onFilter();
}
}});
var _34=_1("dojox.grid.enhanced.plugins.filter.CriteriaBox",[_17,_18,_19],{templateString:_8("dojox.grid","enhanced/templates/CriteriaBox.html"),widgetsInTemplate:true,dlg:null,postMixInProperties:function(){
this.plugin=this.dlg.plugin;
this._curValueBox=null;
var nls=this.plugin.nls;
this._colSelectLabel=nls.columnSelectLabel;
this._condSelectLabel=nls.conditionSelectLabel;
this._valueBoxLabel=nls.valueBoxLabel;
this._anyColumnOption=nls.anyColumnOption;
},postCreate:function(){
var dlg=this.dlg,g=this.plugin.grid;
this._colSelect.set("tabIndex",_1d.colSelect);
this._colOptions=this._getColumnOptions();
this._colSelect.addOption([{label:this.plugin.nls.anyColumnOption,value:"anycolumn",selected:dlg.curColIdx<0},{value:""}].concat(this._colOptions));
this._condSelect.set("tabIndex",_1d.condSelect);
this._condSelect.addOption(this._getUsableConditions(dlg.getColumnType(dlg.curColIdx)));
this._showSelectOrLabel(this._condSelect,this._condSelectAlt);
this.connect(g.layout,"moveColumn","onMoveColumn");
var _35=this;
setTimeout(function(){
var _36=dlg.getColumnType(dlg.curColIdx);
_35._setValueBoxByType(_36);
},0);
},_getColumnOptions:function(){
var _37=this.dlg.curColIdx>=0?String(this.dlg.curColIdx):"anycolumn";
return _2.map(_2.filter(this.plugin.grid.layout.cells,function(_38){
return !(_38.filterable===false||_38.hidden);
}),function(_39){
return {label:_39.name||_39.field,value:String(_39.index),selected:_37==String(_39.index)};
});
},onMoveColumn:function(){
var tmp=this._onChangeColumn;
this._onChangeColumn=function(){
};
var _3a=this._colSelect.get("selectedOptions");
this._colSelect.removeOption(this._colOptions);
this._colOptions=this._getColumnOptions();
this._colSelect.addOption(this._colOptions);
var i=0;
for(;i<this._colOptions.length;++i){
if(this._colOptions[i].label==_3a.label){
break;
}
}
if(i<this._colOptions.length){
this._colSelect.set("value",this._colOptions[i].value);
}
var _3b=this;
setTimeout(function(){
_3b._onChangeColumn=tmp;
},0);
},onRemove:function(){
this.dlg.removeCriteriaBoxes(this);
},uninitialize:function(){
if(this._curValueBox){
this._curValueBox.destroyRecursive();
this._curValueBox=null;
}
this.plugin=null;
this.dlg=null;
},_showSelectOrLabel:function(sel,alt){
var _3c=sel.getOptions();
if(_3c.length==1){
alt.innerHTML=_3c[0].label;
_6.style(sel.domNode,"display","none");
_6.style(alt,"display","");
}else{
_6.style(sel.domNode,"display","");
_6.style(alt,"display","none");
}
},_onChangeColumn:function(val){
this._checkValidCriteria();
var _3d=this.dlg.getColumnType(val);
this._setConditionsByType(_3d);
this._setValueBoxByType(_3d);
this._updateValueBox();
},_onChangeCondition:function(val){
this._checkValidCriteria();
var f=(val=="range");
if(f^this._isRange){
this._isRange=f;
this._setValueBoxByType(this.dlg.getColumnType(this._colSelect.get("value")));
}
this._updateValueBox();
},_updateValueBox:function(_3e){
this._curValueBox.set("disabled",this._condSelect.get("value")=="isempty");
},_checkValidCriteria:function(){
setTimeout(_4.hitch(this,function(){
this.updateRuleTitle();
this.dlg._updatePane();
}),0);
},_createValueBox:function(cls,arg){
var _3f=_4.hitch(arg.cbox,"_checkValidCriteria");
return new cls(_4.mixin(arg,{tabIndex:_1d.valueBox,onKeyPress:_3f,onChange:_3f,"class":"dojoxGridFCBoxValueBox"}));
},_createRangeBox:function(cls,arg){
var _40=_4.hitch(arg.cbox,"_checkValidCriteria");
_4.mixin(arg,{tabIndex:_1d.valueBox,onKeyPress:_40,onChange:_40});
var div=_6.create("div",{"class":"dojoxGridFCBoxValueBox"}),_41=new cls(arg),txt=_6.create("span",{"class":"dojoxGridFCBoxRangeValueTxt","innerHTML":this.plugin.nls.rangeTo}),end=new cls(arg);
_6.addClass(_41.domNode,"dojoxGridFCBoxStartValue");
_6.addClass(end.domNode,"dojoxGridFCBoxEndValue");
div.appendChild(_41.domNode);
div.appendChild(txt);
div.appendChild(end.domNode);
div.domNode=div;
div.set=function(_42,_43){
if(_4.isObject(_43)){
_41.set("value",_43.start);
end.set("value",_43.end);
}
};
div.get=function(){
var s=_41.get("value"),e=end.get("value");
return s&&e?{start:s,end:e}:"";
};
return div;
},changeCurrentColumn:function(_44){
var _45=this.dlg.curColIdx;
this._colSelect.removeOption(this._colOptions);
this._colOptions=this._getColumnOptions();
this._colSelect.addOption(this._colOptions);
this._colSelect.set("value",_45>=0?String(_45):"anycolumn");
this.updateRuleTitle(true);
},curColumn:function(){
return this._colSelect.getOptions(this._colSelect.get("value")).label;
},curCondition:function(){
return this._condSelect.getOptions(this._condSelect.get("value")).label;
},curValue:function(){
var _46=this._condSelect.get("value");
if(_46=="isempty"){
return "";
}
return this._curValueBox?this._curValueBox.get("value"):"";
},save:function(){
if(this.isEmpty()){
return null;
}
var _47=this._colSelect.get("value"),_48=this.dlg.getColumnType(_47),_49=this.curValue(),_4a=this._condSelect.get("value");
return {"column":_47,"condition":_4a,"value":_49,"formattedVal":this.formatValue(_48,_4a,_49),"type":_48,"colTxt":this.curColumn(),"condTxt":this.curCondition()};
},load:function(obj){
var tmp=[this._onChangeColumn,this._onChangeCondition];
this._onChangeColumn=this._onChangeCondition=function(){
};
if(obj.column){
this._colSelect.set("value",obj.column);
}
if(obj.type){
this._setConditionsByType(obj.type);
this._setValueBoxByType(obj.type);
}else{
obj.type=this.dlg.getColumnType(this._colSelect.get("value"));
}
if(obj.condition){
this._condSelect.set("value",obj.condition);
}
var _4b=obj.value||"";
if(_4b||(obj.type!="date"&&obj.type!="time")){
this._curValueBox.set("value",_4b);
}
this._updateValueBox();
setTimeout(_4.hitch(this,function(){
this._onChangeColumn=tmp[0];
this._onChangeCondition=tmp[1];
}),0);
},getExpr:function(){
if(this.isEmpty()){
return null;
}
var _4c=this._colSelect.get("value");
return this.dlg.getExprForCriteria({"type":this.dlg.getColumnType(_4c),"column":_4c,"condition":this._condSelect.get("value"),"value":this.curValue()});
},isEmpty:function(){
var _4d=this._condSelect.get("value");
if(_4d=="isempty"){
return false;
}
var v=this.curValue();
return v===""||v===null||typeof v=="undefined"||(typeof v=="number"&&isNaN(v));
},updateRuleTitle:function(_4e){
var _4f=this._pane._buttonWidget.titleTextNode;
var _50=["<div class='dojoxEllipsis'>"];
if(_4e||this.isEmpty()){
_4f.title=_a.substitute(this.plugin.nls.ruleTitleTemplate,[this._ruleIndex||1]);
_50.push(_4f.title);
}else{
var _51=this.dlg.getColumnType(this._colSelect.get("value"));
var _52=this.curColumn();
var _53=this.curCondition();
var _54=this.formatValue(_51,this._condSelect.get("value"),this.curValue());
_50.push(_52,"&nbsp;<span class='dojoxGridRuleTitleCondition'>",_53,"</span>&nbsp;",_54);
_4f.title=[_52," ",_53," ",_54].join("");
}
_4f.innerHTML=_50.join("");
if(_7("mozilla")){
var tt=_6.create("div",{"style":"width: 100%; height: 100%; position: absolute; top: 0; left: 0; z-index: 9999;"},_4f);
tt.title=_4f.title;
}
},updateRuleIndex:function(_55){
if(this._ruleIndex!=_55){
this._ruleIndex=_55;
if(this.isEmpty()){
this.updateRuleTitle();
}
}
},setAriaInfo:function(idx){
var dss=_a.substitute,nls=this.plugin.nls;
this._colSelect.domNode.setAttribute("aria-label",dss(nls.waiColumnSelectTemplate,[idx]));
this._condSelect.domNode.setAttribute("aria-label",dss(nls.waiConditionSelectTemplate,[idx]));
this._pane._removeCBoxBtn.domNode.setAttribute("aria-label",dss(nls.waiRemoveRuleButtonTemplate,[idx]));
this._index=idx;
},_getUsableConditions:function(_56){
var _57=_4.clone(this.dlg._dataTypeMap[_56].conditions);
var _58=(this.plugin.args.disabledConditions||{})[_56];
var _59=parseInt(this._colSelect.get("value"),10);
var _5a=isNaN(_59)?(this.plugin.args.disabledConditions||{})["anycolumn"]:this.plugin.grid.layout.cells[_59].disabledConditions;
if(!_4.isArray(_58)){
_58=[];
}
if(!_4.isArray(_5a)){
_5a=[];
}
var arr=_58.concat(_5a);
if(arr.length){
var _5b={};
_2.forEach(arr,function(c){
if(_4.isString(c)){
_5b[c.toLowerCase()]=true;
}
});
return _2.filter(_57,function(_5c){
return !(_5c.value in _5b);
});
}
return _57;
},_setConditionsByType:function(_5d){
var _5e=this._condSelect;
_5e.removeOption(_5e.options);
_5e.addOption(this._getUsableConditions(_5d));
this._showSelectOrLabel(this._condSelect,this._condSelectAlt);
},_setValueBoxByType:function(_5f){
if(this._curValueBox){
this.valueNode.removeChild(this._curValueBox.domNode);
try{
this._curValueBox.destroyRecursive();
}
catch(e){
}
delete this._curValueBox;
}
var _60=this.dlg._dataTypeMap[_5f].valueBoxCls[this._getValueBoxClsInfo(this._colSelect.get("value"),_5f)],_61=this._getValueBoxArgByType(_5f);
this._curValueBox=this[this._isRange?"_createRangeBox":"_createValueBox"](_60,_61);
this.valueNode.appendChild(this._curValueBox.domNode);
this._curValueBox.domNode.setAttribute("aria-label",_a.substitute(this.plugin.nls.waiValueBoxTemplate,[this._index]));
this.dlg.onRendered(this);
},_getValueBoxArgByType:function(_62){
var g=this.plugin.grid,_63=g.layout.cells[parseInt(this._colSelect.get("value"),10)],res={cbox:this};
if(_62=="string"){
if(_63&&(_63.suggestion||_63.autoComplete)){
_4.mixin(res,{store:g.store,searchAttr:_63.field||_63.name,query:g.query||{},fetchProperties:{sort:[{"attribute":_63.field||_63.name}],queryOptions:_4.mixin({ignoreCase:true,deep:true},g.queryOptions||{})}});
}
}else{
if(_62=="boolean"){
_4.mixin(res,this.dlg.builder.defaultArgs["boolean"]);
}
}
if(_63&&_63.dataTypeArgs){
_4.mixin(res,_63.dataTypeArgs);
}
return res;
},formatValue:function(_64,_65,v){
if(_65=="isempty"){
return "";
}
if(_64=="date"||_64=="time"){
var opt={selector:_64},fmt=_c.format;
if(_65=="range"){
return _a.substitute(this.plugin.nls.rangeTemplate,[fmt(v.start,opt),fmt(v.end,opt)]);
}
return fmt(v,opt);
}else{
if(_64=="boolean"){
return v?this._curValueBox._lblTrue:this._curValueBox._lblFalse;
}
}
return v;
},_getValueBoxClsInfo:function(_66,_67){
var _68=this.plugin.grid.layout.cells[parseInt(_66,10)];
if(_67=="string"){
return (_68&&(_68.suggestion||_68.autoComplete))?"ac":"dft";
}
return "dft";
}});
var _69=_1("dojox.grid.enhanced.plugins.filter.UniqueComboBox",_f,{_openResultList:function(_6a){
var _6b={},s=this.store,_6c=this.searchAttr;
arguments[0]=_2.filter(_6a,function(_6d){
var key=s.getValue(_6d,_6c),_6e=_6b[key];
_6b[key]=true;
return !_6e;
});
this.inherited(arguments);
},_onKey:function(evt){
if(evt.charOrCode===_9.ENTER&&this._opened){
_5.stop(evt);
}
this.inherited(arguments);
}});
var _6f=_1("dojox.grid.enhanced.plugins.filter.BooleanValueBox",[_17,_18,_19],{templateString:_8("dojox.grid","enhanced/templates/FilterBoolValueBox.html"),widgetsInTemplate:true,constructor:function(_70){
var nls=_70.cbox.plugin.nls;
this._baseId=_70.cbox.id;
this._lblTrue=_70.trueLabel||nls.trueLabel||"true";
this._lblFalse=_70.falseLabel||nls.falseLabel||"false";
this.args=_70;
},postCreate:function(){
this.onChange();
},onChange:function(){
},get:function(_71){
return this.rbTrue.get("checked");
},set:function(_72,v){
this.inherited(arguments);
if(_72=="value"){
this.rbTrue.set("checked",!!v);
this.rbFalse.set("checked",!v);
}
}});
var _73=_1("dojox.grid.enhanced.plugins.filter.FilterDefDialog",null,{curColIdx:-1,_relOpCls:"logicall",_savedCriterias:null,plugin:null,constructor:function(_74){
var _75=this.plugin=_74.plugin;
this.builder=new _d();
this._setupData();
this._cboxes=[];
this.defaultType=_75.args.defaultType||"string";
(this.filterDefPane=new _33({"dlg":this})).startup();
(this._defPane=new _e({"refNode":this.plugin.grid.domNode,"title":_75.nls.filterDefDialogTitle,"class":"dojoxGridFDTitlePane","iconClass":"dojoxGridFDPaneIcon","content":this.filterDefPane})).startup();
this._defPane.connect(_75.grid.layer("filter"),"filterDef",_4.hitch(this,"_onSetFilter"));
_75.grid.setFilter=_4.hitch(this,"setFilter");
_75.grid.getFilter=_4.hitch(this,"getFilter");
_75.grid.getFilterRelation=_4.hitch(this,function(){
return this._relOpCls;
});
_75.connect(_75.grid.layout,"moveColumn",_4.hitch(this,"onMoveColumn"));
},onMoveColumn:function(_76,_77,_78,_79,_7a){
if(this._savedCriterias&&_78!=_79){
if(_7a){
--_79;
}
var min=_78<_79?_78:_79;
var max=_78<_79?_79:_78;
var dir=_79>min?1:-1;
_2.forEach(this._savedCriterias,function(sc){
var idx=parseInt(sc.column,10);
if(!isNaN(idx)&&idx>=min&&idx<=max){
sc.column=String(idx==_78?idx+(max-min)*dir:idx-dir);
}
});
}
},destroy:function(){
this._defPane.destroyRecursive();
this._defPane=null;
this.filterDefPane=null;
this.builder=null;
this._dataTypeMap=null;
this._cboxes=null;
var g=this.plugin.grid;
g.setFilter=null;
g.getFilter=null;
g.getFilterRelation=null;
this.plugin=null;
},_setupData:function(){
var nls=this.plugin.nls;
this._dataTypeMap={"number":{valueBoxCls:{dft:_11},conditions:[{label:nls.conditionEqual,value:"equalto",selected:true},{label:nls.conditionNotEqual,value:"notequalto"},{label:nls.conditionLess,value:"lessthan"},{label:nls.conditionLessEqual,value:"lessthanorequalto"},{label:nls.conditionLarger,value:"largerthan"},{label:nls.conditionLargerEqual,value:"largerthanorequalto"},{label:nls.conditionIsEmpty,value:"isempty"}]},"string":{valueBoxCls:{dft:_10,ac:_69},conditions:[{label:nls.conditionContains,value:"contains",selected:true},{label:nls.conditionIs,value:"equalto"},{label:nls.conditionStartsWith,value:"startswith"},{label:nls.conditionEndWith,value:"endswith"},{label:nls.conditionNotContain,value:"notcontains"},{label:nls.conditionIsNot,value:"notequalto"},{label:nls.conditionNotStartWith,value:"notstartswith"},{label:nls.conditionNotEndWith,value:"notendswith"},{label:nls.conditionIsEmpty,value:"isempty"}]},"date":{valueBoxCls:{dft:_12},conditions:[{label:nls.conditionIs,value:"equalto",selected:true},{label:nls.conditionBefore,value:"lessthan"},{label:nls.conditionAfter,value:"largerthan"},{label:nls.conditionRange,value:"range"},{label:nls.conditionIsEmpty,value:"isempty"}]},"time":{valueBoxCls:{dft:_13},conditions:[{label:nls.conditionIs,value:"equalto",selected:true},{label:nls.conditionBefore,value:"lessthan"},{label:nls.conditionAfter,value:"largerthan"},{label:nls.conditionRange,value:"range"},{label:nls.conditionIsEmpty,value:"isempty"}]},"boolean":{valueBoxCls:{dft:_6f},conditions:[{label:nls.conditionIs,value:"equalto",selected:true},{label:nls.conditionIsEmpty,value:"isempty"}]}};
},setFilter:function(_7b,_7c){
_7b=_7b||[];
if(!_4.isArray(_7b)){
_7b=[_7b];
}
var _7d=function(){
if(_7b.length){
this._savedCriterias=_2.map(_7b,function(_7e){
var _7f=_7e.type||this.defaultType;
return {"type":_7f,"column":String(_7e.column),"condition":_7e.condition,"value":_7e.value,"colTxt":this.getColumnLabelByValue(String(_7e.column)),"condTxt":this.getConditionLabelByValue(_7f,_7e.condition),"formattedVal":_7e.formattedVal||_7e.value};
},this);
this._criteriasChanged=true;
if(_7c==="logicall"||_7c==="logicany"){
this._relOpCls=_7c;
}
var _80=_2.map(_7b,this.getExprForCriteria,this);
_80=this.builder.buildExpression(_80.length==1?_80[0]:{"op":this._relOpCls,"data":_80});
this.plugin.grid.layer("filter").filterDef(_80);
this.plugin.filterBar.toggleClearFilterBtn(false);
}
this._closeDlgAndUpdateGrid();
};
if(this._savedCriterias){
this._clearWithoutRefresh=true;
var _81=_3.connect(this,"clearFilter",this,function(){
_3.disconnect(_81);
this._clearWithoutRefresh=false;
_7d.apply(this);
});
this.onClearFilter();
}else{
_7d.apply(this);
}
},getFilter:function(){
return _4.clone(this._savedCriterias)||[];
},getColumnLabelByValue:function(v){
var nls=this.plugin.nls;
if(v.toLowerCase()=="anycolumn"){
return nls["anyColumnOption"];
}else{
var _82=this.plugin.grid.layout.cells[parseInt(v,10)];
return _82?(_82.name||_82.field):"";
}
},getConditionLabelByValue:function(_83,c){
var _84=this._dataTypeMap[_83].conditions;
for(var i=_84.length-1;i>=0;--i){
var _85=_84[i];
if(_85.value==c.toLowerCase()){
return _85.label;
}
}
return "";
},addCriteriaBoxes:function(cnt){
if(typeof cnt!="number"||cnt<=0){
return;
}
var cbs=this._cboxes,cc=this.filterDefPane.cboxContainer,_86=this.plugin.args.ruleCount,len=cbs.length,_87;
if(_86>0&&len+cnt>_86){
cnt=_86-len;
}
for(;cnt>0;--cnt){
_87=new _34({dlg:this});
cbs.push(_87);
cc.addChild(_87);
}
cc.startup();
this._updatePane();
this._updateCBoxTitles();
cc.selectChild(cbs[cbs.length-1]);
this.filterDefPane.criteriaPane.scrollTop=1000000;
if(cbs.length===4){
if(_7("ie")<=6&&!this.__alreadyResizedForIE6){
var _88=_6.position(cc.domNode);
_88.w-=_1b.getScrollbar().w;
cc.resize(_88);
this.__alreadyResizedForIE6=true;
}else{
cc.resize();
}
}
},removeCriteriaBoxes:function(cnt,_89){
var cbs=this._cboxes,cc=this.filterDefPane.cboxContainer,len=cbs.length,_8a=len-cnt,end=len-1,_8b,_8c=_2.indexOf(cbs,cc.selectedChildWidget.content);
if(_4.isArray(cnt)){
var i,_8d=cnt;
_8d.sort();
cnt=_8d.length;
for(i=len-1;i>=0&&_2.indexOf(_8d,i)>=0;--i){
}
if(i>=0){
if(i!=_8c){
cc.selectChild(cbs[i]);
}
for(i=cnt-1;i>=0;--i){
if(_8d[i]>=0&&_8d[i]<len){
cc.removeChild(cbs[_8d[i]]);
cbs.splice(_8d[i],1);
}
}
}
_8a=cbs.length;
}else{
if(_89===true){
if(cnt>=0&&cnt<len){
_8a=end=cnt;
cnt=1;
}else{
return;
}
}else{
if(cnt instanceof _34){
_8b=cnt;
cnt=1;
_8a=end=_2.indexOf(cbs,_8b);
}else{
if(typeof cnt!="number"||cnt<=0){
return;
}else{
if(cnt>=len){
cnt=end;
_8a=1;
}
}
}
}
if(end<_8a){
return;
}
if(_8c>=_8a&&_8c<=end){
cc.selectChild(cbs[_8a?_8a-1:end+1]);
}
for(;end>=_8a;--end){
cc.removeChild(cbs[end]);
}
cbs.splice(_8a,cnt);
}
this._updatePane();
this._updateCBoxTitles();
if(cbs.length===3){
cc.resize();
}
},getCriteria:function(idx){
if(typeof idx!="number"){
return this._savedCriterias?this._savedCriterias.length:0;
}
if(this._savedCriterias&&this._savedCriterias[idx]){
return _4.mixin({relation:this._relOpCls=="logicall"?this.plugin.nls.and:this.plugin.nls.or},this._savedCriterias[idx]);
}
return null;
},getExprForCriteria:function(_8e){
if(_8e.column=="anycolumn"){
var _8f=_2.filter(this.plugin.grid.layout.cells,function(_90){
return !(_90.filterable===false||_90.hidden);
});
return {"op":"logicany","data":_2.map(_8f,function(_91){
return this.getExprForColumn(_8e.value,_91.index,_8e.type,_8e.condition);
},this)};
}else{
return this.getExprForColumn(_8e.value,_8e.column,_8e.type,_8e.condition);
}
},getExprForColumn:function(_92,_93,_94,_95){
_93=parseInt(_93,10);
var _96=this.plugin.grid.layout.cells[_93],_97=_96.field||_96.name,obj={"datatype":_94||this.getColumnType(_93),"args":_96.dataTypeArgs,"isColumn":true},_98=[_4.mixin({"data":this.plugin.args.isServerSide?_97:_96},obj)];
obj.isColumn=false;
if(_95=="range"){
_98.push(_4.mixin({"data":_92.start},obj),_4.mixin({"data":_92.end},obj));
}else{
if(_95!="isempty"){
_98.push(_4.mixin({"data":_92},obj));
}
}
return {"op":_95,"data":_98};
},getColumnType:function(_99){
var _9a=this.plugin.grid.layout.cells[parseInt(_99,10)];
if(!_9a||!_9a.datatype){
return this.defaultType;
}
var _9b=String(_9a.datatype).toLowerCase();
return this._dataTypeMap[_9b]?_9b:this.defaultType;
},clearFilter:function(_9c){
if(!this._savedCriterias){
return;
}
this._savedCriterias=null;
this.plugin.grid.layer("filter").filterDef(null);
try{
this.plugin.filterBar.toggleClearFilterBtn(true);
this.filterDefPane._clearFilterBtn.set("disabled",true);
this.removeCriteriaBoxes(this._cboxes.length-1);
this._cboxes[0].load({});
}
catch(e){
}
if(_9c){
this.closeDialog();
}else{
this._closeDlgAndUpdateGrid();
}
},showDialog:function(_9d){
this._defPane.show();
this.plugin.filterStatusTip.closeDialog();
this._prepareDialog(_9d);
},closeDialog:function(){
if(this._defPane.open){
this._defPane.hide();
}
},onFilter:function(e){
if(this.canFilter()){
this._defineFilter();
this._closeDlgAndUpdateGrid();
this.plugin.filterBar.toggleClearFilterBtn(false);
}
},onClearFilter:function(e){
if(this._savedCriterias){
if(this._savedCriterias.length>=this.plugin.ruleCountToConfirmClearFilter){
this.plugin.clearFilterDialog.show();
}else{
this.clearFilter(this._clearWithoutRefresh);
}
}
},onCancel:function(e){
var sc=this._savedCriterias;
var cbs=this._cboxes;
if(sc){
this.addCriteriaBoxes(sc.length-cbs.length);
this.removeCriteriaBoxes(cbs.length-sc.length);
_2.forEach(sc,function(c,i){
cbs[i].load(c);
});
}else{
this.removeCriteriaBoxes(cbs.length-1);
cbs[0].load({});
}
this.closeDialog();
},onRendered:function(_9e){
if(!_7("ff")){
var _9f=_1c._getTabNavigable(_6.byId(_9e.domNode));
_1a.focus(_9f.lowest||_9f.first);
}else{
var dp=this._defPane;
dp._getFocusItems(dp.domNode);
_1a.focus(dp._firstFocusItem);
}
},_onSetFilter:function(_a0){
if(_a0===null&&this._savedCriterias){
this.clearFilter();
}
},_prepareDialog:function(_a1){
var sc=this._savedCriterias,cbs=this._cboxes,i,_a2;
this.curColIdx=_a1;
if(!sc){
if(cbs.length===0){
this.addCriteriaBoxes(1);
}else{
for(i=0;(_a2=cbs[i]);++i){
_a2.changeCurrentColumn();
}
}
}else{
if(this._criteriasChanged){
this.filterDefPane._relSelect.set("value",this._relOpCls==="logicall"?"0":"1");
this._criteriasChanged=false;
var _a3=sc.length>cbs.length?sc.length-cbs.length:0;
this.addCriteriaBoxes(_a3);
this.removeCriteriaBoxes(cbs.length-sc.length);
this.filterDefPane._clearFilterBtn.set("disabled",false);
for(i=0;i<cbs.length-_a3;++i){
cbs[i].load(sc[i]);
}
if(_a3>0){
var _a4=[],_a5=_3.connect(this,"onRendered",function(_a6){
var i=_2.indexOf(cbs,_a6);
if(!_a4[i]){
_a4[i]=true;
if(--_a3===0){
_3.disconnect(_a5);
}
_a6.load(sc[i]);
}
});
}
}
}
this.filterDefPane.cboxContainer.resize();
},_defineFilter:function(){
var cbs=this._cboxes,_a7=function(_a8){
return _2.filter(_2.map(cbs,function(_a9){
return _a9[_a8]();
}),function(_aa){
return !!_aa;
});
},_ab=_a7("getExpr");
this._savedCriterias=_a7("save");
_ab=_ab.length==1?_ab[0]:{"op":this._relOpCls,"data":_ab};
_ab=this.builder.buildExpression(_ab);
this.plugin.grid.layer("filter").filterDef(_ab);
this.filterDefPane._clearFilterBtn.set("disabled",false);
},_updateCBoxTitles:function(){
for(var cbs=this._cboxes,i=cbs.length;i>0;--i){
cbs[i-1].updateRuleIndex(i);
cbs[i-1].setAriaInfo(i);
}
},_updatePane:function(){
var cbs=this._cboxes,_ac=this.filterDefPane;
_ac._addCBoxBtn.set("disabled",cbs.length==this.plugin.args.ruleCount);
_ac._filterBtn.set("disabled",!this.canFilter());
},canFilter:function(){
return _2.filter(this._cboxes,function(_ad){
return !_ad.isEmpty();
}).length>0;
},_closeDlgAndUpdateGrid:function(){
this.closeDialog();
var g=this.plugin.grid;
g.showMessage(g.loadingMessage);
setTimeout(_4.hitch(g,g._refresh),this._defPane.duration+10);
}});
return _73;
});
