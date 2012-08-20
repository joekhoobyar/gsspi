package name.khoobyar.joe.httpclient;

import static com.sun.jna.platform.win32.Sspi.SECPKG_CRED_OUTBOUND;
import name.khoobyar.joe.gsspi.win32.SspiUtils;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoScheme;
import org.ietf.jgss.GSSException;

import com.sun.jna.platform.win32.Sspi.CredHandle;
import com.sun.jna.platform.win32.Sspi.CtxtHandle;

public class SspiSpnegoScheme
	extends SPNegoScheme
	implements AuthScheme
{
    private String servicePrincipalName;
    private CredHandle hCredentials;
    private CtxtHandle hContext;
	private boolean initialized = false;
	
	public SspiSpnegoScheme () { }
	public SspiSpnegoScheme (boolean stripPort) { super(stripPort); }
	
	@Override
	protected byte[] generateToken (byte[] input, String authServer)
		throws GSSException
	{
		if (initialized && isComplete ())
			dispose ();
		
		if (! initialized) {
	        servicePrincipalName = SspiUtils.getServicePrincipalName ("http", authServer);
	        hContext = new CtxtHandle ();
	        hCredentials = SspiUtils.getCurrentUserCredentials ("Kerberos", SECPKG_CRED_OUTBOUND, null);
	        initialized = true;
		}
		System.err.println ("generateToken for "+servicePrincipalName);
        return SspiUtils.initSecurityContext (hCredentials, hContext, null, 0, true, input, null, null);
	}
	
	protected void dispose () {
		SspiUtils.dispose (hContext);
		SspiUtils.dispose (hCredentials);
		hContext = null;
		hCredentials = null;
		servicePrincipalName = null;
		initialized = false;
	}
	
	public String getServicePrincipalName () { return servicePrincipalName; }
}
