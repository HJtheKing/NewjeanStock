package com.sascom.chickenstock.account.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import lombok.extern.slf4j.Slf4j; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec; import java.io.*; import java.nio.charset.StandardCharsets; import java.time.Instant;
@Slf4j @Component
public class HmacAuthFilter extends OncePerRequestFilter {
  @Value("${security.shared-secret}") private String secret;
  @Value("${security.allowed-skew-seconds:300}") private long skew;
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    String path = request.getRequestURI();
    if (path.startsWith("/actuator/health")) { chain.doFilter(request, response); return; }
    CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
    String ts = request.getHeader("X-Timestamp"); String sig = request.getHeader("X-Signature");
    if (ts==null || sig==null) { response.sendError(401, "Missing HMAC headers"); return; }
    try { long t=Long.parseLong(ts); if (Math.abs(Instant.now().getEpochSecond()-t) > skew) { response.sendError(401, "Timestamp skew too large"); return; } } catch (NumberFormatException e) { response.sendError(401, "Bad timestamp"); return; }
    String body = new String(wrapped.body(), StandardCharsets.UTF_8);
    String expect = hmacHex(secret, body + ts);
    if (!slowEquals(expect, sig)) { response.sendError(401, "Invalid signature"); return; }
    chain.doFilter(wrapped, response);
  }
  private static String hmacHex(String key, String data) {
    try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb=new StringBuilder(); for(byte b:out) sb.append(String.format("%02x", b)); return sb.toString(); } catch(Exception e){ throw new RuntimeException(e); }
  }
  private static boolean slowEquals(String a, String b){ if(a==null||b==null) return false; int diff=a.length() ^ b.length(); for(int i=0;i<Math.min(a.length(), b.length());i++) diff |= a.charAt(i)^b.charAt(i); return diff==0; }
}
