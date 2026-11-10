@rem Atelier Contacts Gradle wrapper
@echo off
set DIR=%~dp0
if not defined JAVA_HOME goto findJavaFromPath
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
goto execute
:findJavaFromPath
set JAVA_EXE=java.exe
:execute
"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*