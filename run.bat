@echo off

chcp 1251
java -cp bin ru.beelang.utils.GenerateAst C:/Develop/BeeLang/src/ru/beelang
pause



rem just install applet
rem TERMINAL CARD ATR PROFILE LOAD_CAP I_LOAD LOAD I_INSTALL


rem delete previous and install new one
rem TERMINAL CARD ATR PROFILE DELETE RESET ATR PROFILE LOAD_CAP I_LOAD LOAD I_INSTALL

rem send short message
rem RESET ATR PROFILE SET_TAR SMS
