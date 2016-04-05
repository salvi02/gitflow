package httpclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import exception.RuntimeError;

@ApplicationScoped
public class HttpClient {
	private CloseableHttpClient client;
	private PoolingHttpClientConnectionManager cm;
	private String clientInstanceId;

	public HttpClient() {
	}

	public void initialize(String clientInstanceId, int maxHttpConnection, int maxHttpConnectionPerHost,
			List<String> pemEncodedCerts) throws RuntimeError {
		this.clientInstanceId = clientInstanceId;
		cm = new PoolingHttpClientConnectionManager();
		addPoolConfig(maxHttpConnection, maxHttpConnectionPerHost);
		KeyStore trustStore = buildTrustStore(pemEncodedCerts);
		SSLConnectionSocketFactory sslsf = buildSSLFactory(trustStore);
		client = HttpClients.custom().setConnectionManager(cm).setSSLSocketFactory(sslsf).build();
	}

	public HttpResponse makeHttpRequest(String url, HttpMethod method, String reqJsonStr, Map<String, String> headers)
			throws RuntimeError {
		HttpUriRequest request;
		if (method == HttpMethod.POST) {
			request = createHttpPostMethod(url, reqJsonStr);
		} else {
			request = new HttpGet(url);
		}

		addHeaders(headers, request);
		CloseableHttpResponse httpStatus = doHttpCall(request);
		return createHttpResponse(httpStatus);
	}

	public HttpResponse makeHttpRequest(String url, HttpMethod method, String reqJsonStr, String transactionId)
			throws RuntimeError {
		HttpUriRequest request;
		if (method.equals(HttpMethod.POST)) {
			request = createHttpPostMethod(url, reqJsonStr);
		} else {
			request = new HttpGet(url);
		}

		addInternalHeaders(clientInstanceId, createTimeStamp(), transactionId, request);
		CloseableHttpResponse httpStatus = doHttpCall(request);
		return createHttpResponse(httpStatus);
	}

	public void destroy() {
		if (null != cm) {
			cm.close();
		}
	}

	private KeyStore buildTrustStore(List<String> pemEncodedCerts) throws RuntimeError {
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			addCertificates(pemEncodedCerts, trustStore);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_NO_SUCH_ALGORITHM_ERROR, e);
		} catch (KeyStoreException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_KEY_STORE_EXCEPTION_ERROR, e);
		} catch (CertificateException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_CERTIFICATE_ERROR, e);
		} catch (IOException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_IO_ERROR, e);
		}
		return trustStore;
	}

	private SSLConnectionSocketFactory buildSSLFactory(KeyStore trustStore) throws RuntimeError {
		try {
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
					.build();
			return new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		} catch (KeyManagementException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_KEY_MANANGEMENT_ERROR, e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_NO_SUCH_ALGORITHM_ERROR, e);
		} catch (KeyStoreException e) {
			throw new RuntimeError(ErrorHttpClient.HTTP_KEY_STORE_EXCEPTION_ERROR, e);
		}
	}

	// get cached configuration data
	private void addPoolConfig(int maxHttpConnection, int maxHttpConnectionPerHost) {
		// Increase max total connection to 200 across all hosts(routes)
		cm.setMaxTotal(maxHttpConnection);
		// Increase default max connection per (host)route to 50
		cm.setDefaultMaxPerRoute(maxHttpConnectionPerHost);
	}

	// add all certificates to trust store
	private void addCertificates(List<String> certs, KeyStore trustStore)
			throws CertificateException, IOException, KeyStoreException {
		int i = 0;
		if (certs != null) {
			for (String cert : certs) {
				InputStream stream = new ByteArrayInputStream(cert.getBytes());
				Certificate trustedCert1 = CertificateFactory.getInstance("X.509")
						.generateCertificate(new BufferedInputStream(stream));
				stream.close();
				trustStore.setCertificateEntry("Cert" + i, trustedCert1);
				i++;
			}
		}
	}

	private String createTimeStamp() {
		// TODO Auto-generated method stub
		return null;
	}

	private HttpUriRequest createHttpPostMethod(String url, String reqJsonStr) {
		HttpPost httpPostMethod;
		httpPostMethod = new HttpPost(url);
		StringEntity params = new StringEntity(reqJsonStr, ContentType.DEFAULT_TEXT.getCharset());
		httpPostMethod.setEntity(params);
		return httpPostMethod;
	}

	// add headers to http request
	private void addHeaders(Map<String, String> headers, HttpUriRequest request) {

		if (headers != null) {
			Set<Entry<String, String>> pairs = headers.entrySet();
			Iterator<Entry<String, String>> pairsIteraor = pairs.iterator();
			while (pairsIteraor.hasNext()) {
				Entry<String, String> pair = pairsIteraor.next();
				request.addHeader(pair.getKey(), pair.getValue());
			}
		}
	}

	// execute HTTP call
	private CloseableHttpResponse doHttpCall(HttpUriRequest request) throws RuntimeError {
		CloseableHttpResponse httpStatus = null;
		try {
			httpStatus = client.execute(request);
		} catch (ClientProtocolException e1) {
			throw new RuntimeError(ErrorHttpClient.HTTP_PROTOCOL_ERROR, e1);
		} catch (IOException e1) {
			throw new RuntimeError(ErrorHttpClient.HTTP_IO_ERROR, e1);
		}
		return httpStatus;
	}

	// process http response
	private HttpResponse createHttpResponse(CloseableHttpResponse httpStatus) throws RuntimeError {
		HttpResponse response = new HttpResponse();
		response.setStatusCode(httpStatus.getStatusLine().getStatusCode());
		HttpEntity result = httpStatus.getEntity();
		if (result != null) {
			try {
				response.setResponse(EntityUtils.toString(result, "UTF-8"));
			} catch (ParseException | IOException e) {
				throw new RuntimeError(ErrorHttpClient.HTTP_PARSE_ERROR, e);
			}
		}
		return response;
	}

	private void addInternalHeaders(String clientInstanceId, String timestamp, String transactionId,
			HttpUriRequest request) {

		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "application/json");
		request.addHeader("Client-Instance-Id", clientInstanceId);
		request.addHeader("Timestamp", timestamp);
		request.addHeader("Transaction-Id", transactionId);
	}

}
