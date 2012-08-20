package name.khoobyar.joe.gsspi.win32;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Base class for manually executed HTTP client tests.
 * 
 *	@author jkhoobyar
 */
public abstract class AbstractRunHttp {

	/** Subclasses must implement the actual downloading. */
	protected abstract void download (final URL url) throws Exception;

	/** Subclasses should call this from main. */
	public void run (String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println ("usage: "+getClass().getSimpleName()+" url");
			System.exit (-1);
		}
		
		URL url = new URL (args[0]);
		
		if ("https".equals (url.getProtocol ())) {
			System.err.println ("Disabling SSL certificate verification.");
			disableSslCertificateVerification ();
		}
		
		System.err.println ("Downloading "+url);
		download (url);
	}

	/** Globally disables SSL certificate verificiation. */
	protected void disableSslCertificateVerification () {
		try {
		    // Create a trust manager that does not validate certificate chains
		    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		        @Override public void checkClientTrusted( final X509Certificate[] chain, final String authType ) { }
		        @Override public void checkServerTrusted( final X509Certificate[] chain, final String authType ) { }
		        @Override public X509Certificate[] getAcceptedIssuers() { return null; }
		    } };
		    
		    // Install the all-trusting trust manager
		    final SSLContext sslContext = SSLContext.getInstance ("SSL");
		    sslContext.init (null, trustAllCerts, new java.security.SecureRandom());
		    
		    // Create an ssl socket factory with our all-trusting manager
		    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		    HttpsURLConnection.setDefaultSSLSocketFactory (sslSocketFactory);
		} catch (Exception e) {
			e.printStackTrace (System.err);
		}
	}

	/** An authenticator that attempts to use the Java console to read a password, falling back on standard input. */
	protected static class ConsoleAuthenticator extends Authenticator {
		private final Console cons;
		private String user;
		private char password[];
		
		public ConsoleAuthenticator(Console cons) {
			this.cons = cons;
			this.user = System.getProperty ("user.name");
		}
		
		public ConsoleAuthenticator () { this (System.console ()); }

		public PasswordAuthentication getPasswordAuthentication () {
			String scheme = getRequestingScheme ();
			
			if (/*scheme.equals ("Negotiate") || */ scheme.equals ("Kerberos")) {
				System.err.println ("Feeding username for " + scheme);
			}
			
			else {
					if (password == null || password.length==0) {
					String host = getRequestingHost ();
					String caption = (host==null ? user : user+"@"+host) + " password: ";
					
					if (cons == null) {
						try {
							System.err.print (caption);
							System.err.flush ();
							password = new BufferedReader (new InputStreamReader (System.in)).readLine ().toCharArray ();
						} catch (Exception e) {
							e.printStackTrace (System.err);
							System.exit (-1);
						} finally {
							System.err.println ();
						}
					} else {
						password = cons.readPassword (caption);
					}
				}
				
				System.err.println ("Feeding username and password for " + scheme);
			}
			
			if (password == null)
				password = new char[0];
			
			return new PasswordAuthentication (user, password);
		}
	}
}
