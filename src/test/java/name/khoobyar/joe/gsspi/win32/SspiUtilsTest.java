package name.khoobyar.joe.gsspi.win32;

import static org.junit.Assert.*;
import static com.sun.jna.platform.win32.W32Errors.SEC_E_OK;
import static name.khoobyar.joe.gsspi.win32.Sspi.*;

import name.khoobyar.joe.gsspi.GsspiUtilsTest;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class SspiUtilsTest
	extends GsspiUtilsTest
{
	private TimeStamp credExpiry = new TimeStamp ();
	private CredHandle hCredentials = null;
	private static String envDnsDomain;
	private static String envUser;
	
	@BeforeClass
	public static void autodetect () {
		envDnsDomain = System.getenv ("USERDNSDOMAIN");
		envUser = System.getenv ("USERNAME");
	}
	
	@After
	public void clean () {
		if (hCredentials != null && !hCredentials.isNull ()) {
			int status = Secur32.INSTANCE.FreeCredentialsHandle (hCredentials);
			if (status != SEC_E_OK)
				throw new UnexpectedSspiException ("SECUR32.FreeCredentialsHandle()", status);
			hCredentials = null;
		}
	}

	@Test
	public void testGetCurrentUserCredentials() {
		acquireKerberosUserCredentials ();
		assertTrue (hCredentials!=null && !hCredentials.isNull ());
		assertTrue (credExpiry!=null && !(credExpiry.dwUpper.intValue()==0 && credExpiry.dwLower.intValue()==0));
	}

	@Test
	public void testGetUserPrincipalName_CredHandle() {
		acquireKerberosUserCredentials ();
		checkCurrentUserPrincipalName (SspiUtils.getUserPrincipalName (hCredentials));
	}

	@Test
	public void testGetCurrentUserPrincipalName() {
		checkCurrentUserPrincipalName (SspiUtils.getCurrentUserPrincipalName ());
	}

	@Test
	public void testConvertUserPrincipalName() {
		String ugly = "EXAMPLE\\jdoe@example.com";
		String nice = SspiUtils.convertUserPrincipalName (ugly);
		assertEquals ("jdoe/example.com", nice);
	}

	protected void checkCurrentUserPrincipalName (String user) {
		assertNotNull ("This test requires the USERDNSDOMAIN environment variable", envDnsDomain);
		assertNotNull ("This test requires the USERNAME environment variable", envUser);
		
		int slash = user.indexOf ('/');
		assertTrue ("expected exactly one slash in \""+user+"\"", slash >= 0 && user.indexOf('/', slash+1) == -1);
		assertEquals (envUser, user.substring (0, slash));
		assertTrue ("expected domain name to match (case-insensitively) \""+envDnsDomain+"\"", envDnsDomain.equalsIgnoreCase (user.substring (slash+1)));
	}

	protected void acquireKerberosUserCredentials () {
		hCredentials = SspiUtils.getCurrentUserCredentials ("Kerberos", SECPKG_CRED_OUTBOUND, credExpiry);
	}
}
