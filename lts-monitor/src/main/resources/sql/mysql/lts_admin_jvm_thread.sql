CREATE TABLE IF NOT EXISTS `lts_admin_jvm_thread` (
            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
            `gmt_created` bigint(20) NULL DEFAULT NULL,
            `identity` varchar(64) DEFAULT NULL,
            `timestamp` bigint(20) NULL DEFAULT NULL,
            `node_type` varchar(32) NULL DEFAULT NULL,
            `node_group` varchar(64) NULL DEFAULT NULL,
            `daemon_thread_count` int(11) NULL DEFAULT NULL,
            `thread_count` int(11) NULL DEFAULT NULL,
            `total_started_thread_count` bigint(20) NULL DEFAULT NULL,
            `dead_locked_thread_count` int(11) NULL DEFAULT NULL,
            `process_cpu_time_rate` double  DEFAULT NULL,
            PRIMARY KEY (`id`),
            KEY `idx_identity` (`identity`),
            KEY `idx_timestamp` (`timestamp`)
            );