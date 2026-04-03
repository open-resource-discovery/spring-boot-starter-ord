package org.openresourcediscovery.core.services.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.core.services.TLSAuthenticator.TrustedCertificate;
import org.springframework.mock.web.MockHttpServletRequest;

class KymaTLSAuthenticatorTest {

  private static final String ISSUER_DN = "CN=issuer,O=ACME,C=US";
  private static final String SUBJECT_DN = "CN=client,O=ACME,C=US";

  private static final String ISSUER_DN_B64 = b64(ISSUER_DN);
  private static final String SUBJECT_DN_B64 = b64(SUBJECT_DN);

  private static final TrustedCertificate TRUSTED_CERT = new TrustedCertificate(ISSUER_DN, SUBJECT_DN);
  private static final TrustedCertificate WILDCARD_CERT = new TrustedCertificate("*", "*");

  private KymaTLSAuthenticator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(TRUSTED_CERT));
  }

  // ── missing headers ─────────────────────────────────────────────────────────

  @Test
  void givenMissingIssuerHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenMissingSubjectHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-CN");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  // ── issuer matching ─────────────────────────────────────────────────────────

  @Test
  void givenUntrustedIssuer_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer");
    request.addHeader("X-SSL-Client-Issuer", b64("CN=unknown,O=Other,C=DE"));

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWildcardIssuerAndMatchingSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(new TrustedCertificate("*", SUBJECT_DN)));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  // ── subject matching ────────────────────────────────────────────────────────

  @Test
  void givenUntrustedSubject_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-CN");
    request.addHeader("X-SSL-Client-CN", b64("CN=unknown,O=Other,C=DE"));

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenMatchingIssuerAndWildcardSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(new TrustedCertificate(ISSUER_DN, "*")));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  // ── wildcards ───────────────────────────────────────────────────────────────

  @Test
  void givenWildcardIssuerAndWildcardSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(WILDCARD_CERT));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenWildcardCertAndMissingIssuerHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(WILDCARD_CERT));
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWildcardCertAndMissingSubjectHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of(WILDCARD_CERT));
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-CN");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  // ── DN formats ──────────────────────────────────────────────────────────────

  @Test
  void givenSlashSeparatedDnsMatchingTrustedEntry_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    String slashIssuer = "/C=US/O=ACME/CN=issuer";
    String slashSubject = "/C=US/O=ACME/CN=client";
    classUnderTest = new KymaTLSAuthenticator(Set.of(new TrustedCertificate(slashIssuer, slashSubject)));
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer");
    request.removeHeader("X-SSL-Client-CN");
    request.addHeader("X-SSL-Client-Issuer", b64(slashIssuer));
    request.addHeader("X-SSL-Client-CN", b64(slashSubject));

    assertTrue(classUnderTest.isAuthenticated(request));
  }

  // ── multiple trusted certificates ───────────────────────────────────────────

  @Test
  void givenMultipleTrustedCertificatesAndOneMatches_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(
        Set.of(new TrustedCertificate("CN=other,O=Other,C=DE", "CN=other,O=Other,C=DE"), TRUSTED_CERT));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenNoTrustedCertificates_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    classUnderTest = new KymaTLSAuthenticator(Set.of());

    assertFalse(classUnderTest.isAuthenticated(validRequest()));
  }

  // ── full happy path ─────────────────────────────────────────────────────────

  @Test
  void givenAllHeadersValidAndCertificateTrusted_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private static MockHttpServletRequest validRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-SSL-Client-Issuer", ISSUER_DN_B64);
    request.addHeader("X-SSL-Client-CN", SUBJECT_DN_B64);
    return request;
  }

  private static String b64(String value) {
    return Base64.getEncoder().encodeToString(value.getBytes(UTF_8));
  }
}
