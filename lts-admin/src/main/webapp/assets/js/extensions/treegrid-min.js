/**
 * @fileOverview Tree Grid
 * @ignore
 */define("bui/extensions/treegrid",["bui/common","bui/grid","bui/tree"],function(e){"use strict";var t=e("bui/tree"),n=e("bui/grid"),r=n.Grid.extend([t.Mixin],{},{ATTRS:{iconContainer:{value:".bui-grid-cell-inner"}}},{xclass:"tree-grid"});return r});
