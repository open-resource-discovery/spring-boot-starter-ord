package org.openresourcediscovery.core.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;

@ExtendWith(MockitoExtension.class)
class AccessStrategiesResolverImplTest {

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private HttpServletRequest request;

  @Mock
  private DocumentSchemaRegistry documentSchemaRegistry;

  @Mock
  private StaticResourceRegistry staticResourceRegistry;

  private AccessStrategiesResolverImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest =
        new AccessStrategiesResolverImpl(ordProperties, documentSchemaRegistry, staticResourceRegistry);

    lenient().doReturn("/ord/v1/documents").when(ordProperties).getApiBasePathDocuments();
    lenient().doReturn("/ord/v1/resources").when(ordProperties).getApiBasePathResources();
  }

  @Test
  void givenDocumentPath_whenResolve_thenReturnsCorrectDocumentAccessStrategies() {
    when(request.getPathInfo()).thenReturn("/ord/v1/documents/my-doc");
    when(documentSchemaRegistry.lookupAccessStrategies("my-doc")).thenReturn(Set.of("open", "basic-auth"));

    assertThat(classUnderTest.resolve(request)).containsExactlyInAnyOrder("open", "basic-auth");

    verify(documentSchemaRegistry).lookupAccessStrategies("my-doc");
    verify(staticResourceRegistry, never()).lookupAccessStrategies(any());
  }

  @Test
  void givenResourcePath_whenResolve_thenReturnsStaticResourceAccessStrategies() {
    when(request.getPathInfo()).thenReturn("/ord/v1/resources/my-doc");
    when(staticResourceRegistry.lookupAccessStrategies("my-doc")).thenReturn(Set.of("open", "basic-auth"));

    assertThat(classUnderTest.resolve(request)).containsExactlyInAnyOrder("open", "basic-auth");

    verify(staticResourceRegistry).lookupAccessStrategies("my-doc");
    verify(documentSchemaRegistry, never()).lookupAccessStrategies(any());
  }

  @Test
  void givenWellKnownPath_whenResolve_thenReturnsEmptySet() {
    when(request.getPathInfo()).thenReturn("/.well-known/open-resource-discovery");

    assertThat(classUnderTest.resolve(request)).isEmpty();

    verify(documentSchemaRegistry, never()).lookupAccessStrategies(any());
    verify(staticResourceRegistry, never()).lookupAccessStrategies(any());
  }

  @Test
  void givenUnknownPath_whenResolve_thenReturnsEmptySet() {
    when(request.getPathInfo()).thenReturn("/some/random/path");

    assertThat(classUnderTest.resolve(request)).isEmpty();

    verify(documentSchemaRegistry, never()).lookupAccessStrategies(any());
    verify(staticResourceRegistry, never()).lookupAccessStrategies(any());
  }

  @Test
  void givenNullPathInfoAndServletPath_whenResolve_thenServletPathIsUsed() {
    when(request.getPathInfo()).thenReturn(null);
    when(request.getServletPath()).thenReturn("/ord/v1/documents/my-doc");
    when(documentSchemaRegistry.lookupAccessStrategies("my-doc")).thenReturn(Set.of("open"));

    assertThat(classUnderTest.resolve(request)).containsExactly("open");
  }

  @Test
  void givenBlankPathInfoAndServletPath_whenResolve_thenServletPathIsUsed() {
    when(request.getPathInfo()).thenReturn("  ");
    when(request.getServletPath()).thenReturn("/ord/v1/documents/my-doc");
    when(documentSchemaRegistry.lookupAccessStrategies("my-doc")).thenReturn(Set.of("open"));

    assertThat(classUnderTest.resolve(request)).containsExactly("open");
  }

  @Test
  void givenPathInfoTakesPrecedenceOverServletPath_whenResolve_thenPathInfoIsUsed() {
    when(request.getPathInfo()).thenReturn("/ord/v1/documents/doc-from-pathinfo");
    when(documentSchemaRegistry.lookupAccessStrategies("doc-from-pathinfo")).thenReturn(Set.of("open"));

    assertThat(classUnderTest.resolve(request)).containsExactly("open");
    verify(documentSchemaRegistry).lookupAccessStrategies("doc-from-pathinfo");
  }

  @Test
  void givenBothPathInfoAndServletPathAreNull_whenResolve_thenReturnsEmptySet() {
    when(request.getPathInfo()).thenReturn(null);
    when(request.getServletPath()).thenReturn(null);

    assertThat(classUnderTest.resolve(request)).isEmpty();
  }
}
