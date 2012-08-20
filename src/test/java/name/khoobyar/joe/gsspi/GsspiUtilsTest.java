package name.khoobyar.joe.gsspi;

import static org.junit.Assert.*;

import org.junit.Test;

public class GsspiUtilsTest {

	@Test
	public void testGetServicePrincipalName () {
		String spn = GsspiUtils.getServicePrincipalName ("host", "rst-etldev1");
		assertFalse ("host/rst-etldev1".equalsIgnoreCase (spn));
		String qhn = GsspiUtils.getQualifiedHostName ("rst-etldev1");
		assertEquals ("host/"+qhn, spn);
	}

	@Test
	public void testGetQualifiedHostName () {
		String qhn = GsspiUtils.getQualifiedHostName ("rst-etldev1");
		assertFalse ("rst-etldev1".equalsIgnoreCase (qhn));
	}

}
