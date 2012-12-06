//>>built
require({cache:{"url:dijit/templates/Calendar.html":"<table cellspacing=\"0\" cellpadding=\"0\" class=\"dijitCalendarContainer\" role=\"grid\" aria-labelledby=\"${id}_mddb ${id}_year\">\n\t<thead>\n\t\t<tr class=\"dijitReset dijitCalendarMonthContainer\" valign=\"top\">\n\t\t\t<th class='dijitReset dijitCalendarArrow' data-dojo-attach-point=\"decrementMonth\">\n\t\t\t\t<img src=\"${_blankGif}\" alt=\"\" class=\"dijitCalendarIncrementControl dijitCalendarDecrease\" role=\"presentation\"/>\n\t\t\t\t<span data-dojo-attach-point=\"decreaseArrowNode\" class=\"dijitA11ySideArrow\">-</span>\n\t\t\t</th>\n\t\t\t<th class='dijitReset' colspan=\"5\">\n\t\t\t\t<div data-dojo-attach-point=\"monthNode\">\n\t\t\t\t</div>\n\t\t\t</th>\n\t\t\t<th class='dijitReset dijitCalendarArrow' data-dojo-attach-point=\"incrementMonth\">\n\t\t\t\t<img src=\"${_blankGif}\" alt=\"\" class=\"dijitCalendarIncrementControl dijitCalendarIncrease\" role=\"presentation\"/>\n\t\t\t\t<span data-dojo-attach-point=\"increaseArrowNode\" class=\"dijitA11ySideArrow\">+</span>\n\t\t\t</th>\n\t\t</tr>\n\t\t<tr role=\"row\">\n\t\t\t${!dayCellsHtml}\n\t\t</tr>\n\t</thead>\n\t<tbody data-dojo-attach-point=\"dateRowsNode\" data-dojo-attach-event=\"onclick: _onDayClick\" class=\"dijitReset dijitCalendarBodyContainer\">\n\t\t\t${!dateRowsHtml}\n\t</tbody>\n\t<tfoot class=\"dijitReset dijitCalendarYearContainer\">\n\t\t<tr>\n\t\t\t<td class='dijitReset' valign=\"top\" colspan=\"7\" role=\"presentation\">\n\t\t\t\t<div class=\"dijitCalendarYearLabel\">\n\t\t\t\t\t<span data-dojo-attach-point=\"previousYearLabelNode\" class=\"dijitInline dijitCalendarPreviousYear\" role=\"button\"></span>\n\t\t\t\t\t<span data-dojo-attach-point=\"currentYearLabelNode\" class=\"dijitInline dijitCalendarSelectedYear\" role=\"button\" id=\"${id}_year\"></span>\n\t\t\t\t\t<span data-dojo-attach-point=\"nextYearLabelNode\" class=\"dijitInline dijitCalendarNextYear\" role=\"button\"></span>\n\t\t\t\t</div>\n\t\t\t</td>\n\t\t</tr>\n\t</tfoot>\n</table>\n"}});
define("dijit/CalendarLite",["dojo/_base/array","dojo/_base/declare","dojo/cldr/supplemental","dojo/date","dojo/date/locale","dojo/date/stamp","dojo/dom","dojo/dom-class","dojo/_base/event","dojo/_base/lang","dojo/sniff","dojo/string","./_WidgetBase","./_TemplatedMixin","dojo/text!./templates/Calendar.html","./hccss"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f){
var _10=_2("dijit.CalendarLite",[_d,_e],{templateString:_f,dowTemplateString:"<th class=\"dijitReset dijitCalendarDayLabelTemplate\" role=\"columnheader\"><span class=\"dijitCalendarDayLabel\">${d}</span></th>",dateTemplateString:"<td class=\"dijitReset\" role=\"gridcell\" data-dojo-attach-point=\"dateCells\"><span class=\"dijitCalendarDateLabel\" data-dojo-attach-point=\"dateLabels\"></span></td>",weekTemplateString:"<tr class=\"dijitReset dijitCalendarWeekTemplate\" role=\"row\">${d}${d}${d}${d}${d}${d}${d}</tr>",value:new Date(""),datePackage:"",dayWidth:"narrow",tabIndex:"0",currentFocus:new Date(),baseClass:"dijitCalendar",_isValidDate:function(_11){
return _11&&!isNaN(_11)&&typeof _11=="object"&&_11.toString()!=this.constructor.prototype.value.toString();
},_getValueAttr:function(){
if(this.value&&!isNaN(this.value)){
var _12=new this.dateClassObj(this.value);
_12.setHours(0,0,0,0);
if(_12.getDate()<this.value.getDate()){
_12=this.dateModule.add(_12,"hour",1);
}
return _12;
}else{
return null;
}
},_setValueAttr:function(_13,_14){
if(typeof _13=="string"){
_13=_6.fromISOString(_13);
}
_13=this._patchDate(_13);
if(this._isValidDate(_13)&&!this.isDisabledDate(_13,this.lang)){
this._set("value",_13);
this.set("currentFocus",_13);
this._markSelectedDates([_13]);
if(this._created&&(_14||typeof _14=="undefined")){
this.onChange(this.get("value"));
}
}else{
this._set("value",null);
this._markSelectedDates([]);
}
},_patchDate:function(_15){
if(_15){
_15=new this.dateClassObj(_15);
_15.setHours(1,0,0,0);
}
return _15;
},_setText:function(_16,_17){
while(_16.firstChild){
_16.removeChild(_16.firstChild);
}
_16.appendChild(_16.ownerDocument.createTextNode(_17));
},_populateGrid:function(){
var _18=new this.dateClassObj(this.currentFocus);
_18.setDate(1);
var _19=_18.getDay(),_1a=this.dateModule.getDaysInMonth(_18),_1b=this.dateModule.getDaysInMonth(this.dateModule.add(_18,"month",-1)),_1c=new this.dateClassObj(),_1d=_3.getFirstDayOfWeek(this.lang);
if(_1d>_19){
_1d-=7;
}
this._date2cell={};
_1.forEach(this.dateCells,function(_1e,idx){
var i=idx+_1d;
var _1f=new this.dateClassObj(_18),_20,_21="dijitCalendar",adj=0;
if(i<_19){
_20=_1b-_19+i+1;
adj=-1;
_21+="Previous";
}else{
if(i>=(_19+_1a)){
_20=i-_19-_1a+1;
adj=1;
_21+="Next";
}else{
_20=i-_19+1;
_21+="Current";
}
}
if(adj){
_1f=this.dateModule.add(_1f,"month",adj);
}
_1f.setDate(_20);
if(!this.dateModule.compare(_1f,_1c,"date")){
_21="dijitCalendarCurrentDate "+_21;
}
if(this.isDisabledDate(_1f,this.lang)){
_21="dijitCalendarDisabledDate "+_21;
_1e.setAttribute("aria-disabled","true");
}else{
_21="dijitCalendarEnabledDate "+_21;
_1e.removeAttribute("aria-disabled");
_1e.setAttribute("aria-selected","false");
}
var _22=this.getClassForDate(_1f,this.lang);
if(_22){
_21=_22+" "+_21;
}
_1e.className=_21+"Month dijitCalendarDateTemplate";
var _23=_1f.valueOf();
this._date2cell[_23]=_1e;
_1e.dijitDateValue=_23;
this._setText(this.dateLabels[idx],_1f.getDateLocalized?_1f.getDateLocalized(this.lang):_1f.getDate());
},this);
},_populateControls:function(){
var _24=new this.dateClassObj(this.currentFocus);
_24.setDate(1);
this.monthWidget.set("month",_24);
var y=_24.getFullYear()-1;
var d=new this.dateClassObj();
_1.forEach(["previous","current","next"],function(_25){
d.setFullYear(y++);
this._setText(this[_25+"YearLabelNode"],this.dateLocaleModule.format(d,{selector:"year",locale:this.lang}));
},this);
},goToToday:function(){
this.set("value",new this.dateClassObj());
},constructor:function(_26){
this.dateModule=_26.datePackage?_a.getObject(_26.datePackage,false):_4;
this.dateClassObj=this.dateModule.Date||Date;
this.dateLocaleModule=_26.datePackage?_a.getObject(_26.datePackage+".locale",false):_5;
},_createMonthWidget:function(){
return _10._MonthWidget({id:this.id+"_mw",lang:this.lang,dateLocaleModule:this.dateLocaleModule},this.monthNode);
},buildRendering:function(){
var d=this.dowTemplateString,_27=this.dateLocaleModule.getNames("days",this.dayWidth,"standAlone",this.lang),_28=_3.getFirstDayOfWeek(this.lang);
this.dayCellsHtml=_c.substitute([d,d,d,d,d,d,d].join(""),{d:""},function(){
return _27[_28++%7];
});
var r=_c.substitute(this.weekTemplateString,{d:this.dateTemplateString});
this.dateRowsHtml=[r,r,r,r,r,r].join("");
this.dateCells=[];
this.dateLabels=[];
this.inherited(arguments);
_7.setSelectable(this.domNode,false);
var _29=new this.dateClassObj(this.currentFocus);
this.monthWidget=this._createMonthWidget();
this.set("currentFocus",_29,false);
},postCreate:function(){
this.inherited(arguments);
this._connectControls();
},_connectControls:function(){
var _2a=_a.hitch(this,function(_2b,_2c,_2d){
this.connect(this[_2b],"onclick",function(){
this._setCurrentFocusAttr(this.dateModule.add(this.currentFocus,_2c,_2d));
});
});
_2a("incrementMonth","month",1);
_2a("decrementMonth","month",-1);
_2a("nextYearLabelNode","year",1);
_2a("previousYearLabelNode","year",-1);
},_setCurrentFocusAttr:function(_2e,_2f){
var _30=this.currentFocus,_31=this._getNodeByDate(_30);
_2e=this._patchDate(_2e);
this._set("currentFocus",_2e);
if(!this._date2cell||this.dateModule.difference(_30,_2e,"month")!=0){
this._populateGrid();
this._populateControls();
this._markSelectedDates([this.value]);
}
var _32=this._getNodeByDate(_2e);
_32.setAttribute("tabIndex",this.tabIndex);
if(this.focused||_2f){
_32.focus();
}
if(_31&&_31!=_32){
if(_b("webkit")){
_31.setAttribute("tabIndex","-1");
}else{
_31.removeAttribute("tabIndex");
}
}
},focus:function(){
this._setCurrentFocusAttr(this.currentFocus,true);
},_onDayClick:function(evt){
_9.stop(evt);
for(var _33=evt.target;_33&&!_33.dijitDateValue;_33=_33.parentNode){
}
if(_33&&!_8.contains(_33,"dijitCalendarDisabledDate")){
this.set("value",_33.dijitDateValue);
}
},_getNodeByDate:function(_34){
_34=this._patchDate(_34);
return _34&&this._date2cell?this._date2cell[_34.valueOf()]:null;
},_markSelectedDates:function(_35){
function _36(_37,_38){
_8.toggle(_38,"dijitCalendarSelectedDate",_37);
_38.setAttribute("aria-selected",_37?"true":"false");
};
_1.forEach(this._selectedCells||[],_a.partial(_36,false));
this._selectedCells=_1.filter(_1.map(_35,this._getNodeByDate,this),function(n){
return n;
});
_1.forEach(this._selectedCells,_a.partial(_36,true));
},onChange:function(){
},isDisabledDate:function(){
},getClassForDate:function(){
}});
_10._MonthWidget=_2("dijit.CalendarLite._MonthWidget",_d,{_setMonthAttr:function(_39){
var _3a=this.dateLocaleModule.getNames("months","wide","standAlone",this.lang,_39),_3b=(_b("ie")==6?"":"<div class='dijitSpacer'>"+_1.map(_3a,function(s){
return "<div>"+s+"</div>";
}).join("")+"</div>");
this.domNode.innerHTML=_3b+"<div class='dijitCalendarMonthLabel dijitCalendarCurrentMonthLabel'>"+_3a[_39.getMonth()]+"</div>";
}});
return _10;
});
