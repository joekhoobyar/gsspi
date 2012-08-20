package name.khoobyar.joe.gsspi.win32;

import name.khoobyar.joe.httpclient.NullCredentials;
import name.khoobyar.joe.httpclient.SspiSpnegoScheme;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

public class RunHttpSpnegoHTTPClient
	extends AbstractRunHttpHTTPClient
{
	public static void main(String[] args) throws Exception {
		new RunHttpSpnegoHTTPClient ().run (args);
	}

	@Override
	protected DefaultHttpClient getHttpClient () throws Exception {
		DefaultHttpClient client = super.getHttpClient ();

		// Register a customized auth scheme.
        client.getAuthSchemes ().register (AuthPolicy.SPNEGO, new SPNegoSchemeFactory() {
			@Override public AuthScheme newInstance (HttpParams params) {
				return new SspiSpnegoScheme (isStripPort ());
			}
        });
        
        // Setup empty credentials and auth scope.
        client.getCredentialsProvider ().setCredentials(new AuthScope (null, -1, null), new NullCredentials ());		
        
		return client;
	}
}
