package com.emirates.urp.util;

import static com.emirates.urp.util.CheckCodeStyleUtils.CodeStyleGitCommand.DIFF_NAMES_LOCAL;
import static com.emirates.urp.util.CheckCodeStyleUtils.CodeStyleGitCommand.DIFF_NAMES_MASTER;
import static java.lang.String.format;
import static org.apache.commons.lang3.SystemProperties.USER_DIR;

import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class CheckCodeStyleUtils {

  private static String branchName;
  private static boolean isAlreadyFetched;
  private static final List<String> DIFF_WITH_MASTER_LIST = new ArrayList<>();
  public static final Path PROJECT_ROOT = Path.of(USER_DIR);
  private static final AtomicReference<List<String>> changedFileSet = new AtomicReference<>(null);

  public static List<String> getChangedFileList(String... fileExtensions) {
    List<String> currentList = changedFileSet.get();
    if (currentList == null) {
      List<String> initializedList = initializeChangedFileSet(fileExtensions);
      if (changedFileSet.compareAndSet(null, initializedList)) {
        return initializedList;
      } else {
        return changedFileSet.get();
      }
    }
    return currentList;
  }

  private static List<String> initializeChangedFileSet(String... fileExtensions) {
    CheckCodeStyleUtils.fetchChanges();
    List<String> changedFileNameList = CheckCodeStyleUtils.runGitDiffCommandAndGetChanges(
        DIFF_NAMES_LOCAL, DIFF_NAMES_MASTER);
    return CheckCodeStyleUtils.getChangedFileList(changedFileNameList).stream()
        .filter(it -> CommonUtil.matchesFileExtension(it, fileExtensions)).map(File::getName)
        .toList();
  }

  public static List<String> runGitDiffCommandAndGetChanges(CodeStyleGitCommand localDiffCommand,
      CodeStyleGitCommand remoteDiffCommand) {

    log.info(localDiffCommand + " localDiffCommand");
    log.info(remoteDiffCommand + " remoteDiffCommand");

    if (DIFF_WITH_MASTER_LIST.isEmpty()) {
      log.info(
          format("Getting diff between `origin/master` and `%s` branches", getCurrentBranchName()));
      DIFF_WITH_MASTER_LIST.addAll(
          executeCommandAndGetResultList(new ProcessBuilder(localDiffCommand.getCommands())));

      DIFF_WITH_MASTER_LIST.addAll(
          executeCommandAndGetResultList(new ProcessBuilder(remoteDiffCommand.getCommands())));
    }
    return DIFF_WITH_MASTER_LIST;
  }

  private static List<String> executeCommandAndGetResultList(ProcessBuilder processBuilder) {
    Process process;
    StringWriter writer = new StringWriter();
    try {
      process = processBuilder.start();
      process.waitFor(20, TimeUnit.SECONDS);
      IOUtils.copy(process.getInputStream(), writer, StandardCharsets.UTF_8);
    } catch (IOException | InterruptedException e) {
      throw new Error(
          String.format("Can not execute command [%s], trace: %s", processBuilder.command(), e));
    }
    return writer.toString().isEmpty() ? List.of() : Arrays.asList(writer.toString().split("\n"));
  }

  private static synchronized Set<File> getChangedFileList(List<String> fileNameList) {
    log.info("getChangedFileSet " + fileNameList.size());

    fileNameList.forEach(log::info);

    Set<File> fileSet;
    CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(fileNameList);

    fileSet = cowList.stream()
        .filter(StringUtils::isNotEmpty)
        //.map(path -> PROJECT_ROOT + "/" + path)
        .map(File::new)
        .filter(File::exists)
        .collect(Collectors.toSet());

    log.info("\nList of " + fileSet.size() + " files for checking is:\n "
        + fileSet.stream().map(
            file -> "\t" + StringUtils.remove(file.getAbsolutePath(), PROJECT_ROOT.toString()) + "\n")
        .collect(Collectors.joining()));
    return fileSet;
  }

  public static void fetchChanges() {
    if (!isAlreadyFetched) {
      try {
        log.info("Fetching changes...");
        new ProcessBuilder("git", "fetch")
            .start()
            .waitFor(60, TimeUnit.SECONDS);
        isAlreadyFetched = true;
      } catch (InterruptedException | IOException e) {
        throw new Error("Error occurred while fetching changes from Git. "
            + "Please configure your local Git client and run the checks again.\n", e);
      }
    }
  }

  public static String getCurrentRepo() {
    String currentRepo = executeCommandAndGetResultList(
        new ProcessBuilder("git", "rev-parse", "--show-toplevel")).get(0).trim();
    System.out.println("sadasfsdafdsf " + currentRepo);
    return currentRepo + "/";
  }

  public static String findCurrentBranchName() {
    return executeCommandAndGetResultList(
        new ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")).get(0).trim();
  }

  private static String getCurrentBranchName() {
    if (branchName == null) {
      log.info("getting branch name...");
      branchName = findCurrentBranchName();
      log.info(branchName);
    }
    return branchName;
  }

  @SuppressWarnings("Duplicates")
  public static String replaceEscapeCharacters(String string) {
    String tmp = string.replace("|", "||");
    tmp = tmp.replace("'", "|'");
    tmp = tmp.replace("\"", "|'");
    tmp = tmp.replaceAll("\\n", "|n");
    tmp = tmp.replaceAll("\\r", "|r");
    tmp = tmp.replace("[", "|[");
    tmp = tmp.replace("]", "|]");
    return tmp;
  }

  public static List<String> getFileInStringList(String filePath) {
    File codeStyleReportFile = new File(filePath);
    List<String> fileInList;
    try {
      fileInList = FileUtils.readLines(codeStyleReportFile, Charset.defaultCharset());
    } catch (IOException e) {
      throw new Error(format("Can't operate with file %s", filePath), e);
    }
    return fileInList;
  }

  public enum CodeStyleGitCommand {
    DIFF_NAMES_LOCAL(List.of("git", "diff", "--name-only", "HEAD")),

    //This could be different depend on company
    DIFF_NAMES_MASTER(List.of("git", "diff", "--name-only", "origin/main..."));

    private List<String> commands;

    CodeStyleGitCommand(List<String> commands) {
      this.commands = commands;
    }

    public List<String> getCommands() {
      return commands;
    }

    @Override
    public String toString() {
      return commands.toString();
    }
  }
}