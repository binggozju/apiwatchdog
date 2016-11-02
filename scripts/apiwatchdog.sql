SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for api_bad_call
-- ----------------------------
DROP TABLE IF EXISTS `api_bad_call`;
CREATE TABLE `api_bad_call` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `api_id` int(10) unsigned NOT NULL COMMENT 'api的id',
  `call_uuid` varchar(40) NOT NULL COMMENT 'api调用的uuid',
  `request_time` datetime NOT NULL COMMENT '发起request的时间',
  `response_time` datetime DEFAULT NULL COMMENT '收到response的时间',
  `http_reponse_code` varchar(3) DEFAULT '' COMMENT 'response中的http响应码',
  `api_return_code` varchar(12) DEFAULT '' COMMENT 'response中的api返回码',
  `api_return_message` varchar(128) DEFAULT '' COMMENT 'response中的api返回码的解释文本',
  `source_service` varchar(64) NOT NULL COMMENT '发起api调用的服务的名称',
  `source_host` varchar(64) DEFAULT '' COMMENT '发起api调用的服务所在机器的名称或IP',
  `dest_service` varchar(64) DEFAULT '' COMMENT '接收api调用的服务的名称',
  `dest_host` varchar(64) DEFAULT '' COMMENT '接收api调用的服务所在机器的名称或IP',
  `request_body` varchar(1024) DEFAULT '' COMMENT 'request请求内容',
  `response_body` varchar(1024) DEFAULT '' COMMENT 'reponse响应内容',
  PRIMARY KEY (`id`),
  KEY `request_time` (`request_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for api_item
-- ----------------------------
DROP TABLE IF EXISTS `api_item`;
CREATE TABLE `api_item` (
  `api_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT 'api的名称',
  `description` text COMMENT 'api的简要描述',
  `path` varchar(512) NOT NULL COMMENT 'api的访问路径，如/your/api/path',
  `type` varchar(32) NOT NULL COMMENT 'api的类型，如GET/POST/PUT/DELETE',
  `version` varchar(32) DEFAULT '' COMMENT 'api的版本号',
  `provider_id` int(10) unsigned NOT NULL COMMENT 'api所属service的id',
  `state` tinyint(4) NOT NULL DEFAULT '1' COMMENT '标识该api是否启用监控，1->启用，0->禁用',
  `weixin_receivers` varchar(256) DEFAULT '' COMMENT '当前API的微信告警接收人，多人之间用分号隔开',
  `mail_receivers` varchar(256) DEFAULT '' COMMENT '当前API的邮件告警接收人，多人之间用分号隔开',
  `phone_receivers` varchar(256) DEFAULT '' COMMENT '当前API的短信告警接收人，多人之间用分号隔开',
  `metric_not200` tinyint(4) NOT NULL DEFAULT '1' COMMENT 'http响应码非200时是否告警，1->是，0->否',
  `metric_200_not0` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'http响应码为200但api返回码非0时是否告警，1->是，0->否',
  `metric_resptime_threshold` tinyint(4) NOT NULL DEFAULT '1' COMMENT '没有收到http响应或超时是否告警，1->是，0->否',
  `alarm_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '告警方式：1->微信, 2->邮件, 3->微信&邮件, 4->短信, 依次类推',
  `created_time` datetime NOT NULL,
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for api_provider
-- ----------------------------
DROP TABLE IF EXISTS `api_provider`;
CREATE TABLE `api_provider` (
  `provider_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT 'api提供者服务的英文名称，建议统一用小写',
  `description` text COMMENT 'api提供者服务的简要描述',
  `version` varchar(32) DEFAULT '' COMMENT 'api提供者服务的版本号',
  `state` tinyint(4) NOT NULL DEFAULT '1' COMMENT '标识是否开启对该api提供者服务的API监控，1->开启，0->关闭',
  `weixin_receivers` varchar(256) NOT NULL COMMENT '微信告警接收人，多人之间用分号隔开',
  `mail_receivers` varchar(256) NOT NULL COMMENT '邮件告警接收人，多人之间用分号隔开',
  `phone_receivers` varchar(256) NOT NULL COMMENT '短信告警接收人，多人之间用分号隔开',
  `created_time` datetime NOT NULL,
  `last_updated_time` datetime NOT NULL,
  PRIMARY KEY (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for api_stat_data
-- ----------------------------
DROP TABLE IF EXISTS `api_stat_data`;
CREATE TABLE `api_stat_data` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `api_id` int(10) unsigned NOT NULL COMMENT 'api的id',
  `start_time` datetime NOT NULL COMMENT '5分钟时间片的起始时间，格式2016-10-18 17:40:00',
  `count_total` int(10) unsigned NOT NULL COMMENT '5分钟内指定api总的调用次数',
  `count_timeout` int(10) unsigned NOT NULL COMMENT '5分钟内指定api超时或未收到回包的调用次数',
  `count_not200` int(10) unsigned NOT NULL COMMENT '5分钟内指定api http响应码非200的调用次数',
  `count_200_not0` int(10) unsigned NOT NULL COMMENT '5分钟内指定api http响应码为200但api返回码非0的调用次数',
  `resptime_total` int(10) unsigned NOT NULL COMMENT '5分钟内所有收到回包的api调用的累计响应时间',
  `resptime_0s_1s` int(10) unsigned NOT NULL COMMENT '5分钟内响应时间在0s-1s之间的api调用次数',
  `resptime_1s_2s` int(10) unsigned NOT NULL COMMENT '5分钟内响应时间在1s-2s之间的api调用次数',
  `resptime_2s_3s` int(10) unsigned NOT NULL,
  `resptime_3s_4s` int(10) unsigned NOT NULL,
  `resptime_4s_5s` int(10) unsigned NOT NULL,
  `resptime_5s_6s` int(10) unsigned NOT NULL,
  `resptime_6s_7s` int(10) unsigned NOT NULL,
  `resptime_7s_8s` int(10) unsigned NOT NULL,
  `resptime_8s_9s` int(10) unsigned NOT NULL,
  `resptime_10s_11s` int(10) unsigned NOT NULL,
  `resptime_11s_12s` int(10) unsigned NOT NULL,
  `resptime_12s_max` int(10) unsigned NOT NULL COMMENT '5分钟内响应时间大于12s或超时的api调用次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `apiid_starttime` (`api_id`,`start_time`) USING BTREE,
  KEY `start_time` (`start_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
