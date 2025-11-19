-- 创建应用表
CREATE TABLE IF NOT EXISTS `app` (
    `id` bigint NOT NULL COMMENT 'id',
    `appName` varchar(50) NOT NULL COMMENT '应用名称',
    `cover` varchar(500) DEFAULT NULL COMMENT '应用封面',
    `initPrompt` text NOT NULL COMMENT '应用初始化的 prompt',
    `codeGenType` varchar(20) NOT NULL DEFAULT 'html' COMMENT '代码生成类型（枚举）',
    `deployKey` varchar(100) DEFAULT NULL COMMENT '部署标识',
    `deployedTime` datetime DEFAULT NULL COMMENT '部署时间',
    `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
    `userId` bigint NOT NULL COMMENT '创建用户id',
    `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_priority` (`priority`),
    KEY `idx_createTime` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';