# 该文件用于初始化构建study项目数据库, 由于要构建各种外键, 所以要注意顺序.
CREATE TABLE `order` (
  `order_id` int(11) NOT NULL COMMENT '订单编号',
  `uid` int(11) DEFAULT NULL COMMENT '用户Id',
  `amout` bigint(20) NOT NULL COMMENT '订单金额(单位为分)',
  `status` tinyint(2) DEFAULT NULL COMMENT '订单状态',
  `order_time` datetime DEFAULT NULL COMMENT '订单时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `order` VALUES ('200', '100', 5899, '0', '2016-03-30 17:54:20');
INSERT INTO `order` VALUES ('201', '100', 6799, '0', '2016-03-30 17:54:38');
INSERT INTO `order` VALUES ('202', '101', 12699, '0', '2016-03-30 17:55:01');


CREATE TABLE `user` (
  `uid` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `name` varchar(10) DEFAULT NULL COMMENT '姓名',
  `sex` tinyint(1) DEFAULT NULL COMMENT '性别',
  `age` tinyint(2) DEFAULT NULL COMMENT '年龄',
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `index_mobile` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `user` VALUES ('100', '张三', '1', '28', '13547521456', 'ASDAWQ@!#SDF@#$%XCF', '2016-03-30 17:47:51');
INSERT INTO `user` VALUES ('101', '李四', '2', '35', '17025856329', '234ASD@#$@#$AFSDFRT', '2016-03-30 17:48:34');
INSERT INTO `user` VALUES ('102', '王五', '1', '48', '15925874536', '#$%SDFSDR@#$%@#$#@', '2016-03-30 17:53:49');