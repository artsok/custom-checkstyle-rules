@AllArgsConstructor
private static class SampleTestClass {

  private final static String f1 = "v1";
  private final static String f2;
  private final String f3 = "v3";
  private final String f4;
  private String f5 = "aa";
  private String f6;

  private void sampleTestClass(String f1, String f2, String f4) {
    this.f1 = f1;
    this.f2 = f2;
    this.f4 = f4;
  }
}