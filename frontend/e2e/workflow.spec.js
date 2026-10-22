import { test, expect } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";
const creds = JSON.parse(
  fs.readFileSync("../backend/target/e2e-credentials.json", "utf8"),
);
// Values belong only to an ephemeral H2 demo. Never use this runner against real borrower data.
test("borrower registration through officer approval and borrower outcome", async ({
  page,
  browser,
}) => {
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  const email = "ananya." + Date.now() + "@example.test";
  await page.goto("/register");
  await page.getByLabel("Full name", { exact: true }).fill("Ananya Rao");
  await page.getByLabel("Email address").fill(email);
  await page.getByLabel("Password", { exact: true }).fill(creds.password);
  await page.getByRole("button", { name: "Create account" }).click();
  await expect(
    page.getByRole("heading", { name: "Hello, Ananya." }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Log out" }).click();
  await expect(
    page.getByRole("heading", { name: "Welcome back" }),
  ).toBeVisible();
  await page.getByLabel("Email address").fill(email);
  await page.getByLabel("Password", { exact: true }).fill(creds.password);
  await page.getByRole("button", { name: "Sign in" }).click();
  await page.getByRole("link", { name: "My profile" }).click();
  for (const [label, value] of Object.entries({
    "Phone number": "9876501234",
    "Date of birth": "1995-03-12",
    Address: "Fictional Demo Street 1",
    City: "Bengaluru",
    State: "Karnataka",
    "Postal code": "560001",
    Occupation: "Tailor",
    "Monthly income (₹)": "45000",
  }))
    await page.getByLabel(label, { exact: true }).fill(value);
  await page.getByLabel("Employment type").selectOption("SALARIED");
  await page.getByRole("button", { name: "Save profile" }).click();
  await expect(page.getByText("Your profile has been saved.")).toBeVisible();
  const fixture = await browser.newPage({
    viewport: { width: 1000, height: 650 },
  });
  await fixture.setContent(
    fs.readFileSync("../docs/demo/identity.html", "utf8"),
  );
  fs.mkdirSync("../docs/demo/generated", { recursive: true });
  const doc = path.resolve("../docs/demo/generated/not-a-real-id.png");
  await fixture.screenshot({ path: doc });
  await fixture.close();
  await page.getByRole("link", { name: "My documents" }).click();
  await page.getByLabel("Document type").selectOption("PAN");
  await page.getByLabel("Document number", { exact: true }).fill("DEMOP1234A");
  await page.getByLabel("Document file").setInputFiles(doc);
  await page.getByRole("button", { name: "Upload document" }).click();
  await expect(
    page.getByText("Document submitted for verification."),
  ).toBeVisible();
  await expect(page.getByText("PENDING", { exact: true })).toBeVisible();
  await page
    .getByRole("link", { name: "Apply for a loan", exact: false })
    .first()
    .click();
  await page.getByLabel("Requested amount (₹)").fill("100000");
  await page.getByLabel("Tenure (months)").fill("24");
  await page.getByLabel("Existing monthly debt (₹)").fill("3000");
  await page
    .getByLabel("Tell us more about the purpose")
    .fill("Purchase equipment for tailoring business");
  await page.getByRole("button", { name: "Submit application" }).click();
  await expect(
    page.getByRole("heading", { name: "Risk assessment" }),
  ).toBeVisible();
  await expect(page.getByText("MANUAL REVIEW", { exact: true })).toBeVisible();
  const borrowerUrl = page.url();
  const id = borrowerUrl.split("/").pop();
  await page.screenshot({
    path: "../docs/demo/generated/borrower-assessment.png",
    fullPage: true,
  });
  const officerContext = await browser.newContext();
  const officer = await officerContext.newPage();
  await officer.goto("http://localhost:5173/login");
  await officer.getByLabel("Email address").fill(creds.email);
  await officer.getByLabel("Password", { exact: true }).fill(creds.password);
  await officer.getByRole("button", { name: "Sign in" }).click();
  await expect(
    officer.getByRole("heading", { name: "Hello, Demo." }),
  ).toBeVisible();
  await officer
    .getByRole("link", { name: "KYC review", exact: false })
    .first()
    .click();
  const card = officer.locator("article").filter({ hasText: email });
  await expect(card).toBeVisible();
  const popup = officer.waitForEvent("popup");
  await card.getByRole("button", { name: "View securely" }).click();
  const file = await popup;
  await file.waitForLoadState();
  expect(file.url()).toContain("blob:");
  await file.close();
  await card.getByRole("button", { name: "Verify document" }).click();
  await officer.getByRole("button", { name: "Confirm", exact: true }).click();
  await expect(officer.getByText("KYC decision recorded.")).toBeVisible();
  await officer.goto("http://localhost:5173/officer/loans/" + id);
  await officer.getByRole("button", { name: "Move to review" }).click();
  await expect(
    officer.getByRole("button", { name: "Record final decision" }),
  ).toBeVisible();
  await officer.getByRole("button", { name: "Recalculate assessment" }).click();
  await expect(officer.getByText("APPROVE", { exact: true })).toBeVisible();
  await officer
    .getByLabel("Decision reason")
    .fill("Verified demo documents and affordable obligations");
  await officer.getByLabel("Annual interest rate (%)").fill("12");
  await officer.getByRole("button", { name: "Record final decision" }).click();
  await officer.getByRole("button", { name: "Confirm", exact: true }).click();
  await expect(
    officer.getByRole("heading", { name: "Final decision" }),
  ).toBeVisible();
  await page.reload();
  await expect(
    page.getByRole("heading", { name: "Final decision" }),
  ).toBeVisible();
  await expect(
    page.getByText("Verified demo documents and affordable obligations"),
  ).toBeVisible();
  await page.screenshot({
    path: "../docs/demo/generated/borrower-approved.png",
    fullPage: true,
  });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/borrower");
  await expect(
    page.getByRole("heading", { name: "Hello, Ananya." }),
  ).toBeVisible();
  await page.screenshot({
    path: "../docs/demo/generated/mobile-dashboard.png",
    fullPage: true,
  });
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
  expect(errors).toEqual([]);
  await officerContext.close();
});
