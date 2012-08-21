package name.khoobyar.joe.gsspi;

import org.junit.Test;

public class TestBeeper {
	@Test
	public void testBeeper() {
		System.out.println("java.library.path="+System.getProperty("java.library.path"));
		Beeper beeper = new Beeper();
		beeper.beep();
	}
}
