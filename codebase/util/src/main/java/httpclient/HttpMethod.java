package httpclient;

public enum HttpMethod {
	GET("POST"), POST("POST"), PATCH("PATCH");

	private String method;

	private HttpMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}
}
