package com.spring.cloud.config.s3;

import java.io.File;
import java.io.IOException;
import org.springframework.util.FileSystemUtils;

/**
 * Utils for Config Server Tests.
 * @author Nagesh Salunke
 */
public class ConfigServerTestUtils {

  /**
   * Default path where all test related repositories will be copied.
   */
  public static final String DEFAULT_LOCAL_REPO_PATH = "target/repos";

  /**
   * Prepares a local config repo by copying test data.
   *
   * @param repoPath - repoPath to be copied from testdata
   * @throws IOException - {@link IOException}
   */
  public static void prepareLocalRepo(File destDir, String repoPath) throws IOException {
    String baseDir ="./";
    String buildDir = baseDir + DEFAULT_LOCAL_REPO_PATH;
    new File(buildDir).mkdirs();

    if (!repoPath.startsWith("/")) {
      repoPath = "/" + repoPath;
    }
    if (!repoPath.endsWith("/")) {
      repoPath = repoPath + "/";
    }
    File source = new File(baseDir + "src/test/resources/testdata" + repoPath);
    if (destDir.exists()) {
      FileSystemUtils.deleteRecursively(destDir);
    }
    FileSystemUtils.copyRecursively(source, destDir);
  }
}
