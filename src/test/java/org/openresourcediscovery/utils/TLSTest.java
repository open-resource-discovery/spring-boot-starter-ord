package org.openresourcediscovery.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class TLSTest {

  // ── decode ──────────────────────────────────────────────────────────────────

  @Test
  void givenNullHeader_whenDecodeIsCalled_thenEmptyStringIsReturned() {
    assertEquals("", TLS.decode(null));
  }

  @Test
  void givenEmptyHeader_whenDecodeIsCalled_thenEmptyStringIsReturned() {
    assertEquals("", TLS.decode(""));
  }

  @Test
  void givenBase64EncodedValue_whenDecodeIsCalled_thenDecodedStringIsReturned() {
    String encoded = Base64.getEncoder().encodeToString("CN=client,O=ACME,C=US".getBytes(UTF_8));

    assertEquals("CN=client,O=ACME,C=US", TLS.decode(encoded));
  }

  // ── tokenize – comma-separated DN ──────────────────────────────────────────

  @Test
  void givenCommaSeparatedDn_whenTokenizeIsCalled_thenTokensAreReturned() {
    assertArrayEquals(new String[] {"CN=client", "O=ACME", "C=US"}, TLS.tokenize("CN=client,O=ACME,C=US"));
  }

  @Test
  void givenCommaSeparatedDnWithSpaces_whenTokenizeIsCalled_thenTokensAreTrimmed() {
    assertArrayEquals(new String[] {"CN=client", "O=ACME", "C=US"}, TLS.tokenize("CN=client, O=ACME, C=US"));
  }

  @Test
  void givenSingleTokenDn_whenTokenizeIsCalled_thenSingleTokenIsReturned() {
    assertArrayEquals(new String[] {"CN=client"}, TLS.tokenize("CN=client"));
  }

  // ── tokenize – slash-separated DN ──────────────────────────────────────────

  @Test
  void givenSlashSeparatedDn_whenTokenizeIsCalled_thenTokensAreReturned() {
    assertArrayEquals(new String[] {"C=US", "O=ACME", "CN=client"}, TLS.tokenize("/C=US/O=ACME/CN=client"));
  }

  @Test
  void givenSlashSeparatedDnWithLeadingSlash_whenTokenizeIsCalled_thenLeadingSlashIsStripped() {
    String[] tokens = TLS.tokenize("/CN=client");

    assertArrayEquals(new String[] {"CN=client"}, tokens);
  }

  // ── tokenize – edge cases ───────────────────────────────────────────────────

  @Test
  void givenEmptyString_whenTokenizeIsCalled_thenEmptyArrayIsReturned() {
    assertArrayEquals(new String[0], TLS.tokenize(""));
  }

  @Test
  void givenDnWithEmptySegments_whenTokenizeIsCalled_thenEmptySegmentsAreFiltered() {
    assertArrayEquals(new String[] {"CN=client", "O=ACME"}, TLS.tokenize("CN=client,,O=ACME"));
  }

  // ── tokensMatch ─────────────────────────────────────────────────────────────

  @Test
  void givenIdenticalTokenArrays_whenTokensMatchIsCalled_thenTrueIsReturned() {
    assertTrue(TLS.tokensMatch(
        new String[] {"CN=client", "O=ACME", "C=US"}, new String[] {"CN=client", "O=ACME", "C=US"}));
  }

  @Test
  void givenTokenArraysInDifferentOrder_whenTokensMatchIsCalled_thenTrueIsReturned() {
    assertTrue(TLS.tokensMatch(
        new String[] {"CN=client", "O=ACME", "C=US"}, new String[] {"C=US", "CN=client", "O=ACME"}));
  }

  @Test
  void givenTokenArraysWithDifferentValues_whenTokensMatchIsCalled_thenFalseIsReturned() {
    assertFalse(TLS.tokensMatch(
        new String[] {"CN=client", "O=ACME", "C=US"}, new String[] {"CN=other", "O=ACME", "C=US"}));
  }

  @Test
  void givenTokenArraysWithDifferentLengths_whenTokensMatchIsCalled_thenFalseIsReturned() {
    assertFalse(
        TLS.tokensMatch(new String[] {"CN=client", "O=ACME"}, new String[] {"CN=client", "O=ACME", "C=US"}));
  }

  @Test
  void givenEmptyTokenArrays_whenTokensMatchIsCalled_thenTrueIsReturned() {
    assertTrue(TLS.tokensMatch(new String[0], new String[0]));
  }

  @Test
  void givenOneEmptyAndOneNonEmptyTokenArray_whenTokensMatchIsCalled_thenFalseIsReturned() {
    assertFalse(TLS.tokensMatch(new String[] {"CN=client"}, new String[0]));
  }

  @Test
  void givenTokenArraysWithDuplicatesInExpectedButNotFound_whenTokensMatchIsCalled_thenFalseIsReturned() {
    assertFalse(TLS.tokensMatch(new String[] {"CN=client", "CN=client"}, new String[] {"CN=client", "O=ACME"}));
  }
}
