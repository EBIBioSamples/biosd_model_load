/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Collections;

import javax.persistence.EntityManager;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.utils.MSIDumper;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.utils.test.ProcessBasedTestModel;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

/**
 * Performs some test about re-using objects that already exist in the DB.
 *
 * <dl><dt>date</dt><dd>Jan 15, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class LoadExistingEntitiesTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );
	
	/**
	 * 
	 * A modified version of {@link TestModel}, which re-use some items from an existing submission.
	 *
	 * <dl><dt>date</dt><dd>Jan 16, 2013</dd></dl>
	 * @author Marco Brandizi
	 *
	 */
	public static class MyTestModel extends TestModel
	{	
		/**
		 * 	<pre>
		 *  smp1(db) -----> smp3 ----> smp4(db) ---> smp6
		 *  smp2(db) ----/       \---> smp5 -----/
		 *  
		 *  sg1 contains (1,2,3)
		 *  sg2(db) contains (3,4,5,6) 
		 *  </pre>        
		 */
		
		public MyTestModel ( String prefix, String existingPrefix )
		{
			smp1 = new BioSample ( existingPrefix + "smp1" );
			smp2 = new BioSample ( existingPrefix + "smp2" );
			smp3 = new BioSample ( prefix + "smp3" );
			smp4 = new BioSample ( existingPrefix + "smp4" );
			smp5 = new BioSample ( prefix + "smp5" );
			smp6 = new BioSample ( prefix + "smp6" );
			
			// These relations are symmetric
			smp3.addDerivedFrom ( smp1 );
			smp2.addDerivedInto ( smp3 );

			smp4.addDerivedFrom ( smp3 );
			smp5.addDerivedFrom ( smp3 );

			smp6.addDerivedFrom ( smp4 );
			smp5.addDerivedInto ( smp6 );
			
			ch1 = new BioCharacteristicType ( "Organism" );
			cv1 = new BioCharacteristicValue ( "mus-mus", ch1 );
	        cv1.addOntologyTerm ( new OntologyEntry ( existingPrefix + "123", new ReferenceSource ( "EFO", null ) ) );
	        cv1.addOntologyTerm ( new OntologyEntry ( existingPrefix + "456", new ReferenceSource ( "MA", null ) ) );
			smp1.addPropertyValue ( cv1 );
			
			ch2 = new BioCharacteristicType ();
			ch2.setTermText ( "Age" );
			cv2 = new BioCharacteristicValue ();
			cv2.setTermText ( "10" );
			cv2.setType ( ch2 );
			timeDim = new UnitDimension ( "time" );
			monthsUnit = new Unit ( "months", timeDim );
			cv2.setUnit ( monthsUnit );
			smp1.addPropertyValue ( cv2 );

			// Cannot be re-used, you need to create a new one, even if it is the same
			cv3 = new BioCharacteristicValue ( "mus-mus", ch1 );
			smp2.addPropertyValue ( cv3 );
			
			cv4 = new BioCharacteristicValue ( "8", ch2 );
			// Units can be recycled instead
			cv4.setUnit ( monthsUnit );
			smp2.addPropertyValue ( cv4 ); 
			
			
			ch3 = new BioCharacteristicType ();
			ch3.setTermText ( "concentration" );
			cv5 = new BioCharacteristicValue ( "2%", ch3 );
			concentrationUnit = new UnitDimension ( "Concentration" );
			percent = new Unit ( "Percentage", concentrationUnit );
			cv5.setUnit ( percent );
			
			smp4.addPropertyValue ( cv5 );
			
			
			sg1 = new BioSampleGroup ( prefix + "sg1" );
			sg2 = new BioSampleGroup ( existingPrefix + "sg2" );
			// Likely you won't share property values over multiple owners, but it is possible
			sg2.addPropertyValue ( cv5 );
			
			sg1.addSample ( smp1 );
			smp2.addGroup ( sg1 );
			sg1.addSample ( smp3 );
			
			sg2.addSample ( smp4 );
			sg2.addSample ( smp5 );
			sg2.addSample ( smp6 );
			smp3.addGroup ( sg2 ); // same sample in two groups
			
			msi = new MSI ( prefix + "msi1" );
			
			cnt = new Contact ();
			cnt.setFirstName ( prefix + "Mister" );
			cnt.setLastName ( prefix + "Test" );
			cnt.setContactRoles ( Collections.singleton ( new ContactRole ( existingPrefix + "test-submitter" ) ) );
			
			msi.addSample ( smp1 );
			msi.addSample ( smp2 );
			msi.addSample ( smp3 );
			msi.addSample ( smp4 );
			msi.addSample ( smp5 );
			msi.addSample ( smp6 );

			msi.addSampleGroup ( sg1 );
			msi.addSampleGroup ( sg2 );
		}
	}	
	
	/**
	 * Performs a basic test using instances of {@link TestModel} and {@link MyTestModel}.
	 */
	@Test
	public void testMockupModels () throws Exception
	{
		TestModel m1 = new TestModel ( "test1." ), m2 = new MyTestModel ( "test2.", "test1." );
		new Persister ( m1.msi ).persist ();
		new Persister ( m2.msi ).persist ();

		EntityManager em = emProvider.getEntityManager ();
		ProcessBasedTestModel.verifyTestModel ( em, m1, true );
		ProcessBasedTestModel.verifyTestModel ( em, m2, true );
		// TODO: check non-graph objects too. 
		
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class, em );
		MSI msi = dao.find ( m2.msi.getAcc () );
		
		// Avoid to mix the output with logs 
		// 
		StringWriter sw = new StringWriter ();
		PrintStream out = new PrintStream ( new WriterOutputStream ( sw ) );
		MSIDumper.dump ( out, msi );
		out.flush ();
		System.out.println ( "\n\n\n_________ Second Submission Reloaded:\n" + sw + "\n" );
		
		 /* smp1(db) -----> smp3 ----> smp4(db) ---> smp6
		 *  smp2(db) ----/       \---> smp5 -----/
		 *  
		 *  sg1 contains (1,2,3)
		 *  sg2(db) contains (3,4,5,6) 
		 */
		assertTrue ( "Reloaded m2 doesn't contain smp1->msi!", msi.getSamples ().contains ( m1.smp1 ) );
		assertTrue ( "Reloaded m1 doesn't contain smp1->msi!", msi.getSamples ().contains ( m1.smp2 ) );
		assertTrue ( "Reloaded m1 doesn't contain smp1->msi!", msi.getSamples ().contains ( m1.smp4 ) );

		assertTrue ( "Reloaded m2 doesn't contain sg1->msi!", m2.msi.getSampleGroups ().contains ( m2.sg1 ) );
		assertTrue ( "Reloaded m2 doesn't contain sg2->msi!", m2.msi.getSampleGroups ().contains ( m2.sg2 ) );

		BioSample smp3 = null, smp4 = null; 
		for ( BioSample smp: msi.getSamples () )
			if ( "test2.smp3".equals ( smp.getAcc () ) ) 
				smp3 = smp;
			else if ( "test1.smp4".equals ( smp.getAcc () ) )
				smp4 = smp;
		
		assertNotNull ( "test2.smp3 not found in reloaded model!", smp3 );
		assertNotNull ( "test1.smp4 not found in reloaded model!", smp4 );
		
		assertTrue ( "Wrong derived-to relation in reloaded m2!", smp3.getDerivedFrom ().contains ( m1.smp1 ) );
		assertTrue ( "Wrong derived-to relation in reloaded m2!", smp4.getDerivedInto ().contains ( m2.smp6 ) );
		
		assertTrue ( "Reloaded m2 doesn't contain smp1->sg1!", m2.sg1.getSamples ().contains ( m1.smp1 ) );

		BioSampleGroup sg2 = null;
		for ( BioSampleGroup sg: msi.getSampleGroups () )
			if ( "test1.sg2".equals ( sg.getAcc () ) ) { 
				sg2 = sg; break;
		}
		
		assertNotNull ( "test1.sg2 not found in reloaded model!", sg2 );
		assertTrue ( "Reloaded m2 doesn't contain smp6->sg2!", sg2.getSamples ().contains ( m2.smp6 ) );
	}
}
