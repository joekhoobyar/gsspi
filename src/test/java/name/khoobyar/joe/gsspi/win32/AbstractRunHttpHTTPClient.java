package name.khoobyar.joe.gsspi.win32;

import java.net.URL;
import java.security.cert.X509Certificate;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Base support for testing with HTTPClient.
 *
 * @author jkhoobyar
 */
public abstract class AbstractRunHttpHTTPClient
	extends AbstractRunHttp
{
	private boolean verifySslCertificates = true;
	
	/** Subclasses should override in order to configure the HTTP client. */
	protected DefaultHttpClient getHttpClient () throws Exception {
		DefaultHttpClient client = new DefaultHttpClient ();
		
		// Configure SSL, if need be.
		if (! verifySslCertificates) {
			SchemeRegistry registry = client.getConnectionManager ().getSchemeRegistry ();
			TrustStrategy strategy = new TrustStrategy () {
				public boolean isTrusted (X509Certificate[] chain, String authType) { return true; }
			};
			SSLSocketFactory socketFactory = new SSLSocketFactory (strategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			registry.register (new Scheme("https", 443, socketFactory));
		}
		
		return client;
	}

	@Override
	protected void disableSslCertificateVerification () {
		super.disableSslCertificateVerification ();
		verifySslCertificates = false;
	}

	@Override
	protected void download(URL url) throws Exception {
		DefaultHttpClient client = getHttpClient ();
		try {
	        HttpUriRequest request = new HttpGet (url.toURI ());
	        HttpResponse response = client.execute (request);
	        HttpEntity entity = response.getEntity ();
	
	        System.out.println("----------------------------------------");
	        System.out.println(response.getStatusLine ());
	        System.out.println("----------------------------------------");
	        if (entity != null)
	            System.out.println(EntityUtils.toString (entity));
	        System.out.println("----------------------------------------");
	
	        // This ensures the connection gets released back to the manager
	        EntityUtils.consume (entity);
	
	    } finally {
	        // When HttpClient instance is no longer needed,
	        // shut down the connection manager to ensure
	        // immediate deallocation of all system resources
	        client.getConnectionManager().shutdown();
	    }
	}

}