CREATE TABLE `lts_node` ( 
    `identity` varchar(64) NOT NULL DEFAULT '',
    `available` tinyint(4) DEFAULT NULL, 
    `cluster_name` varchar(64) DEFAULT NULL,
    `node_type` varchar(64) DEFAULT NULL,
    `ip` varchar(16) DEFAULT NULL, 
    `port` int(11) DEFAULT NULL,
    `node_group` varchar(64) DEFAULT NULL,
    `create_time` bigint(20) DEFAULT NULL,
    `threads` int(11) DEFAULT NULL,
    `host_name` varchar(64) DEFAULT NULL,
    `http_cmd_port` int(11) DEFAULT NULL,
    PRIMARY KEY (`identity`)
)