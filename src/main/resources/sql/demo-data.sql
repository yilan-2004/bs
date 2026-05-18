-- AgentEdu 演示数据初始化脚本
-- 使用方式：mysql -uroot -p agentedu < src/main/resources/sql/demo-data.sql
-- 说明：脚本不清空表，按名称/用户名判断不存在才插入，适合重复导入演示环境。

SET NAMES utf8mb4;
SET @now = NOW();
SET @pwd_123456 = '$2a$10$wp.TIqcQnwZ4hNaEm7ptTOd949N8enKa4SlUA6nIszX1RH6ogRRu.';

START TRANSACTION;

-- 1. 演示账号，密码均为 123456，密文由 BCryptPasswordEncoder 生成。
INSERT INTO sys_user(username, password, real_name, role, email, phone, status, create_time, update_time)
SELECT 'teacher01', @pwd_123456, '王老师', 'TEACHER', 'teacher01@example.com', '13900000001', 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'teacher01');

INSERT INTO sys_user(username, password, real_name, role, email, phone, status, create_time, update_time)
SELECT 'student01', @pwd_123456, '张同学', 'STUDENT', 'student01@example.com', '13800000001', 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'student01');

SET @teacher_id = (SELECT id FROM sys_user WHERE username = 'teacher01' LIMIT 1);
SET @student_id = (SELECT id FROM sys_user WHERE username = 'student01' LIMIT 1);

-- 2. 默认学科。
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '编程', 'Python 编程基础、算法思维与代码评测训练', 'code', 1, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '编程');
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '数学', '函数、代数、几何与数学应用题训练', 'math', 1, 2, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '数学');
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '英语', '词汇、语法、阅读与写作训练', 'english', 1, 3, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '英语');
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '语文', '阅读理解、表达与写作训练', 'chinese', 1, 4, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '语文');
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '物理', '力学、电学与实验分析训练', 'physics', 1, 5, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '物理');
INSERT INTO subject(name, description, icon, status, sort_order, create_time, update_time)
SELECT '化学', '化学方程式、物质性质与实验分析训练', 'chemistry', 1, 6, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM subject WHERE name = '化学');

SET @subject_programming = (SELECT id FROM subject WHERE name = '编程' LIMIT 1);
SET @subject_math = (SELECT id FROM subject WHERE name = '数学' LIMIT 1);

-- 3. 编程训练题库。
INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT 'Python 基础训练题库', '适合入门阶段，覆盖输入输出、条件判断和基础表达式。', '', 'EASY', '输入输出,条件判断,表达式', @subject_programming, @teacher_id, 1, 0, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = 'Python 基础训练题库' AND creator_id = @teacher_id);

INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT '循环专项训练题库', '围绕 for、while、累加和阶乘进行专项训练。', '', 'EASY', '循环,累加,阶乘', @subject_programming, @teacher_id, 1, 0, 2, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = '循环专项训练题库' AND creator_id = @teacher_id);

INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT '数组与列表专项题库', '训练 Python 列表读取、遍历、统计和聚合。', '', 'MEDIUM', '列表,数组,遍历,统计', @subject_programming, @teacher_id, 1, 0, 3, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = '数组与列表专项题库' AND creator_id = @teacher_id);

INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT '字符串专项题库', '训练字符串遍历、切片、计数和回文判断。', '', 'MEDIUM', '字符串,切片,遍历', @subject_programming, @teacher_id, 1, 0, 4, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = '字符串专项题库' AND creator_id = @teacher_id);

INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT '函数与递归专项题库', '训练函数封装、递归出口和经典递归问题。', '', 'HARD', '函数,递归,算法', @subject_programming, @teacher_id, 1, 0, 5, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = '函数与递归专项题库' AND creator_id = @teacher_id);

INSERT INTO problem_bank(name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order, create_time, update_time)
SELECT '数学基础客观题库', '用于演示选择题、填空题和简答题流程。', '', 'MIXED', '函数,方程,概念理解', @subject_math, @teacher_id, 1, 0, 6, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem_bank WHERE name = '数学基础客观题库' AND creator_id = @teacher_id);

SET @bank_python = (SELECT id FROM problem_bank WHERE name = 'Python 基础训练题库' AND creator_id = @teacher_id LIMIT 1);
SET @bank_loop = (SELECT id FROM problem_bank WHERE name = '循环专项训练题库' AND creator_id = @teacher_id LIMIT 1);
SET @bank_array = (SELECT id FROM problem_bank WHERE name = '数组与列表专项题库' AND creator_id = @teacher_id LIMIT 1);
SET @bank_string = (SELECT id FROM problem_bank WHERE name = '字符串专项题库' AND creator_id = @teacher_id LIMIT 1);
SET @bank_recursion = (SELECT id FROM problem_bank WHERE name = '函数与递归专项题库' AND creator_id = @teacher_id LIMIT 1);
SET @bank_math = (SELECT id FROM problem_bank WHERE name = '数学基础客观题库' AND creator_id = @teacher_id LIMIT 1);

-- 4. 编程题。
INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '两数之和', '输入两个整数 a 和 b，输出它们的和。', '一行两个整数。', '一个整数，表示两数之和。', '1 2', '3', 'EASY', '输入输出,表达式', @bank_python, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '两数之和' AND bank_id = @bank_python);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '判断奇偶数', '输入一个整数 n，判断它是奇数还是偶数。', '一行一个整数。', '如果是偶数输出 even，否则输出 odd。', '4', 'even', 'EASY', '条件判断,取模运算', @bank_python, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '判断奇偶数' AND bank_id = @bank_python);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '求三个数最大值', '输入三个整数，输出其中的最大值。', '一行三个整数。', '一个整数，表示最大值。', '1 7 3', '7', 'EASY', '条件判断,内置函数', @bank_python, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '求三个数最大值' AND bank_id = @bank_python);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '计算圆的面积', '输入圆的半径 r，按 pi=3.14 计算面积，保留两位小数。', '一行一个浮点数。', '面积，保留两位小数。', '2', '12.56', 'EASY', '表达式,格式化输出', @bank_python, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '计算圆的面积' AND bank_id = @bank_python);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '求 1 到 n 的和', '输入正整数 n，输出 1 到 n 的累加和。', '一行一个正整数 n。', '一个整数。', '5', '15', 'EASY', '循环,累加', @bank_loop, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '求 1 到 n 的和' AND bank_id = @bank_loop);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '输出 n 以内的偶数', '输入正整数 n，输出 1 到 n 之间所有偶数，空格分隔。', '一行一个正整数 n。', '一行偶数，空格分隔。', '6', '2 4 6', 'EASY', '循环,取模运算', @bank_loop, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '输出 n 以内的偶数' AND bank_id = @bank_loop);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '阶乘计算', '输入正整数 n，输出 n!。', '一行一个正整数 n。', '一个整数。', '5', '120', 'MEDIUM', '循环,阶乘', @bank_loop, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '阶乘计算' AND bank_id = @bank_loop);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '数组最大值', '输入 n 个整数，输出最大值。', '第一行 n，第二行 n 个整数。', '一个整数，表示最大值。', '5\n1 9 3 7 2', '9', 'EASY', '列表,遍历', @bank_array, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '数组最大值' AND bank_id = @bank_array);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '统计正数个数', '输入 n 个整数，统计其中正数的个数。', '第一行 n，第二行 n 个整数。', '一个整数。', '5\n-1 2 0 3 -5', '2', 'EASY', '列表,统计', @bank_array, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '统计正数个数' AND bank_id = @bank_array);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '数组元素求和', '输入 n 个整数，输出元素和。', '第一行 n，第二行 n 个整数。', '一个整数。', '4\n1 2 3 4', '10', 'EASY', '列表,累加', @bank_array, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '数组元素求和' AND bank_id = @bank_array);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '字符串反转', '输入一个字符串，输出反转后的字符串。', '一行字符串。', '反转后的字符串。', 'hello', 'olleh', 'EASY', '字符串,切片', @bank_string, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '字符串反转' AND bank_id = @bank_string);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '统计字符 a 的个数', '输入一个字符串，统计字符 a 出现的次数。', '一行字符串。', '一个整数。', 'banana', '3', 'EASY', '字符串,计数', @bank_string, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '统计字符 a 的个数' AND bank_id = @bank_string);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '判断回文字符串', '输入一个字符串，判断是否为回文。', '一行字符串。', '是回文输出 yes，否则输出 no。', 'level', 'yes', 'MEDIUM', '字符串,切片,条件判断', @bank_string, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '判断回文字符串' AND bank_id = @bank_string);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '斐波那契数列', '输入 n，输出第 n 个斐波那契数。约定 f(1)=1，f(2)=1。', '一行一个正整数 n。', '一个整数。', '6', '8', 'MEDIUM', '递归,循环,动态规划', @bank_recursion, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '斐波那契数列' AND bank_id = @bank_recursion);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, creator_id, status, create_time, update_time)
SELECT '求最大公约数', '输入两个正整数，输出它们的最大公约数。', '一行两个正整数。', '一个整数。', '12 18', '6', 'MEDIUM', '函数,递归,欧几里得算法', @bank_recursion, @subject_programming, 'PROGRAMMING', @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '求最大公约数' AND bank_id = @bank_recursion);

-- 5. 数学多题型演示题。
INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status, create_time, update_time)
SELECT '一次函数图像性质', '函数 y = 2x + 1 的图像是什么？', '', '', '', '', 'EASY', '一次函数,函数图像', @bank_math, @subject_math, 'CHOICE', 'B', '', 5, @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '一次函数图像性质' AND bank_id = @bank_math);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status, create_time, update_time)
SELECT '平方差公式填空', '请填写平方差公式：(a+b)(a-b)=____。', '', '', '', '', 'EASY', '代数公式,平方差公式', @bank_math, @subject_math, 'FILL_BLANK', 'a^2-b^2\na²-b²', '', 5, @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '平方差公式填空' AND bank_id = @bank_math);

INSERT INTO problem(title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status, create_time, update_time)
SELECT '解释函数单调性', '请用自己的话解释什么是函数的单调递增，并举一个简单例子。', '', '', '', '', 'MEDIUM', '函数,单调性,概念理解', @bank_math, @subject_math, 'SHORT_ANSWER', '在某个区间内，随着自变量 x 增大，函数值 f(x) 也增大，则称函数在该区间单调递增。例如 y=x 在全体实数上单调递增。', '说明区间；说明 x 增大时 f(x) 增大；能举出合理例子。', 10, @teacher_id, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM problem WHERE title = '解释函数单调性' AND bank_id = @bank_math);

-- 6. 编程题测试用例：每题至少 1 个样例和 2 个隐藏用例。
SET @p_two_sum = (SELECT id FROM problem WHERE title = '两数之和' AND bank_id = @bank_python LIMIT 1);
SET @p_odd_even = (SELECT id FROM problem WHERE title = '判断奇偶数' AND bank_id = @bank_python LIMIT 1);
SET @p_max3 = (SELECT id FROM problem WHERE title = '求三个数最大值' AND bank_id = @bank_python LIMIT 1);
SET @p_circle = (SELECT id FROM problem WHERE title = '计算圆的面积' AND bank_id = @bank_python LIMIT 1);
SET @p_sum_n = (SELECT id FROM problem WHERE title = '求 1 到 n 的和' AND bank_id = @bank_loop LIMIT 1);
SET @p_even_n = (SELECT id FROM problem WHERE title = '输出 n 以内的偶数' AND bank_id = @bank_loop LIMIT 1);
SET @p_factorial = (SELECT id FROM problem WHERE title = '阶乘计算' AND bank_id = @bank_loop LIMIT 1);
SET @p_array_max = (SELECT id FROM problem WHERE title = '数组最大值' AND bank_id = @bank_array LIMIT 1);
SET @p_positive_count = (SELECT id FROM problem WHERE title = '统计正数个数' AND bank_id = @bank_array LIMIT 1);
SET @p_array_sum = (SELECT id FROM problem WHERE title = '数组元素求和' AND bank_id = @bank_array LIMIT 1);
SET @p_reverse = (SELECT id FROM problem WHERE title = '字符串反转' AND bank_id = @bank_string LIMIT 1);
SET @p_count_a = (SELECT id FROM problem WHERE title = '统计字符 a 的个数' AND bank_id = @bank_string LIMIT 1);
SET @p_palindrome = (SELECT id FROM problem WHERE title = '判断回文字符串' AND bank_id = @bank_string LIMIT 1);
SET @p_fib = (SELECT id FROM problem WHERE title = '斐波那契数列' AND bank_id = @bank_recursion LIMIT 1);
SET @p_gcd = (SELECT id FROM problem WHERE title = '求最大公约数' AND bank_id = @bank_recursion LIMIT 1);

INSERT INTO test_case(problem_id, input_data, expected_output, is_sample, sort_order, status, create_time, update_time)
SELECT problem_id, input_data, expected_output, is_sample, sort_order, 1, @now, @now
FROM (
    SELECT @p_two_sum problem_id, '1 2' input_data, '3' expected_output, 1 is_sample, 1 sort_order UNION ALL
    SELECT @p_two_sum, '10 20', '30', 0, 2 UNION ALL
    SELECT @p_two_sum, '-1 5', '4', 0, 3 UNION ALL
    SELECT @p_odd_even, '4', 'even', 1, 1 UNION ALL
    SELECT @p_odd_even, '7', 'odd', 0, 2 UNION ALL
    SELECT @p_odd_even, '0', 'even', 0, 3 UNION ALL
    SELECT @p_max3, '1 7 3', '7', 1, 1 UNION ALL
    SELECT @p_max3, '-1 -7 -3', '-1', 0, 2 UNION ALL
    SELECT @p_max3, '9 9 2', '9', 0, 3 UNION ALL
    SELECT @p_circle, '2', '12.56', 1, 1 UNION ALL
    SELECT @p_circle, '1', '3.14', 0, 2 UNION ALL
    SELECT @p_circle, '3', '28.26', 0, 3 UNION ALL
    SELECT @p_sum_n, '5', '15', 1, 1 UNION ALL
    SELECT @p_sum_n, '10', '55', 0, 2 UNION ALL
    SELECT @p_sum_n, '1', '1', 0, 3 UNION ALL
    SELECT @p_even_n, '6', '2 4 6', 1, 1 UNION ALL
    SELECT @p_even_n, '9', '2 4 6 8', 0, 2 UNION ALL
    SELECT @p_even_n, '1', '', 0, 3 UNION ALL
    SELECT @p_factorial, '5', '120', 1, 1 UNION ALL
    SELECT @p_factorial, '1', '1', 0, 2 UNION ALL
    SELECT @p_factorial, '7', '5040', 0, 3 UNION ALL
    SELECT @p_array_max, '5\n1 9 3 7 2', '9', 1, 1 UNION ALL
    SELECT @p_array_max, '4\n-5 -2 -9 -1', '-1', 0, 2 UNION ALL
    SELECT @p_array_max, '3\n6 6 6', '6', 0, 3 UNION ALL
    SELECT @p_positive_count, '5\n-1 2 0 3 -5', '2', 1, 1 UNION ALL
    SELECT @p_positive_count, '4\n1 2 3 4', '4', 0, 2 UNION ALL
    SELECT @p_positive_count, '3\n-1 0 -2', '0', 0, 3 UNION ALL
    SELECT @p_array_sum, '4\n1 2 3 4', '10', 1, 1 UNION ALL
    SELECT @p_array_sum, '3\n-1 5 6', '10', 0, 2 UNION ALL
    SELECT @p_array_sum, '1\n8', '8', 0, 3 UNION ALL
    SELECT @p_reverse, 'hello', 'olleh', 1, 1 UNION ALL
    SELECT @p_reverse, 'Python', 'nohtyP', 0, 2 UNION ALL
    SELECT @p_reverse, 'a', 'a', 0, 3 UNION ALL
    SELECT @p_count_a, 'banana', '3', 1, 1 UNION ALL
    SELECT @p_count_a, 'apple', '1', 0, 2 UNION ALL
    SELECT @p_count_a, 'BBBB', '0', 0, 3 UNION ALL
    SELECT @p_palindrome, 'level', 'yes', 1, 1 UNION ALL
    SELECT @p_palindrome, 'python', 'no', 0, 2 UNION ALL
    SELECT @p_palindrome, 'aba', 'yes', 0, 3 UNION ALL
    SELECT @p_fib, '6', '8', 1, 1 UNION ALL
    SELECT @p_fib, '1', '1', 0, 2 UNION ALL
    SELECT @p_fib, '8', '21', 0, 3 UNION ALL
    SELECT @p_gcd, '12 18', '6', 1, 1 UNION ALL
    SELECT @p_gcd, '7 13', '1', 0, 2 UNION ALL
    SELECT @p_gcd, '24 36', '12', 0, 3
) cases
WHERE problem_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM test_case tc
      WHERE tc.problem_id = cases.problem_id
        AND tc.sort_order = cases.sort_order
        AND tc.input_data = cases.input_data
  );

-- 7. 选择题选项。
SET @p_choice = (SELECT id FROM problem WHERE title = '一次函数图像性质' AND bank_id = @bank_math LIMIT 1);
INSERT INTO question_option(problem_id, option_key, option_content, is_correct, sort_order, create_time)
SELECT @p_choice, 'A', '一条抛物线', 0, 1, @now
WHERE @p_choice IS NOT NULL AND NOT EXISTS (SELECT 1 FROM question_option WHERE problem_id = @p_choice AND option_key = 'A');
INSERT INTO question_option(problem_id, option_key, option_content, is_correct, sort_order, create_time)
SELECT @p_choice, 'B', '一条直线', 1, 2, @now
WHERE @p_choice IS NOT NULL AND NOT EXISTS (SELECT 1 FROM question_option WHERE problem_id = @p_choice AND option_key = 'B');
INSERT INTO question_option(problem_id, option_key, option_content, is_correct, sort_order, create_time)
SELECT @p_choice, 'C', '一个圆', 0, 3, @now
WHERE @p_choice IS NOT NULL AND NOT EXISTS (SELECT 1 FROM question_option WHERE problem_id = @p_choice AND option_key = 'C');
INSERT INTO question_option(problem_id, option_key, option_content, is_correct, sort_order, create_time)
SELECT @p_choice, 'D', '一条双曲线', 0, 4, @now
WHERE @p_choice IS NOT NULL AND NOT EXISTS (SELECT 1 FROM question_option WHERE problem_id = @p_choice AND option_key = 'D');

-- 8. 知识库与 RAG 演示文档。
INSERT INTO knowledge_base(name, description, subject_id, creator_id, status, sort_order, create_time, update_time)
SELECT 'Python 编程基础知识库', '用于 AI 诊断时增强输入输出、条件、循环、列表和字符串等基础知识。', @subject_programming, @teacher_id, 1, 1, @now, @now
WHERE NOT EXISTS (SELECT 1 FROM knowledge_base WHERE name = 'Python 编程基础知识库' AND creator_id = @teacher_id);

SET @kb_python = (SELECT id FROM knowledge_base WHERE name = 'Python 编程基础知识库' AND creator_id = @teacher_id LIMIT 1);

INSERT INTO knowledge_document(base_id, title, content, knowledge_tags, status, chunk_count, create_time, update_time)
SELECT @kb_python, 'Python 基础调试指南',
       '输入输出：Python 中可以使用 input() 读取一行文本，使用 split() 拆分，再结合 map(int, ...) 转换为整数。输出时要注意题目要求的格式，不能多输出额外提示文字。\n条件判断：奇偶判断常用 n % 2 == 0。分支逻辑要覆盖边界值，例如 0、负数或相等情况。\n循环与累加：for 循环常用于固定次数遍历，累加时需要先初始化变量，再在循环中更新。\n列表处理：读取多个整数后通常保存到 list 中，可以通过遍历完成最大值、求和和计数。\n字符串处理：字符串支持切片 s[::-1]，也可以逐字符遍历统计目标字符。判断回文时要比较原字符串与反转字符串。',
       '输入输出,条件判断,循环,列表,字符串,调试方法',
       1, 5, @now, @now
WHERE @kb_python IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM knowledge_document WHERE base_id = @kb_python AND title = 'Python 基础调试指南');

SET @doc_python = (SELECT id FROM knowledge_document WHERE base_id = @kb_python AND title = 'Python 基础调试指南' LIMIT 1);

INSERT INTO knowledge_chunk(base_id, document_id, subject_id, document_title, knowledge_tags, chunk_text, chunk_order, status, create_time, update_time)
SELECT @kb_python, @doc_python, @subject_programming, 'Python 基础调试指南', '输入输出,调试方法',
       'Python 中可以使用 input() 读取一行文本，使用 split() 拆分，再结合 map(int, ...) 转换为整数。输出时要注意题目要求的格式，不能多输出额外提示文字。', 1, 1, @now, @now
WHERE @doc_python IS NOT NULL AND NOT EXISTS (SELECT 1 FROM knowledge_chunk WHERE document_id = @doc_python AND chunk_order = 1);

INSERT INTO knowledge_chunk(base_id, document_id, subject_id, document_title, knowledge_tags, chunk_text, chunk_order, status, create_time, update_time)
SELECT @kb_python, @doc_python, @subject_programming, 'Python 基础调试指南', '条件判断,取模运算',
       '奇偶判断常用 n % 2 == 0。分支逻辑要覆盖边界值，例如 0、负数或相等情况。', 2, 1, @now, @now
WHERE @doc_python IS NOT NULL AND NOT EXISTS (SELECT 1 FROM knowledge_chunk WHERE document_id = @doc_python AND chunk_order = 2);

INSERT INTO knowledge_chunk(base_id, document_id, subject_id, document_title, knowledge_tags, chunk_text, chunk_order, status, create_time, update_time)
SELECT @kb_python, @doc_python, @subject_programming, 'Python 基础调试指南', '循环,累加,阶乘',
       'for 循环常用于固定次数遍历，累加时需要先初始化变量，再在循环中更新。阶乘计算要从 1 开始连乘到 n。', 3, 1, @now, @now
WHERE @doc_python IS NOT NULL AND NOT EXISTS (SELECT 1 FROM knowledge_chunk WHERE document_id = @doc_python AND chunk_order = 3);

INSERT INTO knowledge_chunk(base_id, document_id, subject_id, document_title, knowledge_tags, chunk_text, chunk_order, status, create_time, update_time)
SELECT @kb_python, @doc_python, @subject_programming, 'Python 基础调试指南', '列表,数组,遍历,统计',
       '读取多个整数后通常保存到 list 中，可以通过遍历完成最大值、求和和计数。需要注意输入数量和实际列表长度一致。', 4, 1, @now, @now
WHERE @doc_python IS NOT NULL AND NOT EXISTS (SELECT 1 FROM knowledge_chunk WHERE document_id = @doc_python AND chunk_order = 4);

INSERT INTO knowledge_chunk(base_id, document_id, subject_id, document_title, knowledge_tags, chunk_text, chunk_order, status, create_time, update_time)
SELECT @kb_python, @doc_python, @subject_programming, 'Python 基础调试指南', '字符串,切片,计数',
       '字符串支持切片 s[::-1]，也可以逐字符遍历统计目标字符。判断回文时要比较原字符串与反转字符串。', 5, 1, @now, @now
WHERE @doc_python IS NOT NULL AND NOT EXISTS (SELECT 1 FROM knowledge_chunk WHERE document_id = @doc_python AND chunk_order = 5);

-- 9. 回填题库题目数量，保证首页和管理页展示稳定。
UPDATE problem_bank pb
SET problem_count = (
    SELECT COUNT(1)
    FROM problem p
    WHERE p.bank_id = pb.id AND p.status = 1
)
WHERE pb.creator_id = @teacher_id;

COMMIT;
