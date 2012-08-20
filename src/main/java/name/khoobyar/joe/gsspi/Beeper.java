package name.khoobyar.joe.gsspi;

public class Beeper {
	static { NarSystem.loadLibrary(); }
	 
	public native void beep ();
}
