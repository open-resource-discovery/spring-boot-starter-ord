package org.openresourcediscovery.utils.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class OnOrdPackagesProvidedTest {

  @Mock
  private ConditionContext context;

  @Mock
  private AnnotatedTypeMetadata metadata;

  private OnOrdPackagesProvided classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new OnOrdPackagesProvided();
  }

  @Test
  void givenOrdPackagesPropertyIsSet_whenGetMatchOutcomeIsCalled_thenMatchIsReturned() {
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty("ord.packages[0]", "org.example");
    when(context.getEnvironment()).thenReturn(environment);

    ConditionOutcome outcome = classUnderTest.getMatchOutcome(context, metadata);

    assertTrue(outcome.isMatch());
  }

  @Test
  void givenOrdPackagesPropertyIsNotSet_whenGetMatchOutcomeIsCalled_thenNoMatchIsReturned() {
    MockEnvironment environment = new MockEnvironment();
    when(context.getEnvironment()).thenReturn(environment);

    ConditionOutcome outcome = classUnderTest.getMatchOutcome(context, metadata);

    assertFalse(outcome.isMatch());
  }
}
