CREATE TABLE IF NOT EXISTS `lts_admin_jvm_gc` (
            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
            `gmt_created` bigint(20) NULL DEFAULT NULL,
            `identity` varchar(64) DEFAULT NULL,
            `timestamp` bigint(20) NULL DEFAULT NULL,
            `node_type` varchar(32) NULL DEFAULT NULL,
            `node_group` varchar(64) NULL DEFAULT NULL,
            `young_gc_collection_count` bigint(20) NULL DEFAULT NULL,
            `young_gc_collection_time` bigint(20) NULL DEFAULT NULL,
            `full_gc_collection_count` bigint(20) NULL DEFAULT NULL,
            `full_gc_collection_time` bigint(20) NULL DEFAULT NULL,
            `span_young_gc_collection_count` bigint(20) NULL DEFAULT NULL,
            `span_young_gc_collection_time` bigint(20) NULL DEFAULT NULL,
            `span_full_gc_collection_count` bigint(20) NULL DEFAULT NULL,
            `span_full_gc_collection_time` bigint(20) NULL DEFAULT NULL,
            PRIMARY KEY (`id`),
            KEY `idx_identity` (`identity`),
            KEY `idx_timestamp` (`timestamp`)
            );