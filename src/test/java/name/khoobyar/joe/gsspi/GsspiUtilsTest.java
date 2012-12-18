package name.khoobyar.joe.gsspi;

import static org.junit.Assert.*;

import org.junit.Test;

public class GsspiUtilsTest {

	@Test
	public void testGetServicePrincipalName () {
		String spn = GsspiUtils.getServicePrincipalName ("host", "localhost");
		assertFalse ("host/localhost".equalsIgnoreCase (spn));
		String qhn = GsspiUtils.getQualifiedHostName ("localhost");
		assertEquals ("host/"+qhn, spn);
	}

	@Test
	public void testGetQualifiedHostName () {
		String qhn = GsspiUtils.getQualifiedHostName ("localhost");
		assertFalse ("localhost".equalsIgnoreCase (qhn));
	}

}
