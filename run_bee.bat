@echo off

chcp 1251

rem set sct=%~dp0scripts\interpreter_test.bee
rem set sct=%~dp0scripts\scope_tests.bee
rem set sct=%~dp0scripts\for_loop_test.bee
rem set sct=%~dp0scripts\native_func_test.bee
rem set sct=%~dp0scripts\funcs_test.bee
set sct=%~dp0scripts\funcs_performance_test.bee

java -cp bin ru.beelang.Main %sct%
pause
