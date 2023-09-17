# urp-checkstyle-rules

A set of additional checks to use with [checkstyle](https://checkstyle.sourceforge.io/index.html).

## Content

* [Content](#Content)
* [Checks](#Checks)
    - [MissingJavaDocMethodUrpCheck](#MissingJavaDocMethodUrpCheck)
    - [IntermediateEmptyLinesCheck](#IntermediateEmptyLinesCheck)
    - [EmptyLineAtTheEndChecker](#EmptyLineAtTheEndChecker)
    - [AnnotationsWithoutEmptyLinesChecker](#AnnotationsWithoutEmptyLinesChecker)
* [Configuration](#Configuration)
    - [Maven dependency](#Maven-dependency)
    - [Example checkstyle configuration](#Example-checkstyle-configuration)
    - [Example checkstyle maven plugin configuration](#Example-checkstyle-maven-plugin-configuration)
    - [Example checkstyle gradle plugin configuration](#Example-checkstyle-gradle-plugin-configuration)
* [Why external lib](#Why-external-lib)

## Checks

### JavaDocMethodSizeCheck

Verify the JavaDoc at methods with configurable minimal amount of lines. Additionally, you
could use a regular expression feature for class names, allowing you to ignore classes
that don't match your specified pattern. Furthermore, this check is only applied to git changes
made in your branch (Git should be installed).

#### Configuration

```xml

<module name="MissingJavaDocMethodUrpCheck">
  <property name="ignoreClassNamesRegex" value="WebClientConfig"/>
  <property name="accessModifiers" value="public,protected,package,private"/>
  <property name="fileExtensions" value="java"/>
  <property name="minLineCount" value="1"/>
</module>
```

##### Parameters

| parameter name        | type | default value | description |
|-----------------------|------|---------------|-------------|
| ignoreClassNamesRegex | int  | 12            | TODO        |
| minLineCount          | int  | 7             | TODO        |
| accessModifiers       | int  | 7             | TODO        |

## Configuration

### Maven dependency

```xml

<dependency>
  <groupId>com.emirates.urp</groupId>
  <artifactId>urp-checkstyle-rules</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Example checkstyle configuration

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
  "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
  <module name="TreeWalker">
    <module name="FieldsCountCheck"/>
    <module name="MethodEmptyLines"/>
    <module name="MethodParameterAlignment"/>
    <module name="MethodParameterLines"/>
    <module name="MethodCallParameterAlignment"/>
    <module name="MethodCallParameterLines">
      <property name="ignoreMethods" value="Map.of"/>
    </module>
  </module>
</module>
```

### Example checkstyle maven plugin configuration

To use check from this library with `maven-checkstyle-plugin`,
you have to add the library as a maven dependency to the plugin.

```xml

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.2.0</version>
  <configuration>
    <configLocation>src/main/resources/checkstyle.xml</configLocation>
    <encoding>UTF-8</encoding>
    <consoleOutput>true</consoleOutput>
    <failsOnError>true</failsOnError>
    <linkXRef>false</linkXRef>
  </configuration>
  <executions>
    <execution>
      <id>validate</id>
      <phase>validate</phase>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
  <dependencies>
    <dependency>
      <groupId>com.emirates.urp</groupId>
      <artifactId>urp-checkstyle-rules</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
</plugin>
```

## Why external lib

The checkstyle is a powerful library that has many users, but I need some combo of existing rules.