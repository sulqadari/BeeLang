@echo off

chcp 1251

rem set sct=%~dp0scripts\interpreter_test.bee
set sct=%~dp0scripts\scope_tests.bee

java -cp bin ru.beelang.Main %sct%
pause
