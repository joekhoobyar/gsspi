package name.khoobyar.joe.gsspi.win32;

import static name.khoobyar.joe.gsspi.win32.Sspi.*;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author jkhoobyar
 *
 */
public interface Secur32
	extends com.sun.jna.platform.win32.Secur32
{
    Secur32 INSTANCE = (Secur32) Native.loadLibrary("Secur32", Secur32.class, W32APIOptions.UNICODE_OPTIONS);

	/** Win32 API function (see MSDN for details) */
	int CompleteAuthToken (CtxtHandle phContext, SecBufferDesc pToken);

	/** Win32 API function (see MSDN for details) */
	int MakeSignature (CtxtHandle phContext, NativeLong fQOP, SecBufferDesc pToken, NativeLong messageSeqNo);

	/** Win32 API function (see MSDN for details) */
	int QueryCredentialsAttributes (CredHandle hCredentials, NativeLong ulAttr, Structure pResult);

	/** Win32 API function (see MSDN for details) */
	int QueryContextAttributes (CtxtHandle hContext, NativeLong ulAttr, Structure pResult);
}
