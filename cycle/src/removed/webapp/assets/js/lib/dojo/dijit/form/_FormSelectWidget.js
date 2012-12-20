//>>built
define("dijit/form/_FormSelectWidget",["dojo/_base/array","dojo/_base/Deferred","dojo/aspect","dojo/data/util/sorter","dojo/_base/declare","dojo/dom","dojo/dom-class","dojo/_base/kernel","dojo/_base/lang","dojo/query","dojo/when","dojo/store/util/QueryResults","./_FormValueWidget"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d){
var _e=_5("dijit.form._FormSelectWidget",_d,{multiple:false,options:null,store:null,query:null,queryOptions:null,labelAttr:"",onFetch:null,sortByLabel:true,loadChildrenOnOpen:false,onLoadDeferred:null,getOptions:function(_f){
var _10=_f,_11=this.options||[],l=_11.length;
if(_10===undefined){
return _11;
}
if(_9.isArray(_10)){
return _1.map(_10,"return this.getOptions(item);",this);
}
if(_9.isObject(_f)){
if(!_1.some(this.options,function(o,idx){
if(o===_10||(o.value&&o.value===_10.value)){
_10=idx;
return true;
}
return false;
})){
_10=-1;
}
}
if(typeof _10=="string"){
for(var i=0;i<l;i++){
if(_11[i].value===_10){
_10=i;
break;
}
}
}
if(typeof _10=="number"&&_10>=0&&_10<l){
return this.options[_10];
}
return null;
},addOption:function(_12){
if(!_9.isArray(_12)){
_12=[_12];
}
_1.forEach(_12,function(i){
if(i&&_9.isObject(i)){
this.options.push(i);
}
},this);
this._loadChildren();
},removeOption:function(_13){
if(!_9.isArray(_13)){
_13=[_13];
}
var _14=this.getOptions(_13);
_1.forEach(_14,function(i){
if(i){
this.options=_1.filter(this.options,function(_15){
return (_15.value!==i.value||_15.label!==i.label);
});
this._removeOptionItem(i);
}
},this);
this._loadChildren();
},updateOption:function(_16){
if(!_9.isArray(_16)){
_16=[_16];
}
_1.forEach(_16,function(i){
var _17=this.getOptions(i),k;
if(_17){
for(k in i){
_17[k]=i[k];
}
}
},this);
this._loadChildren();
},setStore:function(_18,_19,_1a){
var _1b=this.store;
_1a=_1a||{};
if(_1b!==_18){
var h;
while((h=this._notifyConnections.pop())){
h.remove();
}
if(!_18.get){
_9.mixin(_18,{_oldAPI:true,get:function(id){
var _1c=new _2();
this.fetchItemByIdentity({identity:id,onItem:function(_1d){
_1c.resolve(_1d);
},onError:function(_1e){
_1c.reject(_1e);
}});
return _1c.promise;
},query:function(_1f,_20){
var _21=new _2(function(){
if(_22.abort){
_22.abort();
}
});
_21.total=new _2();
var _22=this.fetch(_9.mixin({query:_1f,onBegin:function(_23){
_21.total.resolve(_23);
},onComplete:function(_24){
_21.resolve(_24);
},onError:function(_25){
_21.reject(_25);
}},_20));
return new _c(_21);
}});
if(_18.getFeatures()["dojo.data.api.Notification"]){
this._notifyConnections=[_3.after(_18,"onNew",_9.hitch(this,"_onNewItem"),true),_3.after(_18,"onDelete",_9.hitch(this,"_onDeleteItem"),true),_3.after(_18,"onSet",_9.hitch(this,"_onSetItem"),true)];
}
}
this._set("store",_18);
}
if(this.options&&this.options.length){
this.removeOption(this.options);
}
if(this._queryRes&&this._queryRes.close){
this._queryRes.close();
}
if(_1a.query){
this._set("query",_1a.query);
this._set("queryOptions",_1a.queryOptions);
}
if(_18){
this._loadingStore=true;
this.onLoadDeferred=new _2();
this._queryRes=_18.query(this.query,this.queryOptions);
_b(this._queryRes,_9.hitch(this,function(_26){
if(this.sortByLabel&&!_1a.sort&&_26.length){
if(_26[0].getValue){
_26.sort(_4.createSortFunction([{attribute:_18.getLabelAttributes(_26[0])[0]}],_18));
}else{
var _27=this.labelAttr;
_26.sort(function(a,b){
return a[_27]>b[_27]?1:b[_27]>a[_27]?-1:0;
});
}
}
if(_1a.onFetch){
_26=_1a.onFetch.call(this,_26,_1a);
}
_1.forEach(_26,function(i){
this._addOptionForItem(i);
},this);
if(this._queryRes.observe){
this._queryRes.observe(_9.hitch(this,function(_28,_29,_2a){
if(_29==_2a){
this._onSetItem(_28);
}else{
if(_29!=-1){
this._onDeleteItem(_28);
}
if(_2a!=-1){
this._onNewItem(_28);
}
}
}),true);
}
this._loadingStore=false;
this.set("value","_pendingValue" in this?this._pendingValue:_19);
delete this._pendingValue;
if(!this.loadChildrenOnOpen){
this._loadChildren();
}else{
this._pseudoLoadChildren(_26);
}
this.onLoadDeferred.resolve(true);
this.onSetStore();
}),function(err){
console.error("dijit.form.Select: "+err.toString());
this.onLoadDeferred.reject(err);
});
}
return _1b;
},_setValueAttr:function(_2b,_2c){
if(!this._onChangeActive){
_2c=null;
}
if(this._loadingStore){
this._pendingValue=_2b;
return;
}
var _2d=this.getOptions()||[];
if(!_9.isArray(_2b)){
_2b=[_2b];
}
_1.forEach(_2b,function(i,idx){
if(!_9.isObject(i)){
i=i+"";
}
if(typeof i==="string"){
_2b[idx]=_1.filter(_2d,function(_2e){
return _2e.value===i;
})[0]||{value:"",label:""};
}
},this);
_2b=_1.filter(_2b,function(i){
return i&&i.value;
});
if(!this.multiple&&(!_2b[0]||!_2b[0].value)&&_2d.length){
_2b[0]=_2d[0];
}
_1.forEach(_2d,function(i){
i.selected=_1.some(_2b,function(v){
return v.value===i.value;
});
});
var val=_1.map(_2b,function(i){
return i.value;
}),_2f=_1.map(_2b,function(i){
return i.label;
});
if(typeof val=="undefined"||typeof val[0]=="undefined"){
return;
}
this._setDisplay(this.multiple?_2f:_2f[0]);
this.inherited(arguments,[this.multiple?val:val[0],_2c]);
this._updateSelection();
},_getDisplayedValueAttr:function(){
var val=this.get("value");
if(!_9.isArray(val)){
val=[val];
}
var ret=_1.map(this.getOptions(val),function(v){
if(v&&"label" in v){
return v.label;
}else{
if(v){
return v.value;
}
}
return null;
},this);
return this.multiple?ret:ret[0];
},_loadChildren:function(){
if(this._loadingStore){
return;
}
_1.forEach(this._getChildren(),function(_30){
_30.destroyRecursive();
});
_1.forEach(this.options,this._addOptionItem,this);
this._updateSelection();
},_updateSelection:function(){
this._set("value",this._getValueFromOpts());
var val=this.value;
if(!_9.isArray(val)){
val=[val];
}
if(val&&val[0]){
_1.forEach(this._getChildren(),function(_31){
var _32=_1.some(val,function(v){
return _31.option&&(v===_31.option.value);
});
_7.toggle(_31.domNode,this.baseClass.replace(/\s+|$/g,"SelectedOption "),_32);
_31.domNode.setAttribute("aria-selected",_32?"true":"false");
},this);
}
},_getValueFromOpts:function(){
var _33=this.getOptions()||[];
if(!this.multiple&&_33.length){
var opt=_1.filter(_33,function(i){
return i.selected;
})[0];
if(opt&&opt.value){
return opt.value;
}else{
_33[0].selected=true;
return _33[0].value;
}
}else{
if(this.multiple){
return _1.map(_1.filter(_33,function(i){
return i.selected;
}),function(i){
return i.value;
})||[];
}
}
return "";
},_onNewItem:function(_34,_35){
if(!_35||!_35.parent){
this._addOptionForItem(_34);
}
},_onDeleteItem:function(_36){
var _37=this.store;
this.removeOption(_37.getIdentity(_36));
},_onSetItem:function(_38){
this.updateOption(this._getOptionObjForItem(_38));
},_getOptionObjForItem:function(_39){
var _3a=this.store,_3b=(this.labelAttr&&this.labelAttr in _39)?_39[this.labelAttr]:_3a.getLabel(_39),_3c=(_3b?_3a.getIdentity(_39):null);
return {value:_3c,label:_3b,item:_39};
},_addOptionForItem:function(_3d){
var _3e=this.store;
if(_3e.isItemLoaded&&!_3e.isItemLoaded(_3d)){
_3e.loadItem({item:_3d,onItem:function(i){
this._addOptionForItem(i);
},scope:this});
return;
}
var _3f=this._getOptionObjForItem(_3d);
this.addOption(_3f);
},constructor:function(_40){
this._oValue=(_40||{}).value||null;
this._notifyConnections=[];
},buildRendering:function(){
this.inherited(arguments);
_6.setSelectable(this.focusNode,false);
},_fillContent:function(){
if(!this.options){
this.options=this.srcNodeRef?_a("> *",this.srcNodeRef).map(function(_41){
if(_41.getAttribute("type")==="separator"){
return {value:"",label:"",selected:false,disabled:false};
}
return {value:(_41.getAttribute("data-"+_8._scopeName+"-value")||_41.getAttribute("value")),label:String(_41.innerHTML),selected:_41.getAttribute("selected")||false,disabled:_41.getAttribute("disabled")||false};
},this):[];
}
if(!this.value){
this._set("value",this._getValueFromOpts());
}else{
if(this.multiple&&typeof this.value=="string"){
this._set("value",this.value.split(","));
}
}
},postCreate:function(){
this.inherited(arguments);
this.connect(this,"onChange","_updateSelection");
var _42=this.store;
if(_42&&(_42.getIdentity||_42.getFeatures()["dojo.data.api.Identity"])){
this.store=null;
this.setStore(_42,this._oValue);
}
},startup:function(){
this._loadChildren();
this.inherited(arguments);
},destroy:function(){
var h;
while((h=this._notifyConnections.pop())){
h.remove();
}
if(this._queryRes&&this._queryRes.close){
this._queryRes.close();
}
this.inherited(arguments);
},_addOptionItem:function(){
},_removeOptionItem:function(){
},_setDisplay:function(){
},_getChildren:function(){
return [];
},_getSelectedOptionsAttr:function(){
return this.getOptions(this.get("value"));
},_pseudoLoadChildren:function(){
},onSetStore:function(){
}});
return _e;
});
