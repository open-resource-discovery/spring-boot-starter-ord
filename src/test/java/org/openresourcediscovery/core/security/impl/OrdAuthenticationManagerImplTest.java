package org.openresourcediscovery.core.security.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class OrdAuthenticationManagerImplTest {

  @Mock
  private Authentication authentication;

  @Mock
  private TLSAuthenticator tlsAuthenticator;

  @Mock
  private AuthenticationTrustResolver authenticationTrustResolver;

  private OrdAuthenticationManagerImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new OrdAuthenticationManagerImpl(tlsAuthenticator, authenticationTrustResolver);
    getContext().setAuthentication(authentication);
  }

  @AfterEach
  void tearDown() {
    clearContext();
  }

  // ── isAuthenticated ─────────────────────────────────────────────────────────

  @Test
  void givenTlsAuthenticated_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    doReturn(true).when(tlsAuthenticator).isAuthenticated(request);

    assertTrue(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenBasicAuthAuthenticated_whenIsAuthenticatedIsCalled_thenTrueIsReturned() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    doReturn(false).when(tlsAuthenticator).isAuthenticated(request);
    doReturn(true).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertTrue(classUnderTest.isAuthenticated(request));
  }

  @Test
  void givenNotAuthenticated_whenIsAuthenticatedIsCalled_thenFalseIsReturned() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    doReturn(false).when(tlsAuthenticator).isAuthenticated(request);
    doReturn(false).when(authenticationTrustResolver).isAuthenticated(authentication);

    assertFalse(classUnderTest.isAuthenticated(request));
  }
}
