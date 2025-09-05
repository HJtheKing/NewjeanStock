package com.sascom.chickenstock.portfolio.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.*; 
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
  private final byte[] cachedBody;
  public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException { super(request); this.cachedBody = request.getInputStream().readAllBytes(); }
  @Override public ServletInputStream getInputStream() {
    ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
    return new ServletInputStream() {
      @Override public boolean isFinished() { return bais.available()==0; }
      @Override public boolean isReady() { return true; }
      @Override public void setReadListener(ReadListener readListener) { }
      @Override public int read() throws IOException { return bais.read(); }
    };
  }
  @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream())); }
  public byte[] body() { return cachedBody; }
}
