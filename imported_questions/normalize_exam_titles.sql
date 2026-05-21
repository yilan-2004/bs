-- Normalize imported OO Java exam problem titles for display.
-- Rebuilds titles as 01. <description> in each paper bank. Long titles are shortened for problem.title varchar(128).

-- bank_id=1106
UPDATE problem SET title = '01. Java属于那种语言？（ ）' WHERE id = 2503;
UPDATE problem SET title = '02. 下面4种类型的文件中（ ）可以在Java虚拟机中运行。' WHERE id = 2504;
UPDATE problem SET title = '03. 下面哪个选项是Java的有效标识符（ ）' WHERE id = 2505;
UPDATE problem SET title = '04. 下列选项中，不属于基本数据类型的是（ ）' WHERE id = 2506;
UPDATE problem SET title = '05. 下列类的定义中不正确的是（ ）' WHERE id = 2507;
UPDATE problem SET title = '06. 对于声明为private、protected以及public的类成员在类外部（ ）' WHERE id = 2508;
UPDATE problem SET title = '07. Java中的哪个关键字用于实现接口（ ）' WHERE id = 2509;
UPDATE problem SET title = '08. String s="itcast"，则s.substring(3,4)返回的字符串是以下选项中的哪个（ ）' WHERE id = 2510;
UPDATE problem SET title = '09. 使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（ ）' WHERE id = 2511;
UPDATE problem SET title = '10. 下列选项中，哪个类是用来读取文本的字节流（ ）' WHERE id = 2512;
UPDATE problem SET title = '11. 下列哪些是Java中的基本数据类型？' WHERE id = 2513;
UPDATE problem SET title = '12. 面向对象的三个特性是？' WHERE id = 2514;
UPDATE problem SET title = '13. 有A、B、C三个类，以下继承关系正确的是？' WHERE id = 2515;
UPDATE problem SET title = '14. 在Java中，以下关于接口的描述正确的是？' WHERE id = 2516;
UPDATE problem SET title = '15. 以下哪些集合可以保存具有映射关系的数据？' WHERE id = 2517;
UPDATE problem SET title = '16. 将.Java源文件编译为.class文件的是__________命令。' WHERE id = 2518;
UPDATE problem SET title = '17. Java程序代码必须放在一个类中，类使用__________关键词定义。' WHERE id = 2519;
UPDATE problem SET title = '18. 假设 int x=2，三元表达式 x>0?x+1:5 的结果为__________。' WHERE id = 2520;
UPDATE problem SET title = '19. 一个类如果要实现一个接口，可以通过关键字________来实现这个接口。' WHERE id = 2521;
UPDATE problem SET title = '20. Java中的I/O流，按照操作文件类型的不同，可分为字节流和__________。' WHERE id = 2522;
UPDATE problem SET title = '21. 若 x=5，则表达式 (x+5)/3 的值是3。' WHERE id = 2523;
UPDATE problem SET title = '22. 在Java中，方法的重载和重写都是实现多态性的手段。' WHERE id = 2524;
UPDATE problem SET title = '23. 抽象类不一定有抽象方法，有抽象方法的类不一定是抽象类。' WHERE id = 2525;
UPDATE problem SET title = '24. 在try…catch语句中，try语句块存放可能发生异常的语句。' WHERE id = 2526;
UPDATE problem SET title = '25. Set集合是通过键值对的方式来存储对象的。' WHERE id = 2527;
UPDATE problem SET title = '26. 阅读程序并写出运行结果： public static void main(String[] args) { int a[]={10,20,30,40,50,60,70,80,90}; int s=0; for(int i=0;i<a...' WHERE id = 2528;
UPDATE problem SET title = '27. 阅读程序并写出运行结果： class Test { private static String name; static { name = "World"; System.out.print(name); } public stat...' WHERE id = 2529;
UPDATE problem SET title = '28. 阅读程序，写出返回值为 true 的变量名： class Dog { public String name; Dog(String name){ this.name = name; } } public class Demo1 { ...' WHERE id = 2530;
UPDATE problem SET title = '29. 阅读程序并写出运行结果： class Demo { public static void main(String[] args){ int x = div(1,2); try { } catch(Exception e){ Syst...' WHERE id = 2531;
UPDATE problem SET title = '30. 阅读程序并写出运行结果： public static void main(String[] args) { StringBuffer str1 = new StringBuffer("abc"); str1.append("def"...' WHERE id = 2532;
UPDATE problem SET title = '31. 简述面向对象的三大特征。' WHERE id = 2533;
UPDATE problem SET title = '32. 简述 try...catch 语句的异常处理流程。' WHERE id = 2534;
UPDATE problem SET title = '33. 编写2个方法：有参方法 getMax() 用来求数组中的最大值，有参方法 getMin() 用来求数组中的最小值。在 main 函数中初始化数组 arr={4,1,6,3,9,8,-2}，调用 getMax() 打印最大值，调用 g...' WHERE id = 2535;
UPDATE problem SET title = '34. 编写程序：定义接口 Shape，包含计算面积 getArea() 方法。创建 Circle 类计算圆面积，半径 double radius；创建 Triangles 类计算三角形面积，边长 double sideA、sideB、si...' WHERE id = 2536;

-- bank_id=1107
UPDATE problem SET title = '01. 在JDK的bin目录下有许多exe可执行文件，其中java.exe命令的作用是（ ）' WHERE id = 2537;
UPDATE problem SET title = '02. 下面那种类型的文件可以在Java虚拟机中运行？（ ）' WHERE id = 2538;
UPDATE problem SET title = '03. 下列选项中，那些属于合法的标识符？（ ）' WHERE id = 2539;
UPDATE problem SET title = '04. 下列选项中，使用比较运算符正确的是（ ）' WHERE id = 2540;
UPDATE problem SET title = '05. 下列关于构造方法的描述中，错误的是（ ）' WHERE id = 2541;
UPDATE problem SET title = '06. 现有两个类A、B，以下描述中表示B继承自A的是（）' WHERE id = 2542;
UPDATE problem SET title = '07. 下列选项中，可以正确实现String初始化的是（ ）' WHERE id = 2543;
UPDATE problem SET title = '08. 使用Iterator时，判断是否存在下一个元素可以使用以下哪个方法（ ）' WHERE id = 2544;
UPDATE problem SET title = '09. 在Java中，下面哪个关键字用于捕获异常（ ）' WHERE id = 2545;
UPDATE problem SET title = '10. File类中以字符串形式返回文件绝对路径的方法是哪一项？（ ）' WHERE id = 2546;
UPDATE problem SET title = '11. Java语言的特点有哪些？' WHERE id = 2547;
UPDATE problem SET title = '12. 在Java中，下面哪些关键字用于定义循环结构？' WHERE id = 2548;
UPDATE problem SET title = '13. 面向对象的三个特性是？' WHERE id = 2549;
UPDATE problem SET title = '14. 有A、B、C三个类，以下继承关系正确的是？' WHERE id = 2550;
UPDATE problem SET title = '15. 以下属于Map接口集合常用方法的有？' WHERE id = 2551;
UPDATE problem SET title = '16. JDK中，存放可执行程序的目录是________。' WHERE id = 2552;
UPDATE problem SET title = '17. Java程序代码必须放在一个类中，类使用________关键词定义。' WHERE id = 2553;
UPDATE problem SET title = '18. 静态方法必须使用________关键字来修饰。' WHERE id = 2554;
UPDATE problem SET title = '19. 在继承关系中，子类会自动继承父类中的方法，但有时在子类中需要对继承的方法进行一些修改，即对父类的方法进行________。' WHERE id = 2555;
UPDATE problem SET title = '20. 在程序中，获取字符串长度的方法是________。' WHERE id = 2556;
UPDATE problem SET title = '21. 编译Java程序需要使用 java 命令。' WHERE id = 2557;
UPDATE problem SET title = '22. 在成员方法中出现的 this 关键字，代表的是调用这个方法的对象。' WHERE id = 2558;
UPDATE problem SET title = '23. Exception类称为异常类，它表示程序本身可以处理的错误，在开发Java程序中进行的异常处理，都是针对Exception类及其子类。' WHERE id = 2559;
UPDATE problem SET title = '24. Pattern类用于创建一个正则表达式，也可以说创建一个匹配模式，它的构造方法是私有的，不可以直接创建。' WHERE id = 2560;
UPDATE problem SET title = '25. Set集合是通过键值对的方式来存储对象的。' WHERE id = 2561;
UPDATE problem SET title = '26. 阅读程序并写出运行结果： public static void main(String[] args){ int[] arr = {23,544,56,1,23,45,21,1,78}; int m = arr[0]; for (i...' WHERE id = 2562;
UPDATE problem SET title = '27. 阅读程序并写出运行结果： public class getSum { public static void main(String[] args) { int sum = 0; for (int i = 1; i < 100; i+...' WHERE id = 2563;
UPDATE problem SET title = '28. 阅读程序并写出运行结果： class Test { private static String name; static { name = "World"; System.out.print(name); } public stat...' WHERE id = 2564;
UPDATE problem SET title = '29. 阅读程序并写出运行结果： class Calculator { int add(int a, int b) { return a + b; } } class ScientificCalculator extends Calcula...' WHERE id = 2565;
UPDATE problem SET title = '30. 阅读程序并写出运行结果： public class Example { public static void main(String[] args) { int x; try { x = div(1, 2); System.out....' WHERE id = 2566;
UPDATE problem SET title = '31. 程序填空：定义一个抽象类 People，具有私有属性 name 和抽象方法 printInfo()；定义继承自 People 的 Teacher 类，具有私有属性 skill，重写 printInfo() 方法；在 Test 类中测...' WHERE id = 2567;
UPDATE problem SET title = '32. 程序填空：下面是一个用 ArrayList 集合存储字符串元素的程序，请填写④⑤。 for (int i = 0; i < ④; i++) { System.out.println(list.get(i)); } for (⑤ : ...' WHERE id = 2568;
UPDATE problem SET title = '33. 请编写程序，实现获取数组 {22,24,76,12,21,33} 的最大数。请使用 Java 编写，主类名为 Main。' WHERE id = 2569;
UPDATE problem SET title = '34. 定义一个 USB 接口，并通过 Mouse 和 U盘类实现它。接口 USB 包括两个抽象方法：void work() 和 void stop()。Mouse 的 work 输出“我点点点”，stop 输出“我不能点了”；UPan 的...' WHERE id = 2570;

