set basedir=e:\lanes
del /Q %basedir%\classes
mkdir %basedir%\classes
set cp=%basedir%\lib\*;
set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_05"
set PATH=%JAVA_HOME%\bin;%PATH%
dir /b /s %basedir%\src\*.java > %basedir%\build.txt
%JAVA_HOME%\bin\javac -classpath %cp% -d %basedir%\classes @%basedir%\build.txt
%systemroot%\System32\xcopy /S %basedir%\src\*.properties %basedir%\classes
jar -cf %basedir%\lanes.jar -C %basedir%\classes/ .