package name.khoobyar.joe.gsspi.win32;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;

/**A HTTP test of SPNEGO using JGSS.
 * 
 * @author jkhoobyar
 */
public class RunHttpSpnegoJGSS
	extends AbstractRunHttp
{

	@Override
	protected void download (final URL url) throws Exception {
		Authenticator.setDefault (new ConsoleAuthenticator());
		InputStream ins = url.openConnection().getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
		String str;
		while ((str = reader.readLine()) != null)
			System.out.println(str);
	}

	public static void main(String[] args) throws Exception {
		URL loginConfUrl = RunHttpSpnegoJGSS.class.getResource ("/spnego-krb5_login.conf");
		String loginConfPath = loginConfUrl.toExternalForm();
		System.err.println ("Using login config path: "+loginConfPath);
		
		System.setProperty("java.security.auth.login.config", loginConfPath);
		System.setProperty("http.auth.preference", "Kerberos");
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
		//System.setProperty("javax.net.debug", "all");
		
		new RunHttpSpnegoJGSS ().run (args);
	}
	
}
