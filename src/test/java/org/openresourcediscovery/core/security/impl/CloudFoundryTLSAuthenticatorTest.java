package org.openresourcediscovery.core.security.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.BAH_MTLS;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.CMP_MTLS;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.OPEN;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager.TrustedCertificate;

@ExtendWith(MockitoExtension.class)
class CloudFoundryTLSAuthenticatorTest {

  private static final String ISSUER_DN = "CN=issuer,O=ACME,C=US";
  private static final String SUBJECT_DN = "CN=client,O=ACME,C=US";
  private static final String ROOT_CA_DN = "CN=root-ca,O=ACME,C=US";
  private static final String ISSUER_HEADER = getEncoder().encodeToString(ISSUER_DN.getBytes(UTF_8));
  private static final String ROOT_CA_HEADER = getEncoder().encodeToString(ROOT_CA_DN.getBytes(UTF_8));
  private static final String SUBJECT_HEADER = getEncoder().encodeToString(SUBJECT_DN.getBytes(UTF_8));

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessStrategiesResolver accessStrategiesResolver;

  private CloudFoundryTLSAuthenticator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(CMP_MTLS, Set.of(ROOT_CA_DN), Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    lenient().when(request.getHeader("X-SSL-Client")).thenReturn("1");
    lenient().when(request.getHeader("X-SSL-Client-Verify")).thenReturn("0");
    lenient().when(request.getHeader("X-Forwarded-Client-Cert")).thenReturn("cert-value");
    lenient().when(request.getHeader("X-SSL-Client-Issuer-DN")).thenReturn(ISSUER_HEADER);
    lenient().when(request.getHeader("X-SSL-Client-Root-CA-DN")).thenReturn(ROOT_CA_HEADER);
    lenient().when(request.getHeader("X-SSL-Client-Subject-DN")).thenReturn(SUBJECT_HEADER);
  }

  // ── happy path ───────────────────────────────────────────────────────────────

  @Test
  void givenAllConditionsMet_whenIsAuthenticated_thenReturnsTrue() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  // ── XFCC proxy verification ──────────────────────────────────────────────────

  @Test
  void givenMissingXForwardedClientCert_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-Forwarded-Client-Cert")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));
    // X-SSL-Client and X-SSL-Client-Verify are not read after nonNull() short-circuits

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenXSslClientNotOne_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client")).thenReturn("0");
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenXSslClientVerifyNotZero_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Verify")).thenReturn("1");
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenNullXSslClient_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenNullXSslClientVerify_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Verify")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  // ── access-strategy matching ─────────────────────────────────────────────────

  @Test
  void givenNoMatchingAccessStrategy_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyAccessStrategies_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of());

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenUnconfiguredStrategy_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BAH_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  // ── root CA DN matching ───────────────────────────────────────────────────────

  @Test
  void givenRootCaDnMismatch_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));
    when(request.getHeader("X-SSL-Client-Root-CA-DN"))
        .thenReturn(getEncoder().encodeToString("CN=other-root,O=ACME,C=US".getBytes(UTF_8)));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenMissingRootCaDnHeader_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Root-CA-DN")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyRootCaDnHeader_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Root-CA-DN")).thenReturn("");
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenRootCaDnTokensInDifferentOrder_whenIsAuthenticated_thenReturnsTrue() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));
    when(request.getHeader("X-SSL-Client-Root-CA-DN"))
        .thenReturn(getEncoder().encodeToString("O=ACME,CN=root-ca,C=US".getBytes(UTF_8)));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  // ── issuer / subject cert matching ───────────────────────────────────────────

  @Test
  void givenIssuerDnMismatch_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));
    when(request.getHeader("X-SSL-Client-Issuer-DN"))
        .thenReturn(getEncoder().encodeToString("CN=other-issuer,O=ACME,C=US".getBytes(UTF_8)));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenSubjectDnMismatch_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));
    when(request.getHeader("X-SSL-Client-Subject-DN"))
        .thenReturn(getEncoder().encodeToString("CN=other-client,O=ACME,C=US".getBytes(UTF_8)));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenMissingIssuerDnHeader_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Issuer-DN")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenMissingSubjectDnHeader_whenIsAuthenticated_thenReturnsFalse() {
    when(request.getHeader("X-SSL-Client-Subject-DN")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  // ── wildcard certificate matching ─────────────────────────────────────────────

  @Test
  void givenWildcardIssuerAndMatchingSubject_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(CMP_MTLS, Set.of(ROOT_CA_DN), Set.of(new TrustedCertificate("*", SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenMatchingIssuerAndWildcardSubject_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(CMP_MTLS, Set.of(ROOT_CA_DN), Set.of(new TrustedCertificate(ISSUER_DN, "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenFullWildcardCertificate_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(CMP_MTLS, Set.of(ROOT_CA_DN), Set.of(new TrustedCertificate("*", "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  // ── multi-strategy / multi-cert configuration ─────────────────────────────────

  @Test
  void givenMultipleTrustedCertsAndSecondMatches_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(
            CMP_MTLS,
            Set.of(ROOT_CA_DN),
            Set.of(
                new TrustedCertificate(ISSUER_DN, SUBJECT_DN),
                new TrustedCertificate("CN=other-issuer,O=ACME,C=US", "CN=other-client,O=ACME,C=US")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenMultipleStrategiesConfiguredAndMatchingOne_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(CMP_MTLS, Set.of(ROOT_CA_DN), Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)))
        .configure(BAH_MTLS, Set.of("CN=other-root,O=ACME,C=US"), Set.of(new TrustedCertificate("*", "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenNoStrategiesConfigured_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest = new CloudFoundryTLSAuthenticator(accessStrategiesResolver);

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }
}
