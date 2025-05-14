-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS dd_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 选择数据库
USE dd_ai;

CREATE TABLE chat_memory
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL COMMENT '会话ID',
    type            VARCHAR(50)  NOT NULL COMMENT '消息类型',
    text            TEXT         NOT NULL COMMENT '消息内容',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX           idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话记忆表';