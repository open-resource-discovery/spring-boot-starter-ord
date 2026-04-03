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

class CloudFoundryTLSAuthenticatorTest {

  // Comma-separated DNs (Base64-encoded as CloudFoundry sends them)
  private static final String ROOT_CA_DN = "CN=root-ca,O=ACME,C=US";
  private static final String ISSUER_DN = "CN=issuer,O=ACME,C=US";
  private static final String SUBJECT_DN = "CN=client,O=ACME,C=US";

  private static final String ROOT_CA_DN_B64 = b64(ROOT_CA_DN);
  private static final String ISSUER_DN_B64 = b64(ISSUER_DN);
  private static final String SUBJECT_DN_B64 = b64(SUBJECT_DN);

  private static final TrustedCertificate TRUSTED_CERT = new TrustedCertificate(ISSUER_DN, SUBJECT_DN);
  private static final TrustedCertificate WILDCARD_CERT = new TrustedCertificate("*", "*");

  private CloudFoundryTLSAuthenticator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new CloudFoundryTLSAuthenticator(Set.of(TRUSTED_CERT), Set.of(ROOT_CA_DN));
  }

  // ── isXfccProxyVerified – missing / wrong XFCC headers ─────────────────────

  @Test
  void givenMissingXfccHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-Forwarded-Client-Cert");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWrongXSslClientHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client");
    request.addHeader("X-SSL-Client", "0");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWrongXSslClientVerifyHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Verify");
    request.addHeader("X-SSL-Client-Verify", "1");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  // ── isTrustedRootCertificateAuthorityDN ────────────────────────────────────

  @Test
  void givenMissingRootCaDnHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Root-CA-DN");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenUntrustedRootCaDn_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Root-CA-DN");
    request.addHeader("X-SSL-Client-Root-CA-DN", b64("CN=untrusted,O=Other,C=DE"));

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWildcardRootCaDn_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new CloudFoundryTLSAuthenticator(Set.of(TRUSTED_CERT), Set.of("*"));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenSlashSeparatedRootCaDnMatchingTrustedEntry_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    String slashDn = "/C=US/O=ACME/CN=root-ca";
    classUnderTest = new CloudFoundryTLSAuthenticator(Set.of(TRUSTED_CERT), Set.of(slashDn));
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Root-CA-DN");
    request.addHeader("X-SSL-Client-Root-CA-DN", b64(slashDn));

    assertTrue(classUnderTest.isAuthenticated(request));
  }

  // ── isTrustedCertificate ────────────────────────────────────────────────────

  @Test
  void givenMissingIssuerDnHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer-DN");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenMissingSubjectDnHeader_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Subject-DN");

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenUntrustedIssuerDn_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer-DN");
    request.addHeader("X-SSL-Client-Issuer-DN", b64("CN=unknown-issuer,O=Other,C=DE"));

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenUntrustedSubjectDn_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Subject-DN");
    request.addHeader("X-SSL-Client-Subject-DN", b64("CN=unknown-client,O=Other,C=DE"));

    assertFalse(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenWildcardIssuerAndWildcardSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new CloudFoundryTLSAuthenticator(Set.of(WILDCARD_CERT), Set.of(ROOT_CA_DN));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenWildcardIssuerAndMatchingSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest =
        new CloudFoundryTLSAuthenticator(Set.of(new TrustedCertificate("*", SUBJECT_DN)), Set.of(ROOT_CA_DN));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenMatchingIssuerAndWildcardSubject_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest =
        new CloudFoundryTLSAuthenticator(Set.of(new TrustedCertificate(ISSUER_DN, "*")), Set.of(ROOT_CA_DN));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenSlashSeparatedCertDnsMatchingTrustedEntry_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    String slashIssuer = "/C=US/O=ACME/CN=issuer";
    String slashSubject = "/C=US/O=ACME/CN=client";
    classUnderTest = new CloudFoundryTLSAuthenticator(
        Set.of(new TrustedCertificate(slashIssuer, slashSubject)), Set.of(ROOT_CA_DN));
    MockHttpServletRequest request = validRequest();
    request.removeHeader("X-SSL-Client-Issuer-DN");
    request.removeHeader("X-SSL-Client-Subject-DN");
    request.addHeader("X-SSL-Client-Issuer-DN", b64(slashIssuer));
    request.addHeader("X-SSL-Client-Subject-DN", b64(slashSubject));

    assertTrue(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenMultipleTrustedCertificatesAndOneMatches_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    classUnderTest = new CloudFoundryTLSAuthenticator(
        Set.of(new TrustedCertificate("CN=other,O=Other,C=DE", "CN=other,O=Other,C=DE"), TRUSTED_CERT),
        Set.of(ROOT_CA_DN));

    assertTrue(classUnderTest.isAuthenticated(validRequest()));
  }

  @Test
  void givenNoTrustedCertificates_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    classUnderTest = new CloudFoundryTLSAuthenticator(Set.of(), Set.of(ROOT_CA_DN));

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
    request.addHeader("X-Forwarded-Client-Cert", "present");
    request.addHeader("X-SSL-Client", "1");
    request.addHeader("X-SSL-Client-Verify", "0");
    request.addHeader("X-SSL-Client-Root-CA-DN", ROOT_CA_DN_B64);
    request.addHeader("X-SSL-Client-Issuer-DN", ISSUER_DN_B64);
    request.addHeader("X-SSL-Client-Subject-DN", SUBJECT_DN_B64);
    return request;
  }

  private static String b64(String value) {
    return Base64.getEncoder().encodeToString(value.getBytes(UTF_8));
  }
}
