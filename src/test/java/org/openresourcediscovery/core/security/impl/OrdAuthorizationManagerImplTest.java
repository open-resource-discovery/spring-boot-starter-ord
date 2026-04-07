package org.openresourcediscovery.core.security.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class OrdAuthorizationManagerImplTest {

  private static final String DOC_NAME = "doc-1";
  private static final String ORD_DOCUMENT_PATH = "/ord/v1/documents/" + DOC_NAME;

  @Mock
  private TLSAuthenticator tlsAuthenticator;

  @Mock
  private DocumentSchemaRegistry documentSchemaRegistry;

  @Mock
  private AuthenticationTrustResolver authenticationTrustResolver;

  @Mock
  private Authentication authentication;

  private OrdAuthorizationManagerImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest =
        new OrdAuthorizationManagerImpl(tlsAuthenticator, documentSchemaRegistry, authenticationTrustResolver);
    getContext().setAuthentication(authentication);
  }

  @AfterEach
  void tearDown() {
    clearContext();
  }

  // ── check – open access strategy ───────────────────────────────────────────

  @Test
  void givenOpenAccessStrategy_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);

    assertTrue(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenOpenAlongsideOtherStrategies_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open", "basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);

    assertTrue(check(ORD_DOCUMENT_PATH).isGranted());
  }

  // ── check – sap:cmp-mtls:v1 access strategy ────────────────────────────────

  @Test
  void givenMtlsStrategyAndTlsAuthenticated_whenCheckIsCalled_thenGranted() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_DOCUMENT_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);
    doReturn(true).when(tlsAuthenticator).isAuthenticated(request);

    assertTrue(check(request, () -> null).isGranted());
  }

  @Test
  void givenMtlsStrategyAndTlsNotAuthenticated_whenCheckIsCalled_thenDenied() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_DOCUMENT_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);
    doReturn(false).when(tlsAuthenticator).isAuthenticated(request);

    assertFalse(check(request, () -> null).isGranted());
  }

  // ── check – basic-auth access strategy ─────────────────────────────────────

  @Test
  void givenBasicAuthStrategyAndAuthenticated_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);
    doReturn(true).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertTrue(check(ORD_DOCUMENT_PATH, () -> authentication).isGranted());
  }

  @Test
  void givenBasicAuthStrategyAndNotAuthenticated_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);
    doReturn(false).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertFalse(check(ORD_DOCUMENT_PATH, () -> authentication).isGranted());
  }

  // ── check – no matching access strategy ────────────────────────────────────

  @Test
  void givenNoAccessStrategies_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of()).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);

    assertFalse(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenUnknownAccessStrategy_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("custom:strategy:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);

    assertFalse(check(ORD_DOCUMENT_PATH).isGranted());
  }

  // ── check – non-document paths ─────────────────────────────────────────────

  @Test
  void givenNonDocumentServletPath_whenCheckIsCalled_thenDenied() {
    assertFalse(check("/ord/v1/other").isGranted());
  }

  @Test
  void givenRootPath_whenCheckIsCalled_thenDenied() {
    assertFalse(check("/").isGranted());
  }

  // ── check – pathInfo takes precedence over servletPath ─────────────────────

  @Test
  void givenOpenStrategyResolvedViaPathInfo_whenCheckIsCalled_thenGranted() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setPathInfo(ORD_DOCUMENT_PATH);
    doReturn(Set.of("open")).when(documentSchemaRegistry).lookupAccessStrategies(DOC_NAME);

    assertTrue(check(request, () -> null).isGranted());
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private AuthorizationDecision check(String servletPath) {
    return check(servletPath, () -> null);
  }

  private AuthorizationDecision check(String servletPath, Supplier<Authentication> supplier) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(servletPath);
    return check(request, supplier);
  }

  private AuthorizationDecision check(MockHttpServletRequest request, Supplier<Authentication> supplier) {
    return classUnderTest.check(supplier, new RequestAuthorizationContext(request));
  }
}
