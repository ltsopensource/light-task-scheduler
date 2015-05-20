CREATE TABLE IF NOT EXISTS `lts_job_feedback_po` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `gmt_created` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `job_result` text COMMENT '任务执行结果 JSON',
  PRIMARY KEY (`id`),
  KEY `gmt_created` (`gmt_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;