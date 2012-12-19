package name.khoobyar.joe.gsspi.win32;

import java.lang.reflect.Method;

public class SspiBase {
	static {
		Class<?> narSystem;
		try { narSystem = Class.forName("name.khoobyar.joe.gsspi.NarSystem"); }
		catch (ClassNotFoundException e) { throw new RuntimeException (e); }
		
		Method loadLibrary;
		try { loadLibrary = narSystem.getMethod ("loadLibrary"); }
		catch (Exception e) { throw new RuntimeException (e); }
		
		try { loadLibrary.invoke (null); }
		catch (Exception e) { throw new RuntimeException (e); }
	}
	 
	public native void init ();
}
