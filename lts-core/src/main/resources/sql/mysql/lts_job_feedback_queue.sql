CREATE TABLE IF NOT EXISTS `{tableName}` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `gmt_created` bigint(11) COMMENT '创建时间',
  `job_result` text COMMENT '任务执行结果,JSON',
  PRIMARY KEY (`id`),
  KEY `idex_gmt_created` (`gmt_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='作业反馈结果';