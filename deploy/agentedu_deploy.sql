-- AgentEdu production/deployment bootstrap SQL.
-- Usage:
--   mysql -uroot -p < deploy/agentedu_deploy.sql
--
-- Demo accounts, password for both: 123456

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS agentedu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agentedu;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS submit_case_result;
DROP TABLE IF EXISTS submit_record;
DROP TABLE IF EXISTS ai_feedback;
DROP TABLE IF EXISTS ai_error_cache;
DROP TABLE IF EXISTS knowledge_chunk;
DROP TABLE IF EXISTS knowledge_document;
DROP TABLE IF EXISTS knowledge_base;
DROP TABLE IF EXISTS question_option;
DROP TABLE IF EXISTS test_case;
DROP TABLE IF EXISTS problem;
DROP TABLE IF EXISTS problem_bank;
DROP TABLE IF EXISTS student_profile;
DROP TABLE IF EXISTS subject;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    email VARCHAR(128) NULL,
    phone VARCHAR(32) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE subject (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    description TEXT NULL,
    icon VARCHAR(128) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subject_name (name),
    KEY idx_subject_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE problem_bank (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description TEXT NULL,
    cover_url VARCHAR(512) NULL,
    difficulty VARCHAR(32) NOT NULL DEFAULT 'MIXED',
    knowledge_tags VARCHAR(512) NULL,
    subject_id BIGINT NULL,
    creator_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    problem_count INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_problem_bank_subject_id (subject_id),
    KEY idx_problem_bank_creator_id (creator_id),
    KEY idx_problem_bank_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE problem (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    input_description TEXT NULL,
    output_description TEXT NULL,
    sample_input TEXT NULL,
    sample_output TEXT NULL,
    difficulty VARCHAR(32) NOT NULL DEFAULT 'EASY',
    knowledge_tags VARCHAR(512) NULL,
    bank_id BIGINT NULL,
    subject_id BIGINT NULL,
    question_type VARCHAR(32) NOT NULL DEFAULT 'PROGRAMMING',
    standard_answer TEXT NULL,
    scoring_points TEXT NULL,
    score INT NULL,
    creator_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_problem_bank_id (bank_id),
    KEY idx_problem_subject_id (subject_id),
    KEY idx_problem_question_type (question_type),
    KEY idx_problem_creator_id (creator_id),
    KEY idx_problem_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE test_case (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    input_data TEXT NULL,
    expected_output TEXT NOT NULL,
    is_sample TINYINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_test_case_problem_id (problem_id),
    KEY idx_test_case_status_sample (status, is_sample)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE question_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    option_key VARCHAR(16) NOT NULL,
    option_content TEXT NOT NULL,
    is_correct TINYINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_question_option_problem_id (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submit_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    language VARCHAR(32) NULL,
    code MEDIUMTEXT NULL,
    judge_status VARCHAR(64) NOT NULL,
    pass_count INT NOT NULL DEFAULT 0,
    total_count INT NOT NULL DEFAULT 0,
    run_time BIGINT NOT NULL DEFAULT 0,
    error_message TEXT NULL,
    output_result TEXT NULL,
    score INT NULL,
    need_ai_feedback TINYINT NOT NULL DEFAULT 0,
    code_hash VARCHAR(64) NULL,
    error_fingerprint VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_submit_user_id (user_id),
    KEY idx_submit_problem_id (problem_id),
    KEY idx_submit_judge_status (judge_status),
    KEY idx_submit_error_fingerprint (error_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submit_case_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submit_id BIGINT NOT NULL,
    test_case_id BIGINT NULL,
    input_data TEXT NULL,
    expected_output TEXT NULL,
    actual_output TEXT NULL,
    error_output TEXT NULL,
    judge_status VARCHAR(64) NOT NULL,
    run_time BIGINT NOT NULL DEFAULT 0,
    pass_flag TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_case_result_submit_id (submit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submit_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    error_type VARCHAR(128) NULL,
    diagnosis TEXT NULL,
    explanation TEXT NULL,
    suggestion TEXT NULL,
    evaluation TEXT NULL,
    related_knowledge TEXT NULL,
    next_practice_advice TEXT NULL,
    score INT NULL,
    recommend_problems TEXT NULL,
    from_cache TINYINT NOT NULL DEFAULT 0,
    cache_id BIGINT NULL,
    ai_model VARCHAR(64) NULL,
    rag_used TINYINT NOT NULL DEFAULT 0,
    evidence_chunk_ids VARCHAR(512) NULL,
    evidence_summary TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_feedback_submit_id (submit_id),
    KEY idx_ai_feedback_user_id (user_id),
    KEY idx_ai_feedback_problem_id (problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_error_cache (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    judge_status VARCHAR(64) NOT NULL,
    error_fingerprint VARCHAR(64) NOT NULL,
    error_type VARCHAR(128) NULL,
    diagnosis TEXT NULL,
    explanation TEXT NULL,
    suggestion TEXT NULL,
    evaluation TEXT NULL,
    related_knowledge TEXT NULL,
    next_practice_advice TEXT NULL,
    score INT NULL,
    reuse_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_error_cache_fingerprint (problem_id, judge_status, error_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    accepted_count INT NOT NULL DEFAULT 0,
    submit_count INT NOT NULL DEFAULT 0,
    weak_knowledge_tags VARCHAR(512) NULL,
    last_update_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_profile_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_base (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description TEXT NULL,
    subject_id BIGINT NULL,
    creator_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_kb_creator_id (creator_id),
    KEY idx_kb_subject_id (subject_id),
    KEY idx_kb_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_document (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    knowledge_tags VARCHAR(512) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    chunk_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_kdoc_base_id (base_id),
    KEY idx_kdoc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE knowledge_chunk (
    id BIGINT NOT NULL AUTO_INCREMENT,
    base_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    subject_id BIGINT NULL,
    document_title VARCHAR(128) NULL,
    knowledge_tags VARCHAR(512) NULL,
    chunk_text TEXT NOT NULL,
    chunk_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_kchunk_base_id (base_id),
    KEY idx_kchunk_document_id (document_id),
    KEY idx_kchunk_subject_id (subject_id),
    KEY idx_kchunk_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @pwd_123456 = '$2a$10$wp.TIqcQnwZ4hNaEm7ptTOd949N8enKa4SlUA6nIszX1RH6ogRRu.';

INSERT INTO sys_user(id, username, password, real_name, role, email, phone, status) VALUES
(1, 'teacher01', @pwd_123456, '王老师', 'TEACHER', 'teacher01@example.com', '13900000001', 1),
(2, 'student01', @pwd_123456, '张同学', 'STUDENT', 'student01@example.com', '13800000001', 1);

INSERT INTO subject(id, name, description, icon, status, sort_order) VALUES
(1, '编程', 'Python 编程、算法思维和代码评测训练', 'code', 1, 1),
(2, '数学', '数学基础与应用题训练', 'math', 1, 2),
(3, '英语', '英语词汇、语法和阅读训练', 'english', 1, 3),
(4, '语文', '语文阅读理解和表达训练', 'chinese', 1, 4),
(5, '物理', '物理概念与实验分析训练', 'physics', 1, 5),
(6, '化学', '化学概念与实验分析训练', 'chemistry', 1, 6);

INSERT INTO problem_bank(id, name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order) VALUES
(1, 'Python 基础训练题库', '适合入门阶段，覆盖输入输出、条件判断和基础表达式。', '', 'EASY', '输入输出,条件判断,表达式', 1, 1, 1, 4, 1),
(2, '循环专项训练题库', '围绕 for、while、累加和阶乘进行专项训练。', '', 'EASY', '循环,累加,阶乘', 1, 1, 1, 3, 2),
(3, '数组与列表专项题库', '训练 Python 列表读取、遍历、统计和聚合。', '', 'MEDIUM', '列表,数组,遍历,统计', 1, 1, 1, 3, 3),
(4, '字符串专项题库', '训练字符串遍历、切片、计数和回文判断。', '', 'MEDIUM', '字符串,切片,遍历', 1, 1, 1, 3, 4),
(5, '函数与递归专项题库', '训练函数封装、递归出口和经典递归问题。', '', 'HARD', '函数,递归,算法', 1, 1, 1, 2, 5);

INSERT INTO problem(id, title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status) VALUES
(1, '两数之和', '输入两个整数 a 和 b，输出它们的和。', '一行两个整数。', '一个整数，表示两数之和。', '1 2', '3', 'EASY', '输入输出,表达式', 1, 1, 'PROGRAMMING', 1, 1),
(2, '判断奇偶数', '输入一个整数 n，判断它是奇数还是偶数。', '一行一个整数。', '偶数输出 even，否则输出 odd。', '4', 'even', 'EASY', '条件判断,取模运算', 1, 1, 'PROGRAMMING', 1, 1),
(3, '求三个数最大值', '输入三个整数，输出其中最大值。', '一行三个整数。', '一个整数，表示最大值。', '1 5 3', '5', 'EASY', '条件判断,最大值', 1, 1, 'PROGRAMMING', 1, 1),
(4, '计算圆的面积', '输入半径 r，输出圆面积，结果保留两位小数。', '一行一个浮点数。', '圆面积，保留两位小数。', '2', '12.57', 'EASY', '浮点数,格式化输出', 1, 1, 'PROGRAMMING', 1, 1),
(5, '求 1 到 n 的和', '输入 n，输出 1 到 n 的整数和。', '一行一个正整数 n。', '一个整数。', '5', '15', 'EASY', '循环,累加', 2, 1, 'PROGRAMMING', 1, 1),
(6, '输出 n 以内的偶数', '输入 n，按从小到大输出不超过 n 的正偶数，用空格分隔。', '一行一个正整数 n。', '一行若干偶数。', '8', '2 4 6 8', 'EASY', '循环,条件判断', 2, 1, 'PROGRAMMING', 1, 1),
(7, '阶乘计算', '输入 n，输出 n 的阶乘。', '一行一个非负整数 n。', '一个整数。', '5', '120', 'MEDIUM', '循环,阶乘', 2, 1, 'PROGRAMMING', 1, 1),
(8, '数组最大值', '输入数组长度和数组元素，输出最大值。', '第一行 n，第二行 n 个整数。', '一个整数。', '5\n1 3 9 2 4', '9', 'EASY', '列表,最大值', 3, 1, 'PROGRAMMING', 1, 1),
(9, '统计正数个数', '输入数组，统计其中正数个数。', '第一行 n，第二行 n 个整数。', '一个整数。', '5\n-1 2 0 3 -4', '2', 'EASY', '列表,统计', 3, 1, 'PROGRAMMING', 1, 1),
(10, '数组元素求和', '输入数组，输出所有元素之和。', '第一行 n，第二行 n 个整数。', '一个整数。', '4\n1 2 3 4', '10', 'EASY', '列表,求和', 3, 1, 'PROGRAMMING', 1, 1),
(11, '字符串反转', '输入一个字符串，输出反转后的字符串。', '一行字符串。', '反转后的字符串。', 'abc', 'cba', 'EASY', '字符串,切片', 4, 1, 'PROGRAMMING', 1, 1),
(12, '统计字符 a 的个数', '输入一个字符串，统计字符 a 出现的次数。', '一行字符串。', '一个整数。', 'banana', '3', 'EASY', '字符串,计数', 4, 1, 'PROGRAMMING', 1, 1),
(13, '判断回文字符串', '输入一个字符串，判断是否为回文。', '一行字符串。', '是回文输出 yes，否则输出 no。', 'level', 'yes', 'MEDIUM', '字符串,回文', 4, 1, 'PROGRAMMING', 1, 1),
(14, '斐波那契数列', '输入 n，输出第 n 项斐波那契数，约定 f(1)=1, f(2)=1。', '一行一个正整数 n。', '一个整数。', '6', '8', 'MEDIUM', '递归,动态规划', 5, 1, 'PROGRAMMING', 1, 1),
(15, '求最大公约数', '输入两个正整数，输出最大公约数。', '一行两个正整数。', '一个整数。', '12 18', '6', 'MEDIUM', '函数,欧几里得算法', 5, 1, 'PROGRAMMING', 1, 1);

INSERT INTO test_case(problem_id, input_data, expected_output, is_sample, sort_order, status) VALUES
(1, '1 2', '3', 1, 1, 1), (1, '10 20', '30', 0, 2, 1), (1, '-1 5', '4', 0, 3, 1),
(2, '4', 'even', 1, 1, 1), (2, '7', 'odd', 0, 2, 1), (2, '0', 'even', 0, 3, 1),
(3, '1 5 3', '5', 1, 1, 1), (3, '-1 -5 -3', '-1', 0, 2, 1), (3, '8 8 2', '8', 0, 3, 1),
(4, '2', '12.57', 1, 1, 1), (4, '1', '3.14', 0, 2, 1), (4, '3', '28.27', 0, 3, 1),
(5, '5', '15', 1, 1, 1), (5, '1', '1', 0, 2, 1), (5, '100', '5050', 0, 3, 1),
(6, '8', '2 4 6 8', 1, 1, 1), (6, '1', '', 0, 2, 1), (6, '10', '2 4 6 8 10', 0, 3, 1),
(7, '5', '120', 1, 1, 1), (7, '0', '1', 0, 2, 1), (7, '6', '720', 0, 3, 1),
(8, '5\n1 3 9 2 4', '9', 1, 1, 1), (8, '3\n-5 -2 -7', '-2', 0, 2, 1), (8, '1\n42', '42', 0, 3, 1),
(9, '5\n-1 2 0 3 -4', '2', 1, 1, 1), (9, '4\n1 2 3 4', '4', 0, 2, 1), (9, '3\n-1 -2 -3', '0', 0, 3, 1),
(10, '4\n1 2 3 4', '10', 1, 1, 1), (10, '3\n-1 2 -3', '-2', 0, 2, 1), (10, '1\n9', '9', 0, 3, 1),
(11, 'abc', 'cba', 1, 1, 1), (11, 'hello', 'olleh', 0, 2, 1), (11, 'a', 'a', 0, 3, 1),
(12, 'banana', '3', 1, 1, 1), (12, 'apple', '1', 0, 2, 1), (12, 'bbb', '0', 0, 3, 1),
(13, 'level', 'yes', 1, 1, 1), (13, 'abc', 'no', 0, 2, 1), (13, 'aba', 'yes', 0, 3, 1),
(14, '6', '8', 1, 1, 1), (14, '1', '1', 0, 2, 1), (14, '7', '13', 0, 3, 1),
(15, '12 18', '6', 1, 1, 1), (15, '7 13', '1', 0, 2, 1), (15, '20 30', '10', 0, 3, 1);

INSERT INTO student_profile(user_id, accepted_count, submit_count, weak_knowledge_tags, last_update_time)
VALUES (2, 0, 0, '', NOW());
