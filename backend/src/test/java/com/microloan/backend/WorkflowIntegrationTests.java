package com.microloan.backend;

import static org.junit.jupiter.api.Assertions.*;

import com.microloan.backend.entity.*;
import com.microloan.backend.repository.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkflowIntegrationTests {

  @LocalServerPort
  int port;

  @Autowired
  ObjectMapper mapper;

  @Autowired
  UserRepository users;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  KycDocumentRepository documents;

  static final Path UPLOADS;

  static {
    try {
      UPLOADS = Files.createTempDirectory("microloan-workflow-");
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("app.upload.dir", () -> UPLOADS.toString());
    String pg = System.getenv("MICROLOAN_TEST_PG_URL");
    if (pg == null) r.add(
      "spring.datasource.url",
      () ->
        "jdbc:h2:mem:workflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=15000"
    );
    else {
      if (
        !pg.matches(
          "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/microloan_checkpoint_test"
        )
      ) throw new IllegalArgumentException(
        "Only an isolated local checkpoint database is allowed"
      );
      r.add("spring.datasource.url", () -> pg);
      r.add(
        "spring.datasource.driver-class-name",
        () -> "org.postgresql.Driver"
      );
      r.add("spring.datasource.username", () -> "checkpoint_user");
      r.add("spring.datasource.password", () ->
        System.getenv("MICROLOAN_TEST_PG_PASSWORD")
      );
    }
  }

  final HttpClient client = HttpClient.newHttpClient();
  final String password = "Fictional-test-password-42";

  record Reply(int status, Map<String, Object> body) {}

  Reply call(String method, String path, String token, Object body)
    throws Exception {
    var b = HttpRequest.newBuilder(
      URI.create("http://localhost:" + port + path)
    ).header("Content-Type", "application/json");
    if (token != null) b.header("Authorization", "Bearer " + token);
    var response = client.send(
      b
        .method(
          method,
          body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(
                mapper.writeValueAsString(body)
              )
        )
        .build(),
      HttpResponse.BodyHandlers.ofString()
    );
    Map<String, Object> result = new HashMap<>();
    if (response.body().startsWith("{")) result = mapper.readValue(
      response.body(),
      new TypeReference<Map<String, Object>>() {}
    );
    return new Reply(response.statusCode(), result);
  }

  Map<String, Object> register() throws Exception {
    var r = call(
      "POST",
      "/api/auth/register",
      null,
      Map.of(
        "fullName",
        "Ananya Rao",
        "email",
        UUID.randomUUID() + "@example.test",
        "password",
        password
      )
    );
    assertEquals(201, r.status());
    return r.body();
  }

  String officer() throws Exception {
    String email = UUID.randomUUID() + "@officer.example.test";
    User u = new User();
    u.setEmail(email);
    u.setFullName("Demo officer");
    u.setPasswordHash(encoder.encode(password));
    u.setRole(Role.LOAN_OFFICER);
    users.saveAndFlush(u);
    return (String) call(
      "POST",
      "/api/auth/login",
      null,
      Map.of("email", email, "password", password)
    )
      .body()
      .get("token");
  }

  Map<String, Object> profile() {
    return Map.of(
      "phone",
      "9876501234",
      "dateOfBirth",
      "1995-03-12",
      "address",
      "Fictional Demo Street 1",
      "city",
      "Bengaluru",
      "state",
      "Karnataka",
      "postalCode",
      "560001",
      "occupation",
      "Tailor",
      "employmentType",
      "SALARIED",
      "monthlyIncome",
      45000
    );
  }

  Map<String, Object> loan() {
    return Map.of(
      "loanAmount",
      100000,
      "purpose",
      "BUSINESS",
      "purposeDescription",
      "Purchase equipment for tailoring business",
      "tenureMonths",
      24,
      "existingMonthlyDebt",
      3000
    );
  }

  Reply upload(String token, String type, String mime, byte[] bytes)
    throws Exception {
    String boundary = "MicroloanTestBoundary";
    String head =
      "--" +
      boundary +
      "\r\nContent-Disposition: form-data; name=\"documentType\"\r\n\r\n" +
      type +
      "\r\n--" +
      boundary +
      "\r\nContent-Disposition: form-data; name=\"documentNumber\"\r\n\r\nDEMOP1234A\r\n--" +
      boundary +
      "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"demo.pdf\"\r\nContent-Type: " +
      mime +
      "\r\n\r\n";
    var body = new java.io.ByteArrayOutputStream();
    body.write(head.getBytes(StandardCharsets.UTF_8));
    body.write(bytes);
    body.write(
      ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8)
    );
    var req = HttpRequest.newBuilder(
      URI.create("http://localhost:" + port + "/api/borrower/kyc")
    )
      .header("Authorization", "Bearer " + token)
      .header("Content-Type", "multipart/form-data; boundary=" + boundary)
      .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
      .build();
    var res = client.send(req, HttpResponse.BodyHandlers.ofString());
    return new Reply(res.statusCode(), mapper.readValue(res.body(), new TypeReference<Map<String, Object>>() {}));
  }

  byte[] pdf() {
    return "%PDF-1.4\nNOT A REAL ID - FICTIONAL DEMO ONLY".getBytes(
      StandardCharsets.UTF_8
    );
  }

  record Setup(String token, long kyc, long loan) {}

  Setup setup() throws Exception {
    String t = (String) register().get("token");
    assertEquals(
      200,
      call("PUT", "/api/borrower/profile", t, profile()).status()
    );
    var k = upload(t, "PAN", "application/pdf", pdf());
    assertEquals(201, k.status());
    var a = call("POST", "/api/borrower/loans", t, loan());
    assertEquals(201, a.status());
    return new Setup(
      t,
      ((Number) k.body().get("id")).longValue(),
      ((Number) a.body().get("id")).longValue()
    );
  }

  @Test
  void registrationLoginAndDuplicate() throws Exception {
    var u = register();
    assertEquals("BORROWER", u.get("role"));
    assertNotNull(u.get("token"));
    assertEquals(
      409,
      call(
        "POST",
        "/api/auth/register",
        null,
        Map.of(
          "fullName",
          "Demo",
          "email",
          u.get("email"),
          "password",
          password
        )
      ).status()
    );
    assertEquals(
      200,
      call(
        "POST",
        "/api/auth/login",
        null,
        Map.of("email", u.get("email"), "password", password)
      ).status()
    );
    assertEquals(
      401,
      call(
        "POST",
        "/api/auth/login",
        null,
        Map.of("email", u.get("email"), "password", "wrong")
      ).status()
    );
    var user = users
      .findByEmailIgnoreCase((String) u.get("email"))
      .orElseThrow();
    user.setEnabled(false);
    users.saveAndFlush(user);
    assertEquals(
      401,
      call(
        "POST",
        "/api/auth/login",
        null,
        Map.of("email", u.get("email"), "password", password)
      ).status()
    );
    assertEquals(
      401,
      call(
        "GET",
        "/api/borrower/profile",
        (String) u.get("token"),
        null
      ).status()
    );
    assertEquals(401, call("POST", "/api/auth/login", null,
      Map.of("email", u.get("email"), "password", "x".repeat(100))).status());

  }

  @Test
  void jwtAndRoleProtection() throws Exception {
    assertEquals(401, call("GET", "/api/borrower/loans", null, null).status());
    assertEquals(
      401,
      call("GET", "/api/borrower/loans", "invalid", null).status()
    );
    String token = (String) register().get("token");
    assertEquals(403, call("GET", "/api/officer/loans", token, null).status());
    assertEquals(
      403,
      call("GET", "/api/borrower/profile", officer(), null).status()
    );
  }

  @Test
  void profileCreateUpdateAndValidation() throws Exception {
    String t = (String) register().get("token");
    assertEquals(404, call("GET", "/api/borrower/profile", t, null).status());
    assertEquals(
      200,
      call("PUT", "/api/borrower/profile", t, profile()).status()
    );
    var changed = new HashMap<>(profile());
    changed.put("city", "Mysuru");
    assertEquals(
      "Mysuru",
      call("PUT", "/api/borrower/profile", t, changed).body().get("city")
    );
    assertEquals(
      "Mysuru",
      call("GET", "/api/borrower/profile", t, null).body().get("city")
    );
    changed.put("phone", "bad");
    assertEquals(
      400,
      call("PUT", "/api/borrower/profile", t, changed).status()
    );
  }

  @Test
  void uploadValidationReplacementAndKycTransitions() throws Exception {
    String t = (String) register().get("token"),
      o = officer();
    assertEquals(
      415,
      upload(t, "PAN", "application/pdf", "fake".getBytes()).status()
    );
    assertEquals(415, upload(t, "PAN", "text/plain", pdf()).status());
    assertEquals(400, upload(t, "INVALID", "application/pdf", pdf()).status());
    assertEquals(
      400,
      upload(t, "PAN", "application/pdf", new byte[0]).status()
    );
    byte[] huge = new byte[5 * 1024 * 1024 + 1];
    System.arraycopy(pdf(), 0, huge, 0, pdf().length);
    assertEquals(413, upload(t, "PAN", "application/pdf", huge).status());
    var first = upload(t, "PAN", "application/pdf", pdf());
    assertEquals(201, first.status());
    long id = ((Number) first.body().get("id")).longValue();
    assertEquals("******234A", first.body().get("maskedDocumentNumber"));
    assertFalse(first.body().containsKey("storedFileName"));
    assertEquals(
      200,
      call(
        "PATCH",
        "/api/officer/kyc/" + id + "/reject",
        o,
        Map.of("reason", "Fictional demo rejection")
      ).status()
    );
    var replacement = upload(t, "PAN", "application/pdf", pdf());
    assertEquals(201, replacement.status());
    assertEquals("PENDING", replacement.body().get("status"));
    assertNull(replacement.body().get("rejectionReason"));
    assertNotEquals(
      first.body().get("uploadedAt"),
      replacement.body().get("uploadedAt")
    );
    assertEquals(
      200,
      call("PATCH", "/api/officer/kyc/" + id + "/verify", o, null).status()
    );
    assertEquals(
      409,
      call("PATCH", "/api/officer/kyc/" + id + "/verify", o, null).status()
    );
    assertEquals(409, upload(t, "PAN", "application/pdf", pdf()).status());
  }

  @Test
  void secureFilesAndLoanOwnership() throws Exception {
    var s = setup();
    String stranger = (String) register().get("token");
    assertEquals(
      404,
      call("GET", "/api/borrower/loans/" + s.loan(), stranger, null).status()
    );
    assertEquals(
      404,
      call(
        "GET",
        "/api/borrower/loans/" + s.loan() + "/details",
        stranger,
        null
      ).status()
    );
    assertEquals(
      404,
      call(
        "PATCH",
        "/api/borrower/loans/" + s.loan() + "/cancel",
        stranger,
        null
      ).status()
    );
    assertEquals(
      404,
      call(
        "GET",
        "/api/borrower/kyc/" + s.kyc() + "/file",
        stranger,
        null
      ).status()
    );
    var response = client.send(
      HttpRequest.newBuilder(
        URI.create(
          "http://localhost:" + port + "/api/borrower/kyc/" + s.kyc() + "/file"
        )
      )
        .header("Authorization", "Bearer " + s.token())
        .build(),
      HttpResponse.BodyHandlers.ofByteArray()
    );
    assertEquals(200, response.statusCode());
    assertEquals(
      "application/pdf",
      response.headers().firstValue("Content-Type").orElseThrow()
    );
    assertArrayEquals(pdf(), response.body());
  }

  @Test
  void loanValidationDuplicateAndCancellation() throws Exception {
    String t = (String) register().get("token");
    assertEquals(400, call("POST", "/api/borrower/loans", t, loan()).status());
    call("PUT", "/api/borrower/profile", t, profile());
    assertEquals(400, call("POST", "/api/borrower/loans", t, loan()).status());
    upload(t, "PAN", "application/pdf", pdf());
    for (var pair : List.of(
      Map.entry("loanAmount", 999),
      Map.entry("loanAmount", 500001),
      Map.entry("tenureMonths", 2),
      Map.entry("tenureMonths", 61),
      Map.entry("existingMonthlyDebt", -1),
      Map.entry("loanAmount", 1000.001)
    )) {
      var bad = new HashMap<>(loan());
      bad.put(pair.getKey(), pair.getValue());
      assertEquals(400, call("POST", "/api/borrower/loans", t, bad).status());
    }
    var other = new HashMap<>(loan());
    other.put("purpose", "OTHER");
    other.put("purposeDescription", "");
    assertEquals(400, call("POST", "/api/borrower/loans", t, other).status());
    var a = call("POST", "/api/borrower/loans", t, loan());
    assertEquals(201, a.status());
    long id = ((Number) a.body().get("id")).longValue();
    assertEquals(409, call("POST", "/api/borrower/loans", t, loan()).status());
    assertEquals(
      "CANCELLED",
      call("PATCH", "/api/borrower/loans/" + id + "/cancel", t, null)
        .body()
        .get("status")
    );
    assertEquals(
      409,
      call("PATCH", "/api/borrower/loans/" + id + "/cancel", t, null).status()
    );
  }

  @Test
  void completeApprovalWorkflowAndFinalImmutability() throws Exception {
    var s = setup();
    String o = officer(),
      base = "/api/officer/loans/" + s.loan();
    assertNotNull(
      call("GET", "/api/borrower/loans/" + s.loan() + "/risk", s.token(), null)
        .body()
        .get("score")
    );
    var approve = Map.of(
      "decision",
      "APPROVED",
      "reason",
      "Verified income and suitable affordability",
      "approvedAmount",
      100000,
      "annualInterestRate",
      12
    );
    assertEquals(409, call("POST", base + "/decision", o, approve).status());
    assertEquals(200, call("PATCH", base + "/review", o, null).status());
    assertEquals(409, call("POST", base + "/decision", o, approve).status());
    assertEquals(
      200,
      call("PATCH", "/api/officer/kyc/" + s.kyc() + "/verify", o, null).status()
    );
    assertEquals(
      "APPROVE",
      call("POST", base + "/risk", o, null)
        .body()
        .get("recommendation")
    );
    var tooMuch = new HashMap<>(approve);
    tooMuch.put("approvedAmount", 100001);
    assertEquals(400, call("POST", base + "/decision", o, tooMuch).status());
    assertEquals(200, call("POST", base + "/decision", o, approve).status());
    assertEquals(
      "APPROVED",
      call("GET", "/api/borrower/loans/" + s.loan(), s.token(), null)
        .body()
        .get("status")
    );
    assertEquals(
      "APPROVED",
      call(
        "GET",
        "/api/borrower/loans/" + s.loan() + "/decision",
        s.token(),
        null
      )
        .body()
        .get("decision")
    );
    assertEquals(409, call("POST", base + "/decision", o, approve).status());
    assertEquals(
      409,
      call(
        "PATCH",
        "/api/borrower/loans/" + s.loan() + "/cancel",
        s.token(),
        null
      ).status()
    );
    assertEquals(409, call("POST", base + "/risk", o, null).status());
  }

  @Test
  void rejectionRequiresReasonAndIsFinal() throws Exception {
    var s = setup();
    String o = officer(),
      base = "/api/officer/loans/" + s.loan();
    call("PATCH", base + "/review", o, null);
    assertEquals(
      400,
      call(
        "POST",
        base + "/decision",
        o,
        Map.of("decision", "REJECTED", "reason", " ")
      ).status()
    );
    assertEquals(
      200,
      call(
        "POST",
        base + "/decision",
        o,
        Map.of(
          "decision",
          "REJECTED",
          "reason",
          "Unable to verify declared information"
        )
      ).status()
    );
    assertEquals(
      "REJECTED",
      call("GET", "/api/borrower/loans/" + s.loan(), s.token(), null)
        .body()
        .get("status")
    );
  }

  @Test
  void concurrentSubmissionsAllowOnlyOneActiveLoan() throws Exception {
    String t = (String) register().get("token");
    call("PUT", "/api/borrower/profile", t, profile());
    upload(t, "PAN", "application/pdf", pdf());
    try (var pool = Executors.newFixedThreadPool(2)) {
      CountDownLatch go = new CountDownLatch(1);
      Callable<Integer> task = () -> {
        go.await();
        return call("POST", "/api/borrower/loans", t, loan()).status();
      };
      var a = pool.submit(task);
      var b = pool.submit(task);
      go.countDown();
      var statuses = new ArrayList<>(
        List.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS))
      );
      Collections.sort(statuses);
      assertEquals(List.of(201, 409), statuses);
    }
  }

  @Test
  void swaggerAndErrorsDoNotExposeInternals() throws Exception {
    assertEquals(200, call("GET", "/v3/api-docs", null, null).status());
    var r = call("POST", "/api/auth/register", null, Map.of());
    assertEquals(400, r.status());
    assertTrue(r.body().containsKey("validationErrors"));
    assertFalse(r.body().containsKey("trace"));
  }

  @Autowired
  com.microloan.backend.service.KycService kycService;

  @Autowired
  org.springframework.transaction.PlatformTransactionManager transactionManager;

  @Test
  void replacementRollbackRetainsOldFileAndRemovesNewFile() throws Exception {
    String token = (String) register().get("token");
    var r = upload(token, "PAN", "application/pdf", pdf());
    long id = ((Number) r.body().get("id")).longValue();
    var old = documents.findById(id).orElseThrow();
    String oldName = old.getStoredFileName();
    String email = users
      .findById(((Number) r.body().get("borrowerId")).longValue())
      .orElseThrow()
      .getEmail();
    long before;
    try (var files = Files.list(UPLOADS)) {
      before = files.count();
    }
    new org.springframework.transaction.support.TransactionTemplate(
      transactionManager
    ).executeWithoutResult(status -> {
      kycService.uploadDocument(
        email,
        DocumentType.PAN,
        "DEMOX5678B",
        new org.springframework.mock.web.MockMultipartFile(
          "file",
          "replacement.pdf",
          "application/pdf",
          pdf()
        )
      );
      status.setRollbackOnly();
    });
    assertEquals(
      oldName,
      documents.findById(id).orElseThrow().getStoredFileName()
    );
    assertTrue(Files.isRegularFile(UPLOADS.resolve(oldName)));
    try (var files = Files.list(UPLOADS)) {
      assertEquals(before, files.count());
    }
  }

  @Test
  void expiredTokensAndKycRejectionValidation() throws Exception {
    var u = register();
    String token = (String) u.get("token");
    String expired = com.auth0.jwt.JWT.create()
      .withSubject((String) u.get("email"))
      .withExpiresAt(java.time.Instant.now().minusSeconds(10))
      .sign(
        com.auth0.jwt.algorithms.Algorithm.HMAC256(
          "test-only-isolated-signing-key-not-for-deployment-1234567890"
        )
      );
    assertEquals(
      401,
      call("GET", "/api/borrower/profile", expired, null).status()
    );
    long id = (
      (Number) upload(token, "PAN", "application/pdf", pdf()).body().get("id")
    ).longValue();
    assertEquals(
      400,
      call(
        "PATCH",
        "/api/officer/kyc/" + id + "/reject",
        officer(),
        Map.of("reason", " ")
      ).status()
    );
  }

  @Test
  void devBootstrapCreatesOfficerAndNeverPromotesExistingAccount()
    throws Exception {
    var existing = register();
    String email = (String) existing.get("email");
    var environment = new org.springframework.mock.env.MockEnvironment()
      .withProperty("APP_OFFICER_NAME", "Demo officer")
      .withProperty("APP_OFFICER_EMAIL", email)
      .withProperty("APP_OFFICER_PASSWORD", password);
    new com.microloan.backend.config.OfficerBootstrap(
      users,
      encoder,
      environment
    ).run();
    assertEquals(
      Role.BORROWER,
      users.findByEmailIgnoreCase(email).orElseThrow().getRole()
    );
    String fresh = UUID.randomUUID() + "@bootstrap.example.test";
    environment.withProperty("APP_OFFICER_EMAIL", fresh);
    new com.microloan.backend.config.OfficerBootstrap(
      users,
      encoder,
      environment
    ).run();
    var officer = users.findByEmailIgnoreCase(fresh).orElseThrow();
    assertEquals(Role.LOAN_OFFICER, officer.getRole());
    assertTrue(encoder.matches(password, officer.getPasswordHash()));
  }
}
