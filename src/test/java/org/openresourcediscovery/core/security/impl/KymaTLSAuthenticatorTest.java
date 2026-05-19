package org.openresourcediscovery.core.security.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static org.assertj.core.api.Assertions.assertThat;
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
class KymaTLSAuthenticatorTest {

  private static final String ISSUER_DN = "CN=issuer,O=ACME,C=US";
  private static final String SUBJECT_DN = "CN=client,O=ACME,C=US";
  private static final String ISSUER_HEADER = getEncoder().encodeToString(ISSUER_DN.getBytes(UTF_8));
  private static final String SUBJECT_HEADER = getEncoder().encodeToString(SUBJECT_DN.getBytes(UTF_8));

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessStrategiesResolver accessStrategiesResolver;

  private KymaTLSAuthenticator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new KymaTLSAuthenticator(accessStrategiesResolver);

    when(request.getHeader("X-SSL-Client-CN")).thenReturn(SUBJECT_HEADER);
    when(request.getHeader("X-SSL-Client-Issuer")).thenReturn(ISSUER_HEADER);
  }

  // ── happy paths ─────────────────────────────────────────────────────────────

  @Test
  void givenMatchingStrategyAndExactCertificate_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenWildcardIssuerAndMatchingSubject_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate("*", SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenMatchingIssuerAndWildcardSubject_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenFullWildcardCertificate_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate("*", "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenTokensInDifferentOrder_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(
        CMP_MTLS, Set.of(new TrustedCertificate("O=ACME,CN=issuer,C=US", "O=ACME,CN=client,C=US")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenMultipleTrustedCertificatesAndSecondMatches_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest.configure(
        CMP_MTLS,
        Set.of(
            new TrustedCertificate(ISSUER_DN, SUBJECT_DN),
            new TrustedCertificate("CN=other-issuer,O=ACME,C=US", "CN=other,O=ACME,C=US")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenMultipleStrategiesAndOneMatches_whenIsAuthenticated_thenReturnsTrue() {
    classUnderTest
        .configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)))
        .configure(BAH_MTLS, Set.of(new TrustedCertificate("CN=other-issuer,O=ACME,C=US", "*")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  // ── access-strategy mismatches ───────────────────────────────────────────────

  @Test
  void givenNoMatchingAccessStrategy_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyAccessStrategies_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of());

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyTrustedCertificatesMap_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  // ── certificate mismatches ───────────────────────────────────────────────────

  @Test
  void givenIssuerMismatch_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate("CN=other-issuer,O=ACME,C=US", SUBJECT_DN)));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenSubjectMismatch_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, "CN=other,O=ACME,C=US")));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  // ── missing / empty headers ──────────────────────────────────────────────────

  @Test
  void givenMissingSubjectHeader_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(request.getHeader("X-SSL-Client-CN")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenMissingIssuerHeader_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(request.getHeader("X-SSL-Client-Issuer")).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptySubjectHeader_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(request.getHeader("X-SSL-Client-CN")).thenReturn("");
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyIssuerHeader_whenIsAuthenticated_thenReturnsFalse() {
    classUnderTest.configure(CMP_MTLS, Set.of(new TrustedCertificate(ISSUER_DN, SUBJECT_DN)));

    when(request.getHeader("X-SSL-Client-Issuer")).thenReturn("");
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }
}
