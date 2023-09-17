@AllArgsConstructor
private static class SampleTestClass {

  private final static String f1 = "v1";
  private final static String f2;
  private final String f3 = "v3";
  private final String f4;
  private String f5 = "aa";
  private String f6;
  private String f7;
  private String f8;
  private String f9;
  private String f10;
  private String f11;
  private String f12;
  private String f13;

  public void nothing() {
    int a = 5;
  }

  public void nothing1() {

  }

  class A {

    private void nothing() {
      int a = 5;
      int b = 10;
    }

    interface C {

      default int call() {
        return 5;
      }
    }
  }
}