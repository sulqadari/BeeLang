@echo off

chcp 1251

set sct=%~dp0scripts\interpreter_test.bee
@echo on
java -cp bin ru.beelang.Main %sct%
pause
