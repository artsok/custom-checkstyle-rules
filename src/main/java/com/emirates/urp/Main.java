package com.emirates.urp;

import com.emirates.urp.util.CheckCodeStyleUtils;
import com.emirates.urp.util.DiffParser;
import com.emirates.urp.util.GitChange;
import java.io.IOException;
import java.util.List;
import org.eclipse.jgit.api.errors.GitAPIException;

public class Main {

  public static void main(String[] args) {
    try {
      String currentBranchName = CheckCodeStyleUtils.findCurrentBranchName();
      String currentRepo = CheckCodeStyleUtils.getCurrentRepo();
      System.out.println(currentBranchName);
      System.out.println(currentRepo);
      final List<GitChange> changes = DiffParser.parse(currentRepo, currentBranchName);

      System.out.println("test commit");

      changes.forEach(it -> {
        System.out.println(it.path());
        System.out.println(it.addedLines());
      });

    } catch (IOException | GitAPIException e) {
      throw new RuntimeException("222 something here");
    }
  }

}
