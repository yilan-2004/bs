DROP TABLE IF EXISTS `submit_record`;
DROP TABLE IF EXISTS `submit_case_result`;
DROP TABLE IF EXISTS `ai_feedback`;
DROP TABLE IF EXISTS `ai_error_cache`;
DROP TABLE IF EXISTS `knowledge_chunk`;
DROP TABLE IF EXISTS `knowledge_document`;
DROP TABLE IF EXISTS `knowledge_base`;
DROP TABLE IF EXISTS `test_case`;
DROP TABLE IF EXISTS `question_option`;
DROP TABLE IF EXISTS `problem`;
DROP TABLE IF EXISTS `problem_bank`;
DROP TABLE IF EXISTS `subject`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL,
    `password` VARCHAR(128) NOT NULL,
    `real_name` VARCHAR(64) NOT NULL,
    `role` VARCHAR(32) NOT NULL,
    `email` VARCHAR(128) NULL,
    `phone` VARCHAR(32) NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`)
);

CREATE TABLE `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `description` TEXT NULL,
    `icon` VARCHAR(128) NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort_order` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subject_name` (`name`),
    KEY `idx_subject_status` (`status`)
);

INSERT INTO `subject` (`id`, `name`, `description`, `icon`, `status`, `sort_order`)
VALUES
    (1, '编程', '编程与算法训练', 'code', 1, 1),
    (2, '数学', '数学基础与应用', 'calculator', 1, 2),
    (3, '英语', '英语语言学习', 'english', 1, 3),
    (4, '语文', '语文阅读与表达', 'book', 1, 4),
    (5, '物理', '物理概念与实验', 'physics', 1, 5),
    (6, '化学', '化学概念与实验', 'chemistry', 1, 6);

CREATE TABLE `problem` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(128) NOT NULL,
    `description` TEXT NOT NULL,
    `input_description` TEXT NULL,
    `output_description` TEXT NULL,
    `sample_input` TEXT NULL,
    `sample_output` TEXT NULL,
    `difficulty` VARCHAR(32) NOT NULL DEFAULT 'EASY',
    `knowledge_tags` VARCHAR(512) NULL,
    `bank_id` BIGINT NULL,
    `subject_id` BIGINT NULL,
    `question_type` VARCHAR(32) NOT NULL DEFAULT 'PROGRAMMING',
    `standard_answer` TEXT NULL,
    `scoring_points` TEXT NULL,
    `score` INT NULL,
    `creator_id` BIGINT NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_problem_creator_id` (`creator_id`),
    KEY `idx_problem_bank_id` (`bank_id`),
    KEY `idx_problem_subject_id` (`subject_id`),
    KEY `idx_problem_question_type` (`question_type`),
    KEY `idx_problem_status` (`status`)
);

CREATE TABLE `question_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `problem_id` BIGINT NOT NULL,
    `option_key` VARCHAR(16) NOT NULL,
    `option_content` TEXT NOT NULL,
    `is_correct` TINYINT NOT NULL DEFAULT 0,
    `sort_order` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_question_option_problem_id` (`problem_id`),
    KEY `idx_question_option_sort` (`problem_id`, `sort_order`)
);

CREATE TABLE `problem_bank` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `description` TEXT NULL,
    `cover_url` VARCHAR(512) NULL,
    `difficulty` VARCHAR(32) NOT NULL DEFAULT 'MIXED',
    `knowledge_tags` VARCHAR(512) NULL,
    `subject_id` BIGINT NULL,
    `creator_id` BIGINT NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `problem_count` INT NOT NULL DEFAULT 0,
    `sort_order` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_problem_bank_creator_id` (`creator_id`),
    KEY `idx_problem_bank_subject_id` (`subject_id`),
    KEY `idx_problem_bank_status` (`status`)
);

CREATE TABLE `test_case` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `problem_id` BIGINT NOT NULL,
    `input_data` TEXT NULL,
    `expected_output` TEXT NULL,
    `is_sample` TINYINT NOT NULL DEFAULT 0,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_test_case_problem_id` (`problem_id`),
    KEY `idx_test_case_status` (`status`)
);

CREATE TABLE `submit_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `problem_id` BIGINT NOT NULL,
    `language` VARCHAR(32) NOT NULL,
    `code` TEXT NOT NULL,
    `judge_status` VARCHAR(32) NOT NULL,
    `pass_count` INT NOT NULL DEFAULT 0,
    `total_count` INT NOT NULL DEFAULT 0,
    `run_time` BIGINT NULL,
    `error_message` TEXT NULL,
    `output_result` TEXT NULL,
    `score` INT NULL,
    `need_ai_feedback` TINYINT NOT NULL DEFAULT 0,
    `code_hash` CHAR(32) NOT NULL,
    `error_fingerprint` CHAR(32) NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_submit_user_id` (`user_id`),
    KEY `idx_submit_problem_id` (`problem_id`),
    KEY `idx_submit_code_hash` (`code_hash`),
    KEY `idx_submit_error_fingerprint` (`error_fingerprint`)
);

CREATE TABLE `submit_case_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `submit_id` BIGINT NOT NULL,
    `test_case_id` BIGINT NOT NULL,
    `input_data` TEXT NULL,
    `expected_output` TEXT NULL,
    `actual_output` TEXT NULL,
    `error_output` TEXT NULL,
    `judge_status` VARCHAR(32) NOT NULL,
    `run_time` BIGINT NULL,
    `pass_flag` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_submit_case_result_submit_id` (`submit_id`),
    KEY `idx_submit_case_result_test_case_id` (`test_case_id`)
);

CREATE TABLE `ai_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `submit_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `problem_id` BIGINT NOT NULL,
    `error_type` VARCHAR(64) NULL,
    `diagnosis` TEXT NULL,
    `explanation` TEXT NULL,
    `suggestion` TEXT NULL,
    `evaluation` TEXT NULL,
    `related_knowledge` TEXT NULL,
    `next_practice_advice` TEXT NULL,
    `score` INT NULL,
    `recommend_problems` TEXT NULL,
    `from_cache` TINYINT NOT NULL DEFAULT 0,
    `cache_id` BIGINT NULL,
    `ai_model` VARCHAR(64) NULL,
    `rag_used` TINYINT NOT NULL DEFAULT 0,
    `evidence_chunk_ids` VARCHAR(512) NULL,
    `evidence_summary` TEXT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_feedback_submit_id` (`submit_id`),
    KEY `idx_ai_feedback_user_id` (`user_id`),
    KEY `idx_ai_feedback_problem_id` (`problem_id`)
);

CREATE TABLE `ai_error_cache` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `problem_id` BIGINT NOT NULL,
    `judge_status` VARCHAR(32) NOT NULL,
    `error_fingerprint` CHAR(32) NOT NULL,
    `error_type` VARCHAR(64) NULL,
    `diagnosis` TEXT NULL,
    `explanation` TEXT NULL,
    `suggestion` TEXT NULL,
    `evaluation` TEXT NULL,
    `related_knowledge` TEXT NULL,
    `next_practice_advice` TEXT NULL,
    `score` INT NULL,
    `reuse_count` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_error_cache_fingerprint` (`error_fingerprint`),
    KEY `idx_ai_error_cache_problem_status` (`problem_id`, `judge_status`)
);

CREATE TABLE `knowledge_base` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `description` TEXT NULL,
    `subject_id` BIGINT NULL,
    `creator_id` BIGINT NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort_order` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_base_creator` (`creator_id`),
    KEY `idx_knowledge_base_subject` (`subject_id`),
    KEY `idx_knowledge_base_status` (`status`)
);

CREATE TABLE `knowledge_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `base_id` BIGINT NOT NULL,
    `title` VARCHAR(160) NOT NULL,
    `content` CLOB NOT NULL,
    `knowledge_tags` VARCHAR(512) NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `chunk_count` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_document_base` (`base_id`),
    KEY `idx_knowledge_document_status` (`status`)
);

CREATE TABLE `knowledge_chunk` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `base_id` BIGINT NOT NULL,
    `document_id` BIGINT NOT NULL,
    `subject_id` BIGINT NULL,
    `document_title` VARCHAR(160) NULL,
    `knowledge_tags` VARCHAR(512) NULL,
    `chunk_text` TEXT NOT NULL,
    `chunk_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_chunk_subject` (`subject_id`),
    KEY `idx_knowledge_chunk_document` (`document_id`),
    KEY `idx_knowledge_chunk_base_status` (`base_id`, `status`),
    KEY `idx_knowledge_chunk_status` (`status`)
);
