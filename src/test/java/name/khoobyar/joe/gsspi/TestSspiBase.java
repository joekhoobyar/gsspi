package name.khoobyar.joe.gsspi;

import name.khoobyar.joe.gsspi.win32.SspiBase;

import org.junit.Test;

public class TestSspiBase {
	
	@Test
	public void test_init () {
		System.out.println("java.library.path="+System.getProperty("java.library.path"));
		SspiBase sspi = new SspiBase();
		sspi.init ();
	}
}
