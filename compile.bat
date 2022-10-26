@echo off
chcp 1251
javac -d bin src/ru/beelang/utils/GenerateAst.java
javac -d bin -cp src src/ru/beelang/Main.java
pause