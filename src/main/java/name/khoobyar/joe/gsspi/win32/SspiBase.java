package name.khoobyar.joe.gsspi.win32;

public class SspiBase {
	static { NarSystem.loadLibrary(); }
	 
	public native void init ();
}
