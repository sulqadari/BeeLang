@echo off

chcp 1251
set src=%~dp0scripts\

set input=%src%print_test.bee;
set input=%src%native_clock_test.bee;%input%
rem set input=%src%equality_test.bee;%input%
rem set input=%src%environments_test.bee;%input%
rem set input=%src%parentheses_test.bee;%input%
rem set input=%src%functions_call_test.bee;%input%

java -cp bin ru.beelang.Main %input%
pause
