package gov.nasa.daveml.dave;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class VectorInfoArrayListTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testVectorInfoArrayList() {
		VectorInfoArrayList vial = new VectorInfoArrayList();
		assertNotNull( vial );
	}

	public void testVectorInfoArrayListInt() {
		VectorInfoArrayList vial = new VectorInfoArrayList(50);
		assertNotNull( vial );
	}

	public void testVectorInfoArrayListCollectionOfVectorInfo() {
		VectorInfo vi1 = new VectorInfo("alpha", "deg", null, false );
		VectorInfo vi2 = new VectorInfo("beta",  "deg", null, false );
		VectorInfo vi3 = new VectorInfo("gamma", "deg", null, false );
		Collection<VectorInfo> c = Arrays.asList( vi1, vi2, vi3 );
		VectorInfoArrayList vial = new VectorInfoArrayList( c );
		assertNotNull( vial );
		assertEquals( 3, vial.size() );
		VectorInfo vi = vial.get(0);
		assertNotNull( vi );
		assertEquals( "alpha", vi.getName() );
		vi = vial.get(1);
		assertNotNull( vi );
		assertEquals( "beta", vi.getName() );
		vi = vial.get(2);
		assertNotNull( vi );
		assertEquals( "gamma", vi.getName() );
	}

}
