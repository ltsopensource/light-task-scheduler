CREATE TABLE IF NOT EXISTS `{tableName}` (
  `node_type` varchar(16) NOT NULL DEFAULT '' COMMENT '节点类型',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '名字',
  `gmt_created` bigint(20) NULL COMMENT '创建时间',
  PRIMARY KEY (`node_type`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点组';