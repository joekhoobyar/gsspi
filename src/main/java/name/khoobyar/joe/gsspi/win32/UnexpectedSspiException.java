package name.khoobyar.joe.gsspi.win32;

import static com.sun.jna.platform.win32.Kernel32Util.formatMessageFromLastErrorCode;
import static com.sun.jna.platform.win32.Kernel32Util.formatMessageFromHR;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HRESULT;

import name.khoobyar.joe.gsspi.UnexpectedGsspiException;

/**
 * @author jkhoobyar
 */
public class UnexpectedSspiException
	extends UnexpectedGsspiException
{
	private static final long serialVersionUID = -564689685282633284L;

	public UnexpectedSspiException() { }
	public UnexpectedSspiException(String message) { super(message); }
	public UnexpectedSspiException(Throwable cause) { super(cause); }
	public UnexpectedSspiException(String message, Throwable cause) { super(message, cause); }
	public UnexpectedSspiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public UnexpectedSspiException(String message, boolean formatLastError) {
		super(formatLastError ? message : message+": "+formatMessageFromLastErrorCode (Kernel32.INSTANCE.GetLastError()));
	}
	
	public UnexpectedSspiException(String message, int ntstatus) {
		super(ntstatus==0 ? message : message+": "+formatMessageFromHR(new HRESULT (ntstatus)));
	}
}
