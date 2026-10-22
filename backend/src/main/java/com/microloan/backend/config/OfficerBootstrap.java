package com.microloan.backend.config;

import com.microloan.backend.entity.*;
import com.microloan.backend.repository.UserRepository;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class OfficerBootstrap implements CommandLineRunner {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final Environment env;

  public OfficerBootstrap(UserRepository u, PasswordEncoder p, Environment e) {
    users = u;
    encoder = p;
    env = e;
  }

  @Override
  public void run(String... args) {
    String name = env.getProperty("APP_OFFICER_NAME"),
      email = env.getProperty("APP_OFFICER_EMAIL"),
      password = env.getProperty("APP_OFFICER_PASSWORD");
    if (
      name == null ||
      name.isBlank() ||
      email == null ||
      email.isBlank() ||
      password == null ||
      password.isBlank()
    ) return;
    email = email.trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmailIgnoreCase(email)) return;
    if (
      name.trim().length() > 100 ||
      email.length() > 150 ||
      !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") ||
      password.length() < 8 ||
      password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
    ) throw new IllegalArgumentException(
      "Invalid development officer configuration"
    );
    User user = new User();
    user.setFullName(name.trim());
    user.setEmail(email);
    user.setPasswordHash(encoder.encode(password));
    user.setRole(Role.LOAN_OFFICER);
    user.setEnabled(true);
    users.save(user);
  }
}
