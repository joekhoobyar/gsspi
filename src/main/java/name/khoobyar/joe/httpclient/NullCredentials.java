package name.khoobyar.joe.httpclient;

import java.security.Principal;

import org.apache.http.auth.Credentials;

/**
 * An implementation of {@link Credentials} in which all fields are <em>null</em>.
 *
 * @author jkhoobyar
 */
public class NullCredentials
	implements Credentials
{
	@Override public Principal getUserPrincipal() { return null; }
	@Override public String getPassword() { return null; }
}
