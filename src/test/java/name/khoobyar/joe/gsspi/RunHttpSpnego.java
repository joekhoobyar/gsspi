package name.khoobyar.joe.gsspi;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
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

/**
 * @author jkhoobyar
 * 
 */
public class RunHttpSpnego {

	static class MyAuthenticator extends Authenticator {
		private final Console cons;
		private String user;
		private char password[];
		
		public MyAuthenticator(Console cons) {
			this.cons = cons;
			this.user = System.getProperty("user.name");
		}
		
		public MyAuthenticator() { this(System.console ()); }

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

	public static void main(String[] args) throws Exception {
		URL loginConfUrl = RunHttpSpnego.class.getResource ("/spnego-krb5_login.conf");
		String loginConfPath = loginConfUrl.toExternalForm();
		System.err.println ("Using login config path: "+loginConfPath);
		
		System.setProperty("java.security.auth.login.config", loginConfPath);
		System.setProperty("http.auth.preference", "Kerberos");
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "true");
		System.setProperty("javax.net.debug", "all");
			
		if (args.length < 1) {
			System.err.println ("usage: RunHttpSpnego url");
			System.exit (-1);
		}
		
		final URL url = new URL(args[0]);
		System.err.println ("Will try to download: "+url);
		
		if ("https".equals (url.getProtocol())) {
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
		
		Authenticator.setDefault (new MyAuthenticator());
		InputStream ins = url.openConnection().getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		String str;
		while ((str = reader.readLine()) != null)
			System.out.println(str);
	}
}
