@echo off

chcp 1251

rem set sct=%~dp0scripts\print_test.bee
rem set sct=%~dp0scripts\native_clock_test.bee
rem set sct=%~dp0scripts\equality_test.bee
set sct=%~dp0scripts\environments_test.bee
rem set sct=%~dp0scripts\parentheses_test.bee
rem set sct=%~dp0scripts\increm_test.bee

java -cp bin ru.beelang.Main %sct%
pause
