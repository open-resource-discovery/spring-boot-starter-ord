package org.openresourcediscovery.core.services.impl;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.core.services.StaticResourceRegistry.StaticResource;

public class StaticResourceRegistryImplTest {

  private StaticResourceRegistryImpl classUnderTest;

  @BeforeEach
  public void setUp() {
    classUnderTest = new StaticResourceRegistryImpl();
  }

  @Test
  public void givenNoResources_whenGetAllIsInvoked_thenCorrectResultIsReturned() {
    assertThat(classUnderTest.getAll()).isEmpty();
  }

  @Test
  public void givenSomeResources_whenGetAllIsInvoked_thenCorrectResultIsReturned() {
    StaticResource resource = mock(StaticResource.class);

    classUnderTest.register("resource", emptySet(), resource);

    assertThat(classUnderTest.getAll()).isEqualTo(Map.of("resource", resource));
  }

  @Test
  public void givenNonExistingResources_whenLookupAccessStrategiesIsInvoked_thenCorrectResultIsReturned() {
    assertThat(classUnderTest.lookupAccessStrategies("resource")).isEmpty();
  }

  @Test
  public void givenExistingResources_whenLookupAccessStrategiesIsInvoked_thenCorrectResultIsReturned() {
    classUnderTest.register(
        "resource", Set.of("open", "basic-auth", "sap:cmp-mtls:v1"), mock(StaticResource.class));

    assertThat(classUnderTest.lookupAccessStrategies("resource"))
        .isEqualTo(Set.of("open", "basic-auth", "sap:cmp-mtls:v1"));
  }

  @Test
  public void givenNonExistingResources_whenLookupStaticResourceIsInvoked_thenCorrectResultIsReturned() {
    assertThat(classUnderTest.lookupStaticResource("resource")).isEqualTo(Optional.empty());
  }

  @Test
  public void givenExistingResources_whenLookupStaticResourceIsInvoked_thenCorrectResultIsReturned() {
    StaticResource resource = mock(StaticResource.class);

    classUnderTest.register("resource", emptySet(), resource);

    assertThat(classUnderTest.lookupStaticResource("resource")).isEqualTo(Optional.of(resource));
  }
}
