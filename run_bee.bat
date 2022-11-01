@echo off

chcp 1251
set scrpt_path=%~dp0scripts\
set sct=%scrpt_path%print_test.bee
set sct=%scrpt_path%native_clock_test.bee;%sct%
set sct=%~dp0scripts\equality_test.bee;%sct%
set sct=%~dp0scripts\environments_test.bee;%sct%
set sct=%~dp0scripts\parentheses_test.bee;%sct%
REM set sct=%~dp0scripts\increm_test.bee;%sct%

java -cp bin ru.beelang.Main %sct%
pause
