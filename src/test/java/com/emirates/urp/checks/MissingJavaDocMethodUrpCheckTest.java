package com.emirates.urp.checks;

import com.emirates.urp.fixture.TestCheckstyle;
import org.junit.jupiter.api.Test;


class MissingJavaDocMethodUrpCheckTest {

  @Test
  void shouldReportViolations() {
    TestCheckstyle checkstyle = new TestCheckstyle(MissingJavaDocMethodUrpCheck.class, c -> {
      c.addProperty("fileExtensions", "java");
      c.addProperty("changedFileSet", "SampleTestClass.java");
      c.addProperty("enabledGit", "false");
      c.addProperty("minLineCount", "1");
    });

    checkstyle.check("MissingJavaDocMethodUrpCheck/SampleTestClass.java");
    checkstyle.assertViolationCount(1);
    checkstyle.assertViolation(11, 3,
        "You are missing a JavaDoc comment in a project. Please provide information about the purpose and functionality of the method. The more detailed the information you provide, the more helpful it will be for your colleagues.");
  }

  @Test
  void shouldReportViolationsWhenWeHaveSubClasses() {
    TestCheckstyle checkstyle = new TestCheckstyle(MissingJavaDocMethodUrpCheck.class, c -> {
      c.addProperty("fileExtensions", "java");
      c.addProperty("changedFileSet", "SubMethodsTestClass.java");
      c.addProperty("enabledGit", "false");
      c.addProperty("minLineCount", "0");
    });

    checkstyle.check("MissingJavadocMethodUrpCheck/SubMethodsTestClass.java");
    checkstyle.assertViolationCount(4);
  }
}
