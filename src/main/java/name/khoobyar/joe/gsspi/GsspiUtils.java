package name.khoobyar.joe.gsspi;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author jkhoobyar
 */
public abstract class GsspiUtils {
	
	/**
	 * Combines a service and host name into a service principal name,
	 * after resolving the host into a fully-qualified name.
	 * 
	 * @param	service		the service identifier (example: <code>"host"</code>)
	 * @param	host        the host name of the target server
	 * @return	the service principal name
	 * @see		#getQualifiedHostName(String)
	 */
	public static String getServicePrincipalName (String service, String host) {
		StringBuilder sb = new StringBuilder (service).append ('/');
		return sb.append (getQualifiedHostName (host)).toString ();
	}

	/**
	 * Resolves a host name into a fully-qualified name, using {@link InetAddress#getCanonicalHostName()}.
	 * If the resultant canonical name is actually a textual representation of the host <em>address</em>,
	 * this method will return the original, and assumedly unqualifed, host name.
	 * 
	 * @param	host        the host name of the target server
	 * @return	the fully-qualified name or the given host name
	 * @see		InetAddress#getByName(String)
	 * @see		InetAddress#getCanonicalHostName()
	 * @see		InetAddress#getHostAddress()
	 */
	public static String getQualifiedHostName (String host) {
		try {
			InetAddress inetAddress = InetAddress.getByName (host);
			String qualifiedHost = inetAddress.getCanonicalHostName ();
			if (qualifiedHost.equalsIgnoreCase (inetAddress.getHostAddress ()))
				qualifiedHost = host;
			return qualifiedHost;
		}
		catch (UnknownHostException e) {
			throw new UnexpectedGsspiException ("Failed to canonicalize host name: " + host, e);
		}
	}
}
