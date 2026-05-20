USE agentedu;;
SET FOREIGN_KEY_CHECKS = 0;;
DELETE FROM submit_case_result;;
DELETE FROM ai_feedback;;
DELETE FROM ai_error_cache;;
DELETE FROM submit_record;;
DELETE FROM test_case;;
DELETE FROM question_option;;
DELETE FROM problem;;
DELETE FROM problem_bank;;
SET FOREIGN_KEY_CHECKS = 1;;
INSERT INTO subject (name, description, icon, status, sort_order) VALUES ('编程', '编程与算法训练', 'code', 1, 1) ON DUPLICATE KEY UPDATE status = 1;;
SET @subject_id = (SELECT id FROM subject WHERE name = '编程' LIMIT 1);;
SET @teacher_id = (SELECT id FROM sys_user WHERE username = 'teacher01' LIMIT 1);;
SET @teacher_id = IFNULL(@teacher_id, (SELECT id FROM sys_user WHERE role = 'TEACHER' ORDER BY id LIMIT 1));;
INSERT INTO problem_bank (name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order) VALUES ('面向对象程序设计 A卷题库', '2024-2025-2《面向对象程序设计》考试试卷与参考答案', NULL, 'MIXED', 'Java,面向对象程序设计,考试题库', @subject_id, @teacher_id, 1, 34, 0);;
SET @a_bank_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-01 Java属于那种语言？（  ）', 'Java属于那种语言？（  ）', NULL, NULL, NULL, NULL, 'EASY', 'Java语言基础', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '机器语言', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '汇编语言', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '面向过程语言', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '面向对象语言', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-02 下面4种类型的文件中（  ）可以在Java虚拟机中运行。', '下面4种类型的文件中（  ）可以在Java虚拟机中运行。', NULL, NULL, NULL, NULL, 'EASY', 'JVM与字节码', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '.java', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '.jre', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '.exe', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '.class', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-03 下面哪个选项是Java的有效标识符（  ）', '下面哪个选项是Java的有效标识符（  ）', NULL, NULL, NULL, NULL, 'EASY', '标识符', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '123abc', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '_variable', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '#number', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'class', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-04 下列选项中，不属于基本数据类型的是（  ）', '下列选项中，不属于基本数据类型的是（  ）', NULL, NULL, NULL, NULL, 'EASY', '基本数据类型', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'string', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'short', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'boolean', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'char', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-05 下列类的定义中不正确的是（  ）', '下列类的定义中不正确的是（  ）', NULL, NULL, NULL, NULL, 'EASY', '类定义', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'class X{ ... }', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class X extends Y { ... }', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'static class X imlements Y1，Y2 { ... }', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'public class X extends Applet { ... }', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-06 对于声明为private、protected以及public的类成员在类外部（  ）', '对于声明为private、protected以及public的类成员在类外部（  ）', NULL, NULL, NULL, NULL, 'EASY', '访问控制', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '只能访问声明为public的类成员', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '只能访问声明为protected和public的类成员', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '都可以访问', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '都不能访问', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-07 Java中的哪个关键字用于实现接口（  ）', 'Java中的哪个关键字用于实现接口（  ）', NULL, NULL, NULL, NULL, 'EASY', '接口', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'implements', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'extends', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'inherits', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'interface', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-08 String s="itcast"，则s.substring(3,4)返回的字符串是以下选项中的哪个（  ）', 'String s="itcast"，则s.substring(3,4)返回的字符串是以下选项中的哪个（  ）', NULL, NULL, NULL, NULL, 'EASY', 'String', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'ca', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'c', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'a', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'as', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-09 使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（  ）', '使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（  ）', NULL, NULL, NULL, NULL, 'EASY', '集合与迭代器', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'next()', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'hashcode()', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'hasPrevious()', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'hasNext()', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-CHOICE-10 下列选项中，哪个类是用来读取文本的字节流（  ）', '下列选项中，哪个类是用来读取文本的字节流（  ）', NULL, NULL, NULL, NULL, 'EASY', 'IO流', @a_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'FileReader', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'FileWriter', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'FileInputStream', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'FileOutputStream', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-MULTI-01 下列哪些是Java中的基本数据类型？', '下列哪些是Java中的基本数据类型？', NULL, NULL, NULL, NULL, 'EASY', '基本数据类型', @a_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'int', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'String', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'double', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'boolean', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-MULTI-02 面向对象的三个特性是？', '面向对象的三个特性是？', NULL, NULL, NULL, NULL, 'EASY', '面向对象特征', @a_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '封装性', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '继承性', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '抽象性', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '多态性', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-MULTI-03 有A、B、C三个类，以下继承关系正确的是？', '有A、B、C三个类，以下继承关系正确的是？', NULL, NULL, NULL, NULL, 'EASY', '继承', @a_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'class A{}
class B{}
class C extends A,B{}', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class A{}
class B extends A{}
class C extends B{}', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'class A{}
class B extends A{}
class C extends A{}', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'class A{}
class B,C extends A{}', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-MULTI-04 在Java中，以下关于接口的描述正确的是？', '在Java中，以下关于接口的描述正确的是？', NULL, NULL, NULL, NULL, 'EASY', '接口', @a_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '接口中的所有方法不是抽象的', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '接口能被实例化', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '一个类可以实现多个接口', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '接口之间可以相互继承', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-MULTI-05 以下哪些集合可以保存具有映射关系的数据？', '以下哪些集合可以保存具有映射关系的数据？', NULL, NULL, NULL, NULL, 'EASY', 'Map集合', @a_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'ArrayList', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'TreeMap', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'HashMap', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'TreeSet', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-FILL-01 将.Java源文件编译为.class文件的是__________命令。', '将.Java源文件编译为.class文件的是__________命令。', NULL, NULL, NULL, NULL, 'EASY', 'Java编译', @a_bank_id, @subject_id, 'FILL_BLANK', 'javac', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-FILL-02 Java程序代码必须放在一个类中，类使用__________关键词定义。', 'Java程序代码必须放在一个类中，类使用__________关键词定义。', NULL, NULL, NULL, NULL, 'EASY', '类定义', @a_bank_id, @subject_id, 'FILL_BLANK', 'class', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-FILL-03 假设 int x=2，三元表达式 x>0?x+1:5 的结果为__________。', '假设 int x=2，三元表达式 x>0?x+1:5 的结果为__________。', NULL, NULL, NULL, NULL, 'EASY', '表达式', @a_bank_id, @subject_id, 'FILL_BLANK', '3', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-FILL-04 一个类如果要实现一个接口，可以通过关键字________来实现这个接口。', '一个类如果要实现一个接口，可以通过关键字________来实现这个接口。', NULL, NULL, NULL, NULL, 'EASY', '接口', @a_bank_id, @subject_id, 'FILL_BLANK', 'implements', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-FILL-05 Java中的I/O流，按照操作文件类型的不同，可分为字节流和__________。', 'Java中的I/O流，按照操作文件类型的不同，可分为字节流和__________。', NULL, NULL, NULL, NULL, 'EASY', 'IO流', @a_bank_id, @subject_id, 'FILL_BLANK', '字符流', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-TF-01 若 x=5，则表达式 (x+5)/3 的值是3。', '若 x=5，则表达式 (x+5)/3 的值是3。', NULL, NULL, NULL, NULL, 'EASY', '表达式', @a_bank_id, @subject_id, 'TRUE_FALSE', '√', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-TF-02 在Java中，方法的重载和重写都是实现多态性的手段。', '在Java中，方法的重载和重写都是实现多态性的手段。', NULL, NULL, NULL, NULL, 'EASY', '多态', @a_bank_id, @subject_id, 'TRUE_FALSE', '√', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-TF-03 抽象类不一定有抽象方法，有抽象方法的类不一定是抽象类。', '抽象类不一定有抽象方法，有抽象方法的类不一定是抽象类。', NULL, NULL, NULL, NULL, 'EASY', '抽象类', @a_bank_id, @subject_id, 'TRUE_FALSE', '×', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-TF-04 在try…catch语句中，try语句块存放可能发生异常的语句。', '在try…catch语句中，try语句块存放可能发生异常的语句。', NULL, NULL, NULL, NULL, 'EASY', '异常处理', @a_bank_id, @subject_id, 'TRUE_FALSE', '√', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-TF-05 Set集合是通过键值对的方式来存储对象的。', 'Set集合是通过键值对的方式来存储对象的。', NULL, NULL, NULL, NULL, 'EASY', '集合', @a_bank_id, @subject_id, 'TRUE_FALSE', '×', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-OUTPUT-01 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
public static void main(String[] args) {
    int a[]={10,20,30,40,50,60,70,80,90};
    int s=0;
    for(int i=0;i<a.length;i++){
        if(a[i]%3==0){
            s+=a[i];
        }
    }
    System.out.println(s);
}', NULL, NULL, NULL, NULL, 'EASY', '数组与循环', @a_bank_id, @subject_id, 'FILL_BLANK', '180', NULL, 4, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-OUTPUT-02 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
class Test {
    private static String name;
    static {
        name = "World";
        System.out.print(name);
    }
    public static void main(String[] args) {
        System.out.print("Hello");
        Test test = new Test();
    }
}', NULL, NULL, NULL, NULL, 'EASY', '静态代码块', @a_bank_id, @subject_id, 'FILL_BLANK', 'WorldHello', NULL, 4, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-OUTPUT-03 阅读程序，写出返回值为 true 的变量名：', '阅读程序，写出返回值为 true 的变量名：
class Dog {
    public String name;
    Dog(String name){ this.name = name; }
}
public class Demo1 {
    public static void main(String[] args){
        Dog dog1 = new Dog("xiaohuang");
        Dog dog2 = new Dog("xiaohuang");
        String s3 = "xiaohuang";
        String s4 = "xiaohuang";
        boolean result1 = (dog1 == dog2);
        boolean result2 = (s3 == s4);
        System.out.println(result1);
        System.out.println(result2);
    }
}', NULL, NULL, NULL, NULL, 'EASY', '对象引用与字符串常量池', @a_bank_id, @subject_id, 'FILL_BLANK', 'result2', NULL, 4, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-OUTPUT-04 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
class Demo {
    public static void main(String[] args){
        int x = div(1,2);
        try {
        } catch(Exception e){
            System.out.println(e);
        }
        System.out.println(x);
    }
    public static int div(int a,int b){
        return a / b;
    }
}', NULL, NULL, NULL, NULL, 'EASY', '整数除法与异常', @a_bank_id, @subject_id, 'FILL_BLANK', '0', NULL, 4, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-OUTPUT-05 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
public static void main(String[] args) {
    StringBuffer str1 = new StringBuffer("abc");
    str1.append("def");
    str1.delete(1,3);
    System.out.println(str1);
}', NULL, NULL, NULL, NULL, 'EASY', 'StringBuffer', @a_bank_id, @subject_id, 'FILL_BLANK', 'adef', NULL, 4, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-SHORT-01 简述面向对象的三大特征。', '简述面向对象的三大特征。', NULL, NULL, NULL, NULL, 'MEDIUM', '面向对象特征', @a_bank_id, @subject_id, 'SHORT_ANSWER', '面向对象的三大特征是封装性、继承性和多态性。封装强调将对象的属性和行为封装起来，隐藏具体实现细节；继承描述类与类之间的关系，可在不重写原有代码的情况下扩展功能；多态指同一操作作用于不同对象时产生不同行为，Java中常通过方法重写和接口实现体现。', '面向对象的三大特征是封装性、继承性和多态性。封装强调将对象的属性和行为封装起来，隐藏具体实现细节；继承描述类与类之间的关系，可在不重写原有代码的情况下扩展功能；多态指同一操作作用于不同对象时产生不同行为，Java中常通过方法重写和接口实现体现。', 5, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-SHORT-02 简述 try...catch 语句的异常处理流程。', '简述 try...catch 语句的异常处理流程。', NULL, NULL, NULL, NULL, 'MEDIUM', '异常处理', @a_bank_id, @subject_id, 'SHORT_ANSWER', '程序先执行 try 代码块；若无异常则跳过 catch；若发生异常则停止执行 try 中剩余语句并寻找匹配的 catch 块处理异常；可选 finally 块无论是否异常都会执行，常用于资源释放；异常被捕获后继续执行后续代码，未捕获则向上抛出。', '程序先执行 try 代码块；若无异常则跳过 catch；若发生异常则停止执行 try 中剩余语句并寻找匹配的 catch 块处理异常；可选 finally 块无论是否异常都会执行，常用于资源释放；异常被捕获后继续执行后续代码，未捕获则向上抛出。', 5, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-PROG-01 编写2个方法：有参方法 getMax() 用来求数组中的最大值，有参方法 getMin() 用来求数组中的最小值。在 main 函数中初始化数组 arr={4,1,6,3,9,8,-2}，调用 getMax() 打...', '编写2个方法：有参方法 getMax() 用来求数组中的最大值，有参方法 getMin() 用来求数组中的最小值。在 main 函数中初始化数组 arr={4,1,6,3,9,8,-2}，调用 getMax() 打印最大值，调用 getMin() 打印最小值。请使用 Java 编写，主类名为 Main。', NULL, '请按题目要求输出。', '', '9
-2', 'MEDIUM', '数组与方法', @a_bank_id, @subject_id, 'PROGRAMMING', '参考实现应定义 getMax(int[] arr)、getMin(int[] arr)，遍历数组求最大值和最小值，并输出最大值9和最小值-2。', NULL, 10, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO test_case (problem_id, input_data, expected_output, is_sample, sort_order, status) VALUES (@problem_id, '', '9
-2', 1, 0, 1);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('A-PROG-02 编写程序：定义接口 Shape，包含计算面积 getArea() 方法。创建 Circle 类计算圆面积，半径 double radius；创建 Triangles 类计算三角形面积，边长 double sideA...', '编写程序：定义接口 Shape，包含计算面积 getArea() 方法。创建 Circle 类计算圆面积，半径 double radius；创建 Triangles 类计算三角形面积，边长 double sideA、sideB、sideC；在 Main 类中测试半径为5.0的圆面积和边长为3、4、5的三角形面积。', NULL, '请按题目要求输出。', '', '半径为5的圆面积: 78.54
边长为3,4,5的三角形面积: 6.0', 'MEDIUM', '接口与多态', @a_bank_id, @subject_id, 'PROGRAMMING', '参考实现应定义 Shape 接口、Circle 类、Triangles 类，Circle 使用 Math.PI*r*r，Triangles 使用海伦公式。', NULL, 10, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO test_case (problem_id, input_data, expected_output, is_sample, sort_order, status) VALUES (@problem_id, '', '半径为5的圆面积: 78.54
边长为3,4,5的三角形面积: 6.0', 1, 0, 1);;
INSERT INTO problem_bank (name, description, cover_url, difficulty, knowledge_tags, subject_id, creator_id, status, problem_count, sort_order) VALUES ('面向对象程序设计 B卷题库', '2024-2025-2《面向对象程序设计》考试试卷与参考答案', NULL, 'MIXED', 'Java,面向对象程序设计,考试题库', @subject_id, @teacher_id, 1, 34, 0);;
SET @b_bank_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-01 在JDK的bin目录下有许多exe可执行文件，其中java.exe命令的作用是（ ）', '在JDK的bin目录下有许多exe可执行文件，其中java.exe命令的作用是（ ）', NULL, NULL, NULL, NULL, 'EASY', 'JDK工具', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'Java文档制作工具', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'Java解释器', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'Java编译器', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'Java启动器', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-02 下面那种类型的文件可以在Java虚拟机中运行？（ ）', '下面那种类型的文件可以在Java虚拟机中运行？（ ）', NULL, NULL, NULL, NULL, 'EASY', 'JVM与字节码', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '.java', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '.jre', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '.exe', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '.class', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-03 下列选项中，那些属于合法的标识符？（  ）', '下列选项中，那些属于合法的标识符？（  ）', NULL, NULL, NULL, NULL, 'EASY', '标识符', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'username', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '123username', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'Hello World', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-04 下列选项中，使用比较运算符正确的是（ ）', '下列选项中，使用比较运算符正确的是（ ）', NULL, NULL, NULL, NULL, 'EASY', '比较运算符', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '4!=3结果为false', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '4==3 结果为false', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '4<=3结果为true', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '4>=3结果为true', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-05 下列关于构造方法的描述中，错误的是（  ）', '下列关于构造方法的描述中，错误的是（  ）', NULL, NULL, NULL, NULL, 'EASY', '构造方法', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '构造方法的方法名必须和类名一致', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '构造方法不能写返回值类型', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '构造方法可以重载', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '构造方法的访问权限必须和类的访问权限一致', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-06 现有两个类A、B，以下描述中表示B继承自A的是（）', '现有两个类A、B，以下描述中表示B继承自A的是（）', NULL, NULL, NULL, NULL, 'EASY', '继承', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'class A extends', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class B implements A', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'class A implements B', 0, 3);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'class B extends A', 1, 4);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-07 下列选项中，可以正确实现String初始化的是（ ）', '下列选项中，可以正确实现String初始化的是（ ）', NULL, NULL, NULL, NULL, 'EASY', 'String', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'String str = "abc"', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'String str = ''abc''', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'String str = abc', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'String str = 0', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-08 使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（   ）', '使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（   ）', NULL, NULL, NULL, NULL, 'EASY', '集合与迭代器', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'next()', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'hash()', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'hasPrevious()', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'hasNext()', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-09 在Java中，下面哪个关键字用于捕获异常（  ）', '在Java中，下面哪个关键字用于捕获异常（  ）', NULL, NULL, NULL, NULL, 'EASY', '异常处理', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'catch', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'throw', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'throws', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'try', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-CHOICE-10 File类中以字符串形式返回文件绝对路径的方法是哪一项？（  ）', 'File类中以字符串形式返回文件绝对路径的方法是哪一项？（  ）', NULL, NULL, NULL, NULL, 'EASY', 'File类', @b_bank_id, @subject_id, 'CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'getParent()', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'getName()', 0, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'getAbsolutePath()', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'getPath()', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-MULTI-01 Java语言的特点有哪些？', 'Java语言的特点有哪些？', NULL, NULL, NULL, NULL, 'EASY', 'Java语言基础', @b_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '简单性', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '面向对象', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '跨平台性', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '支持多线程', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-MULTI-02 在Java中，下面哪些关键字用于定义循环结构？', '在Java中，下面哪些关键字用于定义循环结构？', NULL, NULL, NULL, NULL, 'EASY', '循环结构', @b_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'For', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'while', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'do-while', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'switch', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-MULTI-03 面向对象的三个特性是？', '面向对象的三个特性是？', NULL, NULL, NULL, NULL, 'EASY', '面向对象特征', @b_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', '封装性', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', '继承性', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', '抽象性', 0, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', '多态性', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-MULTI-04 有A、B、C三个类，以下继承关系正确的是？', '有A、B、C三个类，以下继承关系正确的是？', NULL, NULL, NULL, NULL, 'EASY', '继承', @b_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'class A{}
class B{}
class C extends A,B{}', 0, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'class A{}
class B extends A{}
class C extends B{}', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'class A{}
class B extends A{}
class C extends A{}', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'class A{}
class B,C extends A{}', 0, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-MULTI-05 以下属于Map接口集合常用方法的有？', '以下属于Map接口集合常用方法的有？', NULL, NULL, NULL, NULL, 'EASY', 'Map集合', @b_bank_id, @subject_id, 'MULTI_CHOICE', NULL, NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'A', 'boolean containsKey(Object key)', 1, 0);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'B', 'Collection values()', 1, 1);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'C', 'void forEach(BiConsumer action)', 1, 2);;
INSERT INTO question_option (problem_id, option_key, option_content, is_correct, sort_order) VALUES (@problem_id, 'D', 'boolean replace(Object key, Object value)', 1, 3);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILL-01 JDK中，存放可执行程序的目录是________。', 'JDK中，存放可执行程序的目录是________。', NULL, NULL, NULL, NULL, 'EASY', 'JDK目录', @b_bank_id, @subject_id, 'FILL_BLANK', 'bin目录', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILL-02 Java程序代码必须放在一个类中，类使用________关键词定义。', 'Java程序代码必须放在一个类中，类使用________关键词定义。', NULL, NULL, NULL, NULL, 'EASY', '类定义', @b_bank_id, @subject_id, 'FILL_BLANK', 'class', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILL-03 静态方法必须使用________关键字来修饰。', '静态方法必须使用________关键字来修饰。', NULL, NULL, NULL, NULL, 'EASY', 'static关键字', @b_bank_id, @subject_id, 'FILL_BLANK', 'static', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILL-04 在继承关系中，子类会自动继承父类中的方法，但有时在子类中需要对继承的方法进行一些修改，即对父类的方法进行________。', '在继承关系中，子类会自动继承父类中的方法，但有时在子类中需要对继承的方法进行一些修改，即对父类的方法进行________。', NULL, NULL, NULL, NULL, 'EASY', '方法重写', @b_bank_id, @subject_id, 'FILL_BLANK', '重写', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILL-05 在程序中，获取字符串长度的方法是________。', '在程序中，获取字符串长度的方法是________。', NULL, NULL, NULL, NULL, 'EASY', 'String', @b_bank_id, @subject_id, 'FILL_BLANK', 'length()', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-TF-01 编译Java程序需要使用 java 命令。', '编译Java程序需要使用 java 命令。', NULL, NULL, NULL, NULL, 'EASY', 'JDK工具', @b_bank_id, @subject_id, 'TRUE_FALSE', '×', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-TF-02 在成员方法中出现的 this 关键字，代表的是调用这个方法的对象。', '在成员方法中出现的 this 关键字，代表的是调用这个方法的对象。', NULL, NULL, NULL, NULL, 'EASY', 'this关键字', @b_bank_id, @subject_id, 'TRUE_FALSE', '×', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-TF-03 Exception类称为异常类，它表示程序本身可以处理的错误，在开发Java程序中进行的异常处理，都是针对Exception类及其子类。', 'Exception类称为异常类，它表示程序本身可以处理的错误，在开发Java程序中进行的异常处理，都是针对Exception类及其子类。', NULL, NULL, NULL, NULL, 'EASY', '异常处理', @b_bank_id, @subject_id, 'TRUE_FALSE', '√', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-TF-04 Pattern类用于创建一个正则表达式，也可以说创建一个匹配模式，它的构造方法是私有的，不可以直接创建。', 'Pattern类用于创建一个正则表达式，也可以说创建一个匹配模式，它的构造方法是私有的，不可以直接创建。', NULL, NULL, NULL, NULL, 'EASY', '正则表达式', @b_bank_id, @subject_id, 'TRUE_FALSE', '√', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-TF-05 Set集合是通过键值对的方式来存储对象的。', 'Set集合是通过键值对的方式来存储对象的。', NULL, NULL, NULL, NULL, 'EASY', '集合', @b_bank_id, @subject_id, 'TRUE_FALSE', '×', NULL, 2, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-OUTPUT-01 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
public static void main(String[] args){
    int[] arr = {23,544,56,1,23,45,21,1,78};
    int m = arr[0];
    for (int i = 1; i < arr.length; i++) {
        if(m < arr[i]){
            m = arr[i];
        }
    }
    System.out.println(m);
}', NULL, NULL, NULL, NULL, 'EASY', '数组', @b_bank_id, @subject_id, 'FILL_BLANK', '544', NULL, 3, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-OUTPUT-02 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
public class getSum {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i < 100; i++) {
            if (i % 2 != 0)
                sum += i;
        }
        System.out.println(sum);
    }
}', NULL, NULL, NULL, NULL, 'EASY', '循环', @b_bank_id, @subject_id, 'FILL_BLANK', '2500', NULL, 3, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-OUTPUT-03 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
class Test {
    private static String name;
    static {
        name = "World";
        System.out.print(name);
    }
    public static void main(String[] args) {
        System.out.print("Hello");
        Test test = new Test();
    }
}', NULL, NULL, NULL, NULL, 'EASY', '静态代码块', @b_bank_id, @subject_id, 'FILL_BLANK', 'WorldHello', NULL, 3, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-OUTPUT-04 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
class Calculator {
    int add(int a, int b) { return a + b; }
}
class ScientificCalculator extends Calculator {
    int multiply(int a, int b) { return a * b; }
}
public class TestCalc {
    public static void main(String[] args) {
        ScientificCalculator sc = new ScientificCalculator();
        System.out.println(sc.add(5, 3));
        System.out.println(sc.multiply(5, 3));
    }
}', NULL, NULL, NULL, NULL, 'EASY', '继承', @b_bank_id, @subject_id, 'FILL_BLANK', '8,15', NULL, 3, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-OUTPUT-05 阅读程序并写出运行结果：', '阅读程序并写出运行结果：
public class Example {
    public static void main(String[] args) {
        int x;
        try {
            x = div(1, 2);
            System.out.println(x);
        } catch (ArithmeticException e) {
            System.out.println(e);
        }
    }
    public static int div(int a, int b) { return a / b; }
}', NULL, NULL, NULL, NULL, 'EASY', '整数除法与异常', @b_bank_id, @subject_id, 'FILL_BLANK', '0', NULL, 3, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILLPROG-01 程序填空：定义一个抽象类 People，具有私有属性 name 和抽象方法 printInfo()；定义继承自 People 的 Teacher 类，具有私有属性 skill，重写 printInfo() ...', '程序填空：定义一个抽象类 People，具有私有属性 name 和抽象方法 printInfo()；定义继承自 People 的 Teacher 类，具有私有属性 skill，重写 printInfo() 方法；在 Test 类中测试 Teacher 对象。请填写①②③。
Public ① void printInfo();
public class Teacher ② People { ... }
public class Test { public static void main(String[] args) { ③; t.printInfo(); } }', NULL, NULL, NULL, NULL, 'EASY', '抽象类与继承', @b_bank_id, @subject_id, 'FILL_BLANK', '["abstract", "extends", "People t = new Teacher(\\"张三\\", \\"java,C\\"); 或 Teacher t = new Teacher(\\"张三\\", \\"java,C\\");"]', NULL, 10, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-FILLPROG-02 程序填空：下面是一个用 ArrayList 集合存储字符串元素的程序，请填写④⑤。', '程序填空：下面是一个用 ArrayList 集合存储字符串元素的程序，请填写④⑤。
for (int i = 0; i < ④; i++) { System.out.println(list.get(i)); }
for (⑤ : list){ System.out.println(s); }', NULL, NULL, NULL, NULL, 'EASY', 'ArrayList遍历', @b_bank_id, @subject_id, 'FILL_BLANK', '["list.size()", "String s"]', NULL, 10, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-PROG-01 请编写程序，实现获取数组 {22,24,76,12,21,33} 的最大数。请使用 Java 编写，主类名为 Main。', '请编写程序，实现获取数组 {22,24,76,12,21,33} 的最大数。请使用 Java 编写，主类名为 Main。', NULL, '请按题目要求输出。', '', 'max=76', 'MEDIUM', '数组与方法', @b_bank_id, @subject_id, 'PROGRAMMING', '参考实现应定义数组并遍历求最大值，输出 max=76。', NULL, 5, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO test_case (problem_id, input_data, expected_output, is_sample, sort_order, status) VALUES (@problem_id, '', 'max=76', 1, 0, 1);;
INSERT INTO problem (title, description, input_description, output_description, sample_input, sample_output, difficulty, knowledge_tags, bank_id, subject_id, question_type, standard_answer, scoring_points, score, creator_id, status) VALUES ('B-PROG-02 定义一个 USB 接口，并通过 Mouse 和 U盘类实现它。接口 USB 包括两个抽象方法：void work() 和 void stop()。Mouse 的 work 输出“我点点点”，stop 输出“我不能点...', '定义一个 USB 接口，并通过 Mouse 和 U盘类实现它。接口 USB 包括两个抽象方法：void work() 和 void stop()。Mouse 的 work 输出“我点点点”，stop 输出“我不能点了”；UPan 的 work 输出“我存存存”，stop 输出“我走了”。Main 中定义 USB 变量 usb1 存放鼠标对象并调用 work、stop；定义 USB 数组 usbs，第0个元素存放 UPan 对象，第1个元素存放 Mouse 对象，循环数组调用每个元素的 work 和 stop。', NULL, '请按题目要求输出。', '', '我点点点
我不能点了
我存存存
我走了
我点点点
我不能点了', 'MEDIUM', '接口与多态', @b_bank_id, @subject_id, 'PROGRAMMING', '参考实现应定义 USB 接口、Mouse 类、UPan 类和 Main 测试类，并按样例输出。', NULL, 10, @teacher_id, 1);;
SET @problem_id = LAST_INSERT_ID();;
INSERT INTO test_case (problem_id, input_data, expected_output, is_sample, sort_order, status) VALUES (@problem_id, '', '我点点点
我不能点了
我存存存
我走了
我点点点
我不能点了', 1, 0, 1);;
