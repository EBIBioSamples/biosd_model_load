/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational.MSINormalizer;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;

/**
 * Tests normalisation of duped objects after parsing.
 *
 * <dl><dt>date</dt><dd>Mar 13, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class NormalizationTest
{
	
	/**
	 * A modified version of {@link TestModel}, which re-use some items from an existing submission.
	 *
	 */
	public static class MyTestModel extends TestModel
	{	
		/**
		 * 	<pre>
		 *  smp1(db) -----> smp3 ----> smp4(db) ---> smp6
		 *  smp2(db) ----/       \---> smp5 -------> smp6b
		 *  
		 *  smp6b is equivalent to smp6
		 *  cv2 has a duped OE
		 *  the contact role in contact is duped in cnt1
		 *  </pre>        
		 */
		
		public BioSample smp6b;
		public Contact cnt1;
		public OntologyEntry oe1b;
		public ContactRole cntRole1b;
		
		public MyTestModel ( String prefix )
		{
			super ();

			smp6b = new BioSample ( prefix + "smp6" );
			smp5.addDerivedInto ( smp6b );
			
			// Re-add the same OE to a different CV
	    cv2.addOntologyTerm ( oe1b = new OntologyEntry ( oe1.getAcc (), oe1.getSource () ) );
			
	    cnt1 = new Contact ();
			cnt1.setFirstName ( prefix + "Mister" );
			cnt1.setLastName ( prefix + "Test" );
			// Add the same contact role as before
			Collections.addAll ( cnt1.getContactRoles (), cntRole1b = new ContactRole ( cntRole1.getName () ) );
			msi.addContact ( cnt1 );
		}
	}	
	
	/**
	 * Performs a basic test using instances of {@link TestModel} and {@link MyTestModel}.
	 */
	@Test
	public void testMockupModels () throws Exception
	{
		MyTestModel m = new MyTestModel ( "test." );

		Store store = new MemoryStore ();
		new MSINormalizer ( store ).normalize ( m.msi );

		assertNotNull ( "cntRole1 not found in the store!", store.find ( m.cntRole1b, m.cntRole1b.getName () ) );
		
		assertNotNull ( "oe1 not found in the store!", 
			store.find ( m.oe1b, m.oe1b.getAcc (), m.oe1b.getSource ().getAcc (), m.oe1b.getSource ().getVersion (), m.oe1b.getSource ().getUrl () ) );

		assertTrue ( "Contact Role Normalisation failed!", 
			m.cnt.getContactRoles ().iterator ().next () == m.cnt1.getContactRoles ().iterator ().next () );
		
		Iterator<OntologyEntry> cv1Itr = m.cv1.getOntologyTerms ().iterator ();
		OntologyEntry cv2OE = m.cv2.getOntologyTerms ().iterator ().next ();
		assertTrue ( "OE1 Normalisation failed!", cv2OE == cv1Itr.next () || cv2OE == cv1Itr.next () );
	}
}
