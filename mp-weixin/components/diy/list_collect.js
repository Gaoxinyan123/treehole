(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["components/diy/list_collect"],{be73:function(t,e,c){"use strict";var i=c("c1d5"),l=c.n(i);l.a},c1d5:function(t,e,c){},dcc2:function(t,e,c){"use strict";var i;c.r(e);var l,o=function(){var t=this,e=t.$createElement,c=(t._self._c,t.__map(t.list,(function(e,c){var i=t.__get_orig(e),l=t.$fullUrl(e[t.vm.img])||"/static/img/avatar.jpg",o=t.$toTime(e[t.vm.create_time],"yyyy-MM-dd hh:mm:ss");return{$orig:i,m0:l,m1:o}})));t.$mp.data=Object.assign({},{$root:{l0:c}})},r=[],a={props:{list:{type:Array,default:function(){return[]}},vm:{type:Object,default:function(){return{img:"img",title:"title",create_time:"create_time",collect_id:"collect_id",source_table:"source_table",source_field:"source_field",source_id:"source_id"}}}},methods:{del_collect:function(t,e){var c=this;this.$get("~/api/collect/del",{collect_id:t},(function(t){c.list.splice(e,1)}))}}},s=a,n=(c("be73"),c("f0c5")),u=Object(n["a"])(s,o,r,!1,null,"35c9bec8",null,!1,i,l);e["default"]=u.exports}}]);
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'components/diy/list_collect-create-component',
    {
        'components/diy/list_collect-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('543d')['createComponent'](__webpack_require__("dcc2"))
        })
    },
    [['components/diy/list_collect-create-component']]
]);
