package org.openresourcediscovery.core.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.BASIC;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.CMP_MTLS;
import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.OPEN;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class OrdAuthorizationManagerImplTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private RequestAuthorizationContext context;

  @Mock
  private OrdAuthenticationManager authenticator;

  @Mock
  private OrdAuthenticationManager secondAuthenticator;

  @Mock
  private Supplier<Authentication> authenticationSupplier;

  @Mock
  private AccessStrategiesResolver accessStrategiesResolver;

  private OrdAuthorizationManagerImpl classUnderTest;

  @BeforeEach
  void setUp() {
    when(context.getRequest()).thenReturn(request);
  }

  @Test
  void givenOpenAccessStrategy_whenCheckIsCalled_thenAccessIsGranted() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isTrue();

    verify(authenticator, never()).isAuthenticated(request);
  }

  @Test
  void givenOpenAccessStrategyAmongOthers_whenCheckIsCalled_thenAccessIsGranted() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(OPEN.getKey(), BASIC.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isTrue();

    verify(authenticator, never()).isAuthenticated(request);
  }

  @Test
  void givenAuthenticatorSucceeds_whenCheckIsCalled_thenAccessIsGranted() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator));

    when(authenticator.isAuthenticated(request)).thenReturn(true);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isTrue();
  }

  @Test
  void givenNoAuthenticatorSucceeds_whenCheckIsCalled_thenAccessIsDenied() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator));

    when(authenticator.isAuthenticated(request)).thenReturn(false);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isFalse();
  }

  @Test
  void givenNoAuthenticatorsAndNoOpenStrategy_whenCheckIsCalled_thenAccessIsDenied() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of());

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isFalse();
  }

  @Test
  void givenEmptyAccessStrategies_whenCheckIsCalled_thenAccessIsDenied() {
    classUnderTest = new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator));

    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of());
    when(authenticator.isAuthenticated(request)).thenReturn(false);

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isFalse();
  }

  @Test
  void givenFirstAuthenticatorFailsAndSecondSucceeds_whenCheckIsCalled_thenAccessIsGranted() {
    classUnderTest =
        new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator, secondAuthenticator));

    when(authenticator.isAuthenticated(request)).thenReturn(false);
    when(secondAuthenticator.isAuthenticated(request)).thenReturn(true);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isTrue();
  }

  @Test
  void givenMultipleAuthenticatorsAllFail_whenCheckIsCalled_thenAccessIsDenied() {
    classUnderTest =
        new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator, secondAuthenticator));

    when(authenticator.isAuthenticated(request)).thenReturn(false);
    when(secondAuthenticator.isAuthenticated(request)).thenReturn(false);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(CMP_MTLS.getKey()));

    assertThat(classUnderTest.check(authenticationSupplier, context).isGranted())
        .isFalse();
  }

  @Test
  void givenFirstAuthenticatorSucceeds_whenCheckIsCalled_thenSecondAuthenticatorIsNotInvoked() {
    classUnderTest =
        new OrdAuthorizationManagerImpl(accessStrategiesResolver, List.of(authenticator, secondAuthenticator));

    when(authenticator.isAuthenticated(request)).thenReturn(true);
    when(accessStrategiesResolver.resolve(request)).thenReturn(Set.of(BASIC.getKey()));

    classUnderTest.check(authenticationSupplier, context);

    verify(secondAuthenticator, never()).isAuthenticated(request);
  }
}
