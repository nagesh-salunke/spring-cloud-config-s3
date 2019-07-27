package com.spring.cloud.config.s3.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.spring.cloud.config.s3.AbstractTest;
import com.spring.cloud.config.s3.ConfigServerTestUtils;
import com.spring.cloud.config.s3.config.S3EnvironmentRepositoryProperties;
import com.spring.cloud.config.s3.exceptions.SystemException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.FileSystemUtils;

/**
 * Concurrency Tests for {@link S3EnvironmentRepository}
 *
 * @author Nagesh Salunke
 */
public class S3EnvironmentRepositoryConcurrencyTests extends AbstractTest {

  @MockBean
  private S3Repository s3Repository;

  private S3EnvironmentRepository s3EnvironmentRepository;

  private File basedir = new File("target/repos/config-repo");

  @Resource
  private StandardEnvironment standardEnvironment;

  @Resource
  private S3EnvironmentRepositoryProperties properties;

  @Before
  @Override
  public void setUp() {
    this.s3EnvironmentRepository = new S3EnvironmentRepository(standardEnvironment, properties, s3Repository);
    this.s3EnvironmentRepository.setUri("s3://anything");
    this.s3EnvironmentRepository.setBasedir(this.basedir);
    if (this.basedir.exists()) {
      FileSystemUtils.deleteRecursively(this.basedir);
    }
  }

  @Test
  public void getConfigConcurrentAccess_withValidRepo_getsHierarchicalConfig()
      throws ExecutionException, InterruptedException, SystemException {

    mockPrepareLocalRepo();

    ExecutorService threads = Executors.newFixedThreadPool(4);
    List<Future<Boolean>> tasks = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      tasks.add(threads.submit(() ->
          s3EnvironmentRepository.findOne("bar", "staging", "master"), true));
    }
    for (Future<Boolean> future : tasks) {
      future.get();
    }
    Environment environment = s3EnvironmentRepository.findOne("bar", "staging", "master");
    Assert.assertEquals(3, environment.getPropertySources().size());
    Assert.assertEquals("bar", environment.getName());
    Assert.assertEquals("staging" , environment.getProfiles()[0]);
    Assert.assertEquals("master", environment.getLabel());
  }

  @Test
  public void getConfigConcurrentRefresh_withValidRepo_noExceptions()
      throws Exception, SystemException {

    final AtomicInteger errorCount = new AtomicInteger();

    mockPrepareLocalRepo();

    // Prepare two threads to do the parallel work
    Thread client = new Thread(() -> {
      try {
        Environment environment = s3EnvironmentRepository.findOne("bar",
            "staging", "master");
      }
      catch (Exception e) {
        errorCount.incrementAndGet();
        e.printStackTrace();
      }
    });

    Thread refresh = new Thread(() -> {
      try {
        s3EnvironmentRepository.refresh();
      }
      catch (Exception e) {
        errorCount.incrementAndGet();
        e.printStackTrace();
      }
    });

    // Start the parallel actions and wait till the end.
    refresh.start();
    client.start();
    refresh.join();
    client.join();

    Assert.assertEquals(0, errorCount.get());
  }

  private void mockPrepareLocalRepo() throws SystemException {
    Mockito.doAnswer(k -> {
      Object[] args = k.getArguments();
      File destDir = (File) args[1];
      ConfigServerTestUtils.prepareLocalRepo(destDir, "config-repo");
      return null;
    }).when(s3Repository).downloadBucket(anyString(), any());
  }

}
