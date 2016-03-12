CREATE TABLE IF NOT EXISTS `lts_admin_job_client_monitor_data` (
            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
            `gmt_created` bigint(20) NULL DEFAULT NULL,
            `node_group` varchar(64) DEFAULT NULL,
            `identity` varchar(64) DEFAULT NULL,
            `submit_success_num` bigint(20) DEFAULT NULL,
            `submit_failed_num` bigint(11) DEFAULT NULL,
            `fail_store_num` bigint(20) DEFAULT NULL,
            `submit_fail_store_num` bigint(20) DEFAULT NULL,
            `handle_feedback_num` bigint(20) DEFAULT NULL,
            `timestamp` bigint(20) NULL DEFAULT NULL,
            PRIMARY KEY (`id`),
            KEY `idx_timestamp` (`timestamp`),
            KEY `idx_identity` (`identity`),
            KEY `idx_node_group` (`node_group`)
            );