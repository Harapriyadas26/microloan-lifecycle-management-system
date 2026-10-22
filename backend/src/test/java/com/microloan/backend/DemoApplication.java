package com.microloan.backend;

import java.util.Map;
import org.springframework.boot.SpringApplication;

/** Isolated browser-demo launcher: available on the test classpath only. */
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(BackendApplication.class);
    app.setAdditionalProfiles("test", "dev");
    app.setDefaultProperties(
      Map.of("spring.devtools.restart.enabled", "false")
    );
    app.run(args);
  }
}
