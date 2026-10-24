package com.worklyn.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
 @Test void rejectsShortSigningSecretAtInitialization() {
  JwtTokenProvider provider=new JwtTokenProvider();
  ReflectionTestUtils.setField(provider,"jwtSecret","too-short");
  ReflectionTestUtils.setField(provider,"jwtExpirationMs",86400000L);
  assertThrows(IllegalStateException.class,provider::initialize);
 }
}
