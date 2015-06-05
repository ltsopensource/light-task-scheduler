/**
 * @fileOverview 搜索页面业务控件
 * @ignore
 */

define('common/search',['bui/common','bui/grid','bui/form','bui/data','bui/overlay'],function (require) {
  var BUI = require('bui/common'),
    Grid = require('bui/grid'),
    Data = require('bui/data'),
    Overlay = require('bui/overlay'),
    Form = require('bui/form');

  /**
   * @class Search
   * 搜索页类
   */
  function Search(config){

    Search.superclass.constructor.call(this, config);
    this._init();
  }

  Search.ATTRS = {
    /**
     * 是否自动查询，打开页面时未点击查询按钮时是否自动查询
     * @type {Boolean}
     */
    autoSearch :{
      value : true
    },
    /**
     * grid 容器的 id
     * @type {String}
     */
    gridId : {
      value : 'grid'
    },
    /**
     * 表单的容器的id
     * @type {String}
     */
    formId : {
      value : 'searchForm'
    },
    /**
     * 查询按钮的id
     * @type {Object}
     */
    btnId : {
      value : 'btnSearch'
    },
    /**
     * 表单的配置项
     * @type {Object}
     */
    formCfg : {
      value : {}
    },
    /**
     * grid 表格的配置项
     * @type {Object}
     */
    gridCfg : {

    },
    /**
     * 表单对象
     * @type {Object}
     */
    form : {

    },
    /**
     * 表格对象
     * @type {Object}
     */
    grid : {

    },
    /**
     * 数据缓冲类
     * @type {Object}
     */
    store : {

    }
  }

  BUI.extend(Search,BUI.Base);

  BUI.augment(Search,{
    _init : function(){
      var _self = this;

      _self._initForm();
      _self._initStoreEvent();
      _self._initGrid();
      _self._initEvent();
      _self._initData();
    },
    //初始化事件
    _initEvent : function(){
      this._initDomEvent();
      this._initFormEvent();
      this._initGridEvent();
    },
    _initDomEvent : function(){
      var _self = this,
        btnId = _self.get('btnId'),
        store = _self.get('store'),
        form = _self.get('form');
      $('#'+btnId).on('click',function(ev){
        ev.preventDefault();
        form.valid();
        if(form.isValid()){
          _self.load(true);
        }
      });
    },
    //初始化form
    _initForm : function(){
      var _self = this,
        form = _self.get('form');
      if(!form){
        var formCfg = BUI.merge(_self.get('formCfg'),{
          srcNode : '#' + _self.get('formId')
        });
        form = new Form.HForm(formCfg);
        form.render();
        _self.set('form',form);
      }
    },
    _initFormEvent : function(){

    },
    //初始化表格
    _initGrid : function(){
      var _self = this,
        grid = _self.get('grid');
      if(!grid){
        var gridCfg = _self.get('gridCfg'),
          store = _self.get('store');
        gridCfg.store = store;
        gridCfg.render = '#' +_self.get('gridId'),
        grid = new Grid.Grid(gridCfg);
        grid.render();
        _self.set('grid',grid);
      }
    },
    _initGridEvent : function(){

    },
    _initData : function(){
      var _self = this,
        autoSearch = _self.get('autoSearch');
      if(autoSearch){
        _self.load(true);
      }
    },
    //初始化数据加载事件
    _initStoreEvent : function(){
      var _self = this,
        store = _self.get('store');
      //处理异常
      store.on('exception',function(ev){
        BUI.Message.Alert(ev.error);
      });
    },
    /**
     * 加载数据
     * @param {Boolean} reset 是否重置表格查询的页数
     */
    load : function(reset){
      var _self =this,
        form = _self.get('form'),
        store = _self.get('store'),
        param = form.serializeToObject();
      if(reset){
        param.start=0;
        param.pageIndex = 0;
      }
      store.load(param);
    }
  });

  Search.createStore = function(url,cfg){

    cfg = BUI.merge({
      autoLoad : false,
      url : url,
      pageSize : 30
    },cfg);
    return new Data.Store(cfg);
  };

  Search.createGridCfg = function(columns,cfg){
    cfg = BUI.merge({
      columns : columns,
      loadMask : true,
      bbar:{
        pagingBar:true
      }
    },cfg);
    
    return cfg;
  };

  Search.createLink = function(cfg){
    var temp = '<span class="page-action grid-command" data-id="{id}" data-href="{href}" title="{title}">{text}</span>';
    return BUI.substitute(temp,cfg);
  }
  return Search;
});