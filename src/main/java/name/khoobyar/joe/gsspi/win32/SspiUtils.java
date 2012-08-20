package name.khoobyar.joe.gsspi.win32;

import static com.sun.jna.platform.win32.W32Errors.SEC_E_OK;
import static com.sun.jna.platform.win32.W32Errors.SEC_E_SECPKG_NOT_FOUND;
import static com.sun.jna.platform.win32.W32Errors.SEC_I_RENEGOTIATE;
import static com.sun.jna.platform.win32.W32Errors.SEC_I_CONTINUE_NEEDED;
import static com.sun.jna.platform.win32.W32Errors.SEC_I_COMPLETE_AND_CONTINUE;
import static com.sun.jna.platform.win32.W32Errors.SEC_I_COMPLETE_NEEDED;
import static name.khoobyar.joe.gsspi.win32.Sspi.*;
import static name.khoobyar.joe.gsspi.win32.Secur32.*;

import name.khoobyar.joe.gsspi.GsspiUtils;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Secur32Util;
import com.sun.jna.platform.win32.Secur32Util.SecurityPackage;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * SSPI utility functions supporting varous GSS functionality.
 *
 * @author jkhoobyar
 */
public abstract class SspiUtils
	extends GsspiUtils
{
	/**
	 * Requires the given security package to exist, throwing an exception if it doesn't.
	 * 
	 * @throws	UnexpectedSspiException		required security package was not found
	 */
	public static void requireSecurityPackage (String name) {
		for (SecurityPackage pkg : Secur32Util.getSecurityPackages())
			if (pkg.name.equalsIgnoreCase (name))
				return;
		throw new UnexpectedSspiException (name, SEC_E_SECPKG_NOT_FOUND);
	}
	
	/**
	 * Determines the principal name for the user associated with the given credentials handle, by calling
	 * {@link Secur32#QueryCredentialsAttributes(CredHandle, NativeLong, com.sun.jna.Structure)}
	 * and passing the result to {@link #convertUserPrincipalName(String)}.
	 * 
	 * @return	the principal name for the given credentials handle
	 * @throws	UnexpectedSspiException
	 */
	public static String getUserPrincipalName (CredHandle hCredentials) {
		SecPkgContext_Names names = new SecPkgContext_Names ();
		
		int status = Secur32.INSTANCE.QueryCredentialsAttributes (hCredentials, new NativeLong (SECPKG_ATTR_NAMES), names);
			
		// Function must succeed immediately (no continuation codes are acceptable).
		if (status != SEC_E_OK)
    		throw new UnexpectedSspiException ("SECUR32.QueryCredentialsAttributes(type: SECPKG_ATTR_NAMES)", status);
		try { return convertUserPrincipalName (names.getUserName ()); }
		finally { names.dispose (); names = null; }
	}

	/**
	 * Determines the principal name for the user associated with the currently executing thread, by calling
	 * {@link Secur32#GetUserNameEx(int, char[], IntByReference)}
	 * and passing the result to {@link #convertUserPrincipalName(String)}.
	 * 
	 * @return	the principal name for the current user
	 * @throws	UnexpectedSspiException
	 */
	public static String getCurrentUserPrincipalName () {
    	char credNameBuffer[] = new char[255];
    	IntByReference credNameLength = new IntByReference (credNameBuffer.length);
    	
    	if (! Secur32.INSTANCE.GetUserNameEx (EXTENDED_NAME_FORMAT.NameUserPrincipal, credNameBuffer, credNameLength))
    		throw new UnexpectedSspiException ("SECUR32.GetUserNameEx", true);
    	return convertUserPrincipalName (new String (credNameBuffer, 0, credNameLength.getValue ()));
	}
	
	/**
	 * Reformats the given user principal name to follow (de-facto) standard Kerberos naming conventions
	 * and to remove a leading DC domain (if present).
	 * 
	 * @param	user	the principal name for the current user (in a proprietary format)
	 * @return	the principal name for the current user (in a standardized format)
	 */
	public static String convertUserPrincipalName (String user) {
    	int i = user.indexOf ('\\');
    	if (i >= 0)
    		user = user.substring (i + 1);
    	i = user.lastIndexOf ('@');
    	if (i >= 0)
    		user = user.substring (0, i) + "/" + user.substring (i + 1);
    	return user;
	}
	
	/**
	 * Acquires a credentials handle for the user principal associated with the currently executing thread.
	 *
	 * @param sspName		security service package name
	 * @param credUsage		flags specifying how the credentials will be used
	 * @param ctxtExpires	optional output parameter receiving an expiration timestamp for the security context
	 * @return	the credentials handle
	 * @throws	UnexpectedSspiException
	 * @see		{@link #getUserCredentials(String, String, long, com.sun.jna.platform.win32.Sspi.TimeStamp)}
	 */
	public static CredHandle getCurrentUserCredentials (String sspName, long credUsage, TimeStamp credExpires) {
		return getUserCredentials (null, sspName, credUsage, credExpires);
	}
	
	/**
	 * Acquires a credentials handle for the given user.
	 * 
	 * @param user			user name (or user principal name)
	 * @param sspName		security service package name
	 * @param credUsage		flags specifying how the credentials will be used
	 * @param ctxtExpires	optional output parameter receiving an expiration timestamp for the security context
	 * @return	the credentials handle
	 * @throws	UnexpectedSspiException
	 */
	public static CredHandle getUserCredentials (String user, String sspName, long credUsage, TimeStamp credExpires) {
		CredHandle outCredentials = new CredHandle ();
		if (credExpires != null)
			credExpires.clear ();
		
		int status = Secur32.INSTANCE.AcquireCredentialsHandle (user, sspName, new NativeLong (credUsage), null,
				    								            Pointer.NULL, null, null, outCredentials, credExpires);
	
		// Function must succeed immediately (no continuation codes are acceptable).
		if (status != SEC_E_OK)
    		throw new UnexpectedSspiException ("SECUR32.AcquireCredentialsHandle (principal: "+user+")", status);
		return outCredentials;
	}
	
	/**
	 * Initializes a security context for the given input parameters.  On the first call, <em>hContext</em> may be null.
	 * 
	 * @param hCredentials	credentials handle for the user principal
	 * @param hContext		security context handle
	 * @param svcName		service principal name of the target server
	 * @param inAttrs       requested attributes for the security context
	 * @param nativeOrder	whether or not the target server uses native byte order (instead of network byte order)
	 * @param inToken       input token used to initialize the security context
	 * @param outAttrs      optional output parameter indicating the attributes of the established context 
	 * @param ctxtExpires	optional output parameter receiving an expiration timestamp for the security context
	 *
	 * @return	an output token (to be sent to the target server)
	 * @throws	UnexpectedSspiException
	 */
	public static byte[] initSecurityContext (CredHandle hCredentials, CtxtHandle hContext, String svcName,
	                                          long inAttrs, boolean nativeOrder, byte inToken[],
	                                          NativeLongByReference outAttrs, TimeStamp ctxtExpires)
	{
		NativeLong lZero = new NativeLong (), lDataRep = new NativeLong (nativeOrder ? SECURITY_NATIVE_DREP : 0);
		SecBufferDesc outBuffers = new SecBufferDesc (), inBuffers = null;
		
		if (ctxtExpires != null)
			ctxtExpires.clear ();
		if (inToken != null && inToken.length > 0)
			inBuffers = new SecBufferDesc (SECBUFFER_TOKEN, inToken);
		if ((inAttrs & ISC_REQ_ALLOCATE_MEMORY) == 0)
			setSecBuffer (outBuffers.getBuffer(0), SECBUFFER_TOKEN);
		if (outAttrs == null)
			outAttrs = new NativeLongByReference ();

		int status = -1, attrs = 0;
		try {
			status = Secur32.INSTANCE.InitializeSecurityContext (hCredentials, hContext.isNull() ? null : hContext,
					                                             svcName, new NativeLong(inAttrs), lZero, lDataRep,
					                                             inBuffers, lZero, hContext, outBuffers, outAttrs, ctxtExpires);
			attrs = outAttrs.getValue ().intValue ();
			
			// If completion is needed, handle it now.  Otherwise, fail on error.
			if (status == SEC_I_COMPLETE_NEEDED || status == SEC_I_COMPLETE_AND_CONTINUE) {
				int status2 = Secur32.INSTANCE.CompleteAuthToken (hContext, outBuffers);
				if (status2 != SEC_E_OK)
		    		throw new UnexpectedSspiException ("SECUR32.CompleteAuthToken", status);
			} else if (status != SEC_I_CONTINUE_NEEDED && status != SEC_E_OK)
		    	throw new UnexpectedSspiException ("SECUR32.InitializeSecurityContext", status);
			
			// FIXME:  Find another way to signify completion.
			attrs &= ~ISC_RET_INTERMEDIATE_RETURN;
			if (status == SEC_I_CONTINUE_NEEDED || status == SEC_I_COMPLETE_AND_CONTINUE)
				attrs |= ISC_RET_INTERMEDIATE_RETURN;
			outAttrs.setValue (new NativeLong (attrs)); 
			
			return outBuffers.getBytes ();
		}
		catch (RuntimeException e) {
			dispose (hContext, true);
			throw e;
		}
		finally {
			if (0 != (attrs & ISC_RET_ALLOCATED_MEMORY))
				Secur32.INSTANCE.FreeContextBuffer ((Pointer) outBuffers.pBuffers[0].readField ("pvBuffer"));
		}
	}
	
	/**Signs a message for the given security context.
	 * 
	 * @param hContext	security context
	 * @param message	message to be signed
	 * @param sequence	message sequence number, if applicable, otherwise zero.
	 *
	 * @return	the message signature, or <em>null</em> if renegotiation is needed.
	 * @throws	UnexpectedSspiException
	 */
	public static byte[] makeSignature (CtxtHandle hContext, byte[] message, long sequence) {
		SecBufferDesc buffers = new SecBufferDesc (2);
		setSecBuffer (buffers.getBuffer(0), SECBUFFER_DATA, message);
		setSecBuffer (buffers.getBuffer(1), SECBUFFER_TOKEN, getMaxSignatureLength ());
		
		// Sign the message and return the signature unless renegotiation is needed.
		int status = Secur32.INSTANCE.MakeSignature (hContext, new NativeLong (), buffers, new NativeLong (sequence));
		if (status == SEC_I_RENEGOTIATE)
			return null;
		if (status != SEC_E_OK)
    		throw new UnexpectedSspiException ("SECUR32.MakeSignature", status);
		return buffers.getBytes (1);
	}
	
	/**
	 * Attempts to dispose of the given credentials, unless the handle is effectively <em>null</em>.
	 * Unless <em>ignoreFailures</em> is <tt>true</tt>, an {@link UnexpectedSspiException} will be thrown if the attempt fails.
	 * 
	 * @param	hCredentials
	 * @param	ignoreFailures
	 * @see		CredHandle#isNull()
	 * @throws	UnexpectedSspiException		if the handle could not be disposed of and failures are not being ignored
	 */
	public static void dispose (CredHandle hCredentials, boolean ignoreFailures) {
		if (hCredentials==null || hCredentials.isNull ())
			return;
		int status = Secur32.INSTANCE.FreeCredentialsHandle (hCredentials);
		if (status != SEC_E_OK && !ignoreFailures)
    		throw new UnexpectedSspiException ("SECUR32.FreeCredentialsHandle ", status);
	}
	
	/**
	 * Attempts to dispose of the given credentials, unless the handle is effectively <em>null</em>.  Ignores failures.
	 * 
	 * @param	hCredentials
	 * @see		#dispose(com.sun.jna.platform.win32.Sspi.CredHandle, boolean)
	 */
	public static void dispose (CredHandle hCredentials) {
		dispose (hCredentials, true);
	}
	
	/**
	 * Attempts to dispose of the given security context, unless the handle is effectively <em>null</em>.
	 * Unless <em>ignoreFailures</em> is <tt>true</tt>, an {@link UnexpectedSspiException} will be thrown if the attempt fails.
	 * 
	 * @param	hContext
	 * @param	ignoreFailures
	 * @see		CtxtHandle#isNull()
	 * @throws	UnexpectedSspiException		if the handle could not be disposed of and failures are not being ignored
	 */
	public static void dispose (CtxtHandle hContext, boolean ignoreFailures) {
		if (hContext==null || hContext.isNull ())
			return;
		
		int status = Secur32.INSTANCE.DeleteSecurityContext (hContext);
		if (status != SEC_E_OK && !ignoreFailures)
    		throw new UnexpectedSspiException ("SECUR32.DeleteSecurityContext", status);
	}

	/**
	 * Attempts to dispose of the given security context, unless the handle is effectively <em>null</em>.  Ignores failures.
	 * 
	 * @param	hContext
	 * @see		#dispose(com.sun.jna.platform.win32.Sspi.CtxtHandle, boolean)
	 */
	public static void dispose (CtxtHandle hContext) {
		dispose (hContext, true);
	}
	
	/**
	 * @return	the maximum token length (SSPI default)
	 */
	public static long getMaxTokenLength () {
		return MAX_TOKEN_SIZE;
	}
	
	/**
	 * @return	the maximum token length (SSPI default)
	 */
	public static long getMaxSignatureLength () {
		return MAX_TOKEN_SIZE;
	}
	
	public static void setSecBuffer (SecBuffer buffer, int type) {
		setSecBuffer (buffer, type, getMaxTokenLength ());
	}
	
	public static void setSecBuffer (SecBuffer buffer, int type, long size) {
		buffer.BufferType.setValue (type);
		buffer.cbBuffer.setValue (size);
		buffer.pvBuffer = new Memory (size);
		buffer.write();
	}
	
	public static void setSecBuffer (SecBuffer buffer, int type, byte token[]) {
		setSecBuffer (buffer, type, token.length);
		buffer.pvBuffer.write(0, token, 0, token.length);
		buffer.write();
	}
}
