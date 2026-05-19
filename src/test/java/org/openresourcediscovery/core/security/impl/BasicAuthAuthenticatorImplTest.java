package org.openresourcediscovery.core.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.BASIC;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.CMP_MTLS;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.OPEN;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BasicAuthAuthenticatorImplTest {

  private static final Authentication UNAUTHENTICATED = new TestingAuthenticationToken("username", "password");
  private static final Authentication FULLY_AUTHENTICATED =
      new TestingAuthenticationToken("username", "password", createAuthorityList("ROLE_USER"));
  private static final Authentication REMEMBER_ME_AUTHENTICATED =
      new RememberMeAuthenticationToken("key", "user", createAuthorityList("ROLE_USER"));

  @Mock
  private HttpServletRequest request;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private AccessStrategiesResolver accessStrategiesResolver;

  private BasicAuthAuthenticatorImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest =
        new BasicAuthAuthenticatorImpl(accessStrategiesResolver, new AuthenticationTrustResolverImpl());

    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void givenBasicStrategyAndFullyAuthenticatedUser_whenIsAuthenticated_thenReturnsTrue() {
    when(securityContext.getAuthentication()).thenReturn(FULLY_AUTHENTICATED);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }

  @Test
  void givenBasicStrategyAndUnauthenticatedUser_whenIsAuthenticated_thenReturnsFalse() {
    when(securityContext.getAuthentication()).thenReturn(UNAUTHENTICATED);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenBasicStrategyAndNullAuthentication_whenIsAuthenticated_thenReturnsFalse() {
    when(securityContext.getAuthentication()).thenReturn(null);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenBasicStrategyAndRememberMeUser_whenIsAuthenticated_thenReturnsFalse() {
    when(securityContext.getAuthentication()).thenReturn(REMEMBER_ME_AUTHENTICATED);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenNonBasicStrategy_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenNonBasicStrategyAndFullyAuthenticatedUser_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenEmptyAccessStrategies_whenIsAuthenticated_thenReturnsFalse() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of());

    assertThat(classUnderTest.isAuthenticated(request)).isFalse();
  }

  @Test
  void givenNonBasicStrategy_whenIsAuthenticated_thenSecurityContextIsNotQueried() {
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey()));

    classUnderTest.isAuthenticated(request);

    verify(securityContext, never()).getAuthentication();
  }

  @Test
  void givenBasicStrategyAmongOthersAndFullyAuthenticatedUser_whenIsAuthenticated_thenReturnsTrue() {
    when(securityContext.getAuthentication()).thenReturn(FULLY_AUTHENTICATED);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey(), CMP_MTLS.getKey()));

    assertThat(classUnderTest.isAuthenticated(request)).isTrue();
  }
}
