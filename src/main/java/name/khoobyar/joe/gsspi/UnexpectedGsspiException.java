package name.khoobyar.joe.gsspi;

/**	
 *	@author jkhoobyar
 */
public class UnexpectedGsspiException
	extends RuntimeException
{
	private static final long serialVersionUID = 469013689357001034L;

	public UnexpectedGsspiException() { }
	public UnexpectedGsspiException(String message) { super(message); }
	public UnexpectedGsspiException(Throwable cause) { super(cause); }
	public UnexpectedGsspiException(String message, Throwable cause) { super(message, cause); }
}
