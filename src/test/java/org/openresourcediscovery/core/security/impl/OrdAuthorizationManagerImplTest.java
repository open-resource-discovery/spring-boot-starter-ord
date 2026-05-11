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
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class OrdAuthorizationManagerImplTest {

  private static final String DOCUMENT_NAME = "document-1";
  private static final String RESOURCE_NAME = "resource-1";
  private static final String ORD_DOCUMENT_PATH = "/ord/v1/documents/%s".formatted(DOCUMENT_NAME);
  private static final String ORD_RESOURCE_PATH = "/ord/v1/resources/%s".formatted(RESOURCE_NAME);

  @Mock
  private TLSAuthenticator tlsAuthenticator;

  @Mock
  private DocumentSchemaRegistry documentSchemaRegistry;

  @Mock
  private StaticResourceRegistry staticResourceRegistry;

  @Mock
  private AuthenticationTrustResolver authenticationTrustResolver;

  @Mock
  private Authentication authentication;

  private OrdAuthorizationManagerImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new OrdAuthorizationManagerImpl(
        tlsAuthenticator, documentSchemaRegistry, staticResourceRegistry, authenticationTrustResolver);

    getContext().setAuthentication(authentication);
  }

  @AfterEach
  void tearDown() {
    clearContext();
  }

  // ── check – open access strategy ───────────────────────────────────────────

  @Test
  void givenDocumentWithOpenAccessStrategyRequested_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);

    assertTrue(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenResourceWithOpenAccessStrategyRequested_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);

    assertTrue(check(ORD_RESOURCE_PATH).isGranted());
  }

  @Test
  void givenDocumentWithOpenAlongsideOtherStrategies_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open", "basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);

    assertTrue(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenResourceWithOpenAlongsideOtherStrategies_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("open", "basic-auth")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);

    assertTrue(check(ORD_RESOURCE_PATH).isGranted());
  }

  // ── check – sap:cmp-mtls:v1 access strategy ────────────────────────────────

  @Test
  void givenDocumentWithMtlsStrategyAndTlsAuthenticated_whenCheckIsCalled_thenGranted() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_DOCUMENT_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);
    doReturn(true).when(tlsAuthenticator).isAuthenticated(request);

    assertTrue(check(request, () -> null).isGranted());
  }

  @Test
  void givenResourceWithMtlsStrategyAndTlsAuthenticated_whenCheckIsCalled_thenGranted() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_RESOURCE_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);
    doReturn(true).when(tlsAuthenticator).isAuthenticated(request);

    assertTrue(check(request, () -> null).isGranted());
  }

  @Test
  void givenDocumentWithMtlsStrategyAndTlsNotAuthenticated_whenCheckIsCalled_thenDenied() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_DOCUMENT_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);
    doReturn(false).when(tlsAuthenticator).isAuthenticated(request);

    assertFalse(check(request, () -> null).isGranted());
  }

  @Test
  void givenResourceWithMtlsStrategyAndTlsNotAuthenticated_whenCheckIsCalled_thenDenied() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(ORD_RESOURCE_PATH);
    doReturn(Set.of("sap:cmp-mtls:v1")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);
    doReturn(false).when(tlsAuthenticator).isAuthenticated(request);

    assertFalse(check(request, () -> null).isGranted());
  }

  // ── check – basic-auth access strategy ─────────────────────────────────────

  @Test
  void givenDocumentWithBasicAuthStrategyAndAuthenticated_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);
    doReturn(true).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertTrue(check(ORD_DOCUMENT_PATH, () -> authentication).isGranted());
  }

  @Test
  void givenResourceWithBasicAuthStrategyAndAuthenticated_whenCheckIsCalled_thenGranted() {
    doReturn(Set.of("basic-auth")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);
    doReturn(true).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertTrue(check(ORD_RESOURCE_PATH, () -> authentication).isGranted());
  }

  @Test
  void givenDocumentWithBasicAuthStrategyAndNotAuthenticated_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("basic-auth")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);
    doReturn(false).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertFalse(check(ORD_DOCUMENT_PATH, () -> authentication).isGranted());
  }

  @Test
  void givenResourceWithBasicAuthStrategyAndNotAuthenticated_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("basic-auth")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);
    doReturn(false).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertFalse(check(ORD_RESOURCE_PATH, () -> authentication).isGranted());
  }

  // ── check – no matching access strategy ────────────────────────────────────

  @Test
  void givenDocumentWithNoAccessStrategies_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of()).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);

    assertFalse(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenResourceWithNoAccessStrategies_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of()).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);

    assertFalse(check(ORD_RESOURCE_PATH).isGranted());
  }

  @Test
  void givenDocumentWithUnknownAccessStrategy_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("custom:strategy:v1")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);

    assertFalse(check(ORD_DOCUMENT_PATH).isGranted());
  }

  @Test
  void givenResourceWithUnknownAccessStrategy_whenCheckIsCalled_thenDenied() {
    doReturn(Set.of("custom:strategy:v1")).when(staticResourceRegistry).lookupAccessStrategies(RESOURCE_NAME);

    assertFalse(check(ORD_RESOURCE_PATH).isGranted());
  }

  // ── check – non-document paths ─────────────────────────────────────────────

  @Test
  void givenUnsupportedServletPath_whenCheckIsCalled_thenDenied() {
    assertFalse(check("/ord/v1/unsupported").isGranted());
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
    doReturn(Set.of("open")).when(documentSchemaRegistry).lookupAccessStrategies(DOCUMENT_NAME);

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
