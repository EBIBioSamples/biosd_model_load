package ac.uk.ebi.fg.biosd.sampletab.persistence;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.model.utils.MSIDumper;
import uk.ac.ebi.fg.biosd.model.utils.test.TestModel;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.organizational.PublicationStatus;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.OntologyEntryDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import uk.ac.ebi.fg.core_model.utils.test.ProcessBasedTestModel;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import uk.ac.ebi.fg.core_model.xref.XRef;
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
		public BioCharacteristicValue cv6;
		public BioCharacteristicValue cv6b;
		public OntologyEntry oe3;
		public BioSample smp7;


		/**
		 * 	<pre>
		 *  smp1(db) -----> smp3 ----> smp4(db) ---> smp6-->smp7
		 *  smp2(db) ----/       \---> smp5 -----/
		 *  
		 *  sg1 contains (1,2,3,7)
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
			smp7 = new BioSample ( prefix + "smp7" );
			
			// These relations are symmetric
			smp3.addDerivedFrom ( smp1 );
			smp2.addDerivedInto ( smp3 );

			smp4.addDerivedFrom ( smp3 );
			smp5.addDerivedFrom ( smp3 );

			smp6.addDerivedFrom ( smp4 );
			smp5.addDerivedInto ( smp6 );
			smp7.addDerivedFrom ( smp6 );
			
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
			// TODO: check
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
			
			cv6 = new BioCharacteristicValue ( "homo-sapiens", ch1 );
	       cv6.addOntologyTerm ( new OntologyEntry ( existingPrefix + "123", new ReferenceSource ( "EFO", null ) ) );
	       cv6.addOntologyTerm ( new OntologyEntry ( prefix + "456", new ReferenceSource ( "MA", null ) ) );
	    smp3.addPropertyValue ( cv6 );

			
			sg1 = new BioSampleGroup ( prefix + "sg1" );
			sg2 = new BioSampleGroup ( existingPrefix + "sg2" );
			
			// Again don't share property values among different owners, you risk integrity-constraint errors and the like.
			// sg2.addPropertyValue ( cv5 );
			cv5b = new BioCharacteristicValue ( "2%", ch3 );
			cv5.setUnit ( percent );
			sg2.addPropertyValue ( cv5b );
			
			cv6b = new BioCharacteristicValue ( "homo-sapiens", ch1 );
      	cv6b.addOntologyTerm ( new OntologyEntry ( existingPrefix + "123", new ReferenceSource ( "EFO", null ) ) );
      	cv6b.addOntologyTerm ( oe3 = new OntologyEntry ( prefix + "789", new ReferenceSource ( "MA", null ) ) );
			smp7.addPropertyValue ( cv6b );
			
			
			sg1.addSample ( smp1 );
			smp2.addGroup ( sg1 );
			sg1.addSample ( smp3 );
			sg1.addSample ( smp7 );
			
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
			msi.addSample ( smp7 );

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
		TestModel m1 = new TestModel ( "test1." );
		MyTestModel m2 = new MyTestModel ( "test2.", "test1." );

		Publication pub = new Publication ( "test2.123", null );
		pub.setTitle ( "A test publication" );
		
		PublicationStatus pubStat = new PublicationStatus ( "test.status" );
		pub.setStatus ( pubStat );
		
		ReferenceSource xsrc = new ReferenceSource ( "test2.456", "1.0" );
		XRef xr = new XRef ( "test2.789", xsrc );
		pub.addReference ( xr );
		
		m2.msi.addPublication ( pub );
		
		Persister persister = new Persister ();
		persister.persist ( m1.msi );
		
		// Verify update dates
		Date sg2Date = m1.sg2.getUpdateDate ();
		assertNotNull ( "sg2.updateDate not set!", sg2Date );
		
		persister.persist ( m2.msi );

		EntityManager em = emProvider.getEntityManager ();
		ProcessBasedTestModel.verifyTestModel ( em, m1, true );
		ProcessBasedTestModel.verifyTestModel ( em, m2, true );
		// TODO: check non-graph objects too. 
		
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class, em );
		MSI msi2DB = dao.find ( m2.msi.getAcc () );
		
		// Avoid to mix the output with logs 
		// 
		StringWriter sw = new StringWriter ();
		PrintStream out = new PrintStream ( new WriterOutputStream ( sw ) );
		MSIDumper.dump ( out, msi2DB );
		out.flush ();
		System.out.println ( "\n\n\n_________ Second Submission Reloaded:\n" + sw + "\n" );
		
		 /* smp1(db) -----> smp3 ----> smp4(db) ---> smp6 --->smp7
		 *  smp2(db) ----/       \---> smp5 -----/
		 *  
		 *  sg1 contains (1,2,3,7)
		 *  sg2(db) contains (3,4,5,6) 
		 */
		assertTrue ( "Reloaded m2 doesn't contain smp1->msi!", msi2DB.getSamples ().contains ( m1.smp1 ) );
		assertTrue ( "Reloaded m1 doesn't contain smp1->msi!", msi2DB.getSamples ().contains ( m1.smp2 ) );
		assertTrue ( "Reloaded m1 doesn't contain smp1->msi!", msi2DB.getSamples ().contains ( m1.smp4 ) );

		assertTrue ( "Reloaded m2 doesn't contain sg1->msi!", m2.msi.getSampleGroups ().contains ( m2.sg1 ) );
		assertTrue ( "Reloaded m2 doesn't contain sg2->msi!", m2.msi.getSampleGroups ().contains ( m2.sg2 ) );


		BioSample smp3 = null, smp4 = null, smp7 = null; 
		for ( BioSample smp: msi2DB.getSamples () )
			if ( "test2.smp3".equals ( smp.getAcc () ) ) smp3 = smp;
			else if ( "test1.smp4".equals ( smp.getAcc () ) ) smp4 = smp;
			else if ( "test2.smp7".equals ( smp.getAcc () ) ) smp7 = smp;
		
		assertNotNull ( "test2.smp3 not found in reloaded model!", smp3 );
		assertNotNull ( "test1.smp4 not found in reloaded model!", smp4 );
		
		assertTrue ( "Wrong derived-to relation in reloaded m2!", smp3.getDerivedFrom ().contains ( m1.smp1 ) );
		assertTrue ( "Wrong derived-to relation in reloaded m2!", smp4.getDerivedInto ().contains ( m2.smp6 ) );

		assertTrue ( "Wrong derived-to relation in reloaded m2!", m2.smp6.getDerivedInto ().contains ( smp7 ) );
		assertTrue ( "Wrong derived-to relation in reloaded m2!", smp7.getDerivedFrom ().contains ( m2.smp6 ) );

		assertTrue ( "Reloaded m2 doesn't contain smp1->sg1!", m2.sg1.getSamples ().contains ( m1.smp1 ) );
		assertTrue ( "Reloaded m2 doesn't contain smp7->sg1!", m2.sg1.getSamples ().contains ( smp7 ) );

		
		BioSampleGroup sg2 = null;
		for ( BioSampleGroup sg: msi2DB.getSampleGroups () )
			if ( "test1.sg2".equals ( sg.getAcc () ) ) { 
				sg2 = sg; break;
		}
		
		assertNotNull ( "test1.sg2 not found in reloaded model!", sg2 );
		assertTrue ( "Reloaded m2 doesn't contain smp6->sg2!", sg2.getSamples ().contains ( m2.smp6 ) );

		// Test update dates
		assertNotNull ( "sg2.updateDate not set!", m2.sg2.getUpdateDate () );
		assertTrue ( "sg2.updateDate not changed!", sg2Date.compareTo ( m2.sg2.getUpdateDate () ) < 0 );
		
		Publication pubDB = msi2DB.getPublications ().iterator ().next ();
		PublicationStatus pubStatDB = pubDB.getStatus ();
		XRef xrDB = pubDB.getReferences ().iterator ().next ();
		ReferenceSource srcDB = xrDB.getSource ();

		long pubId = pubDB.getId (), xrId = xrDB.getId (), srcId = srcDB.getId ();
		
		assertNotNull ( "Test Pub Status not saved!", pubStatDB );
		
		// Test update dates
		assertNotNull ( "MSI's update date not set!", msi2DB.getUpdateDate () );
		assertNotNull ( "sg1 update date not set!", m2.sg1.getUpdateDate () );
		
		// Verify the job register
		JobRegisterDAO jr = new JobRegisterDAO ( em );
		assertTrue ( "Unload log didn't work for smp3!", jr.hasEntry ( msi2DB, 1, Operation.ADD ) );
		assertTrue ( "Unload log didn't work for smp7!", jr.hasEntry ( smp7, 1, Operation.ADD ) );
		assertTrue ( "Unload log didn't work for sg1!", jr.hasEntry ( m2.sg1, 1, Operation.UPDATE ) );
		assertTrue ( "Unload log didn't work for sg1!", jr.hasEntry ( m2.sg1, 1, Operation.ADD ) );

		
		
		// ------------------------------ Unloading Test --------------------------------------
		// 
		
		Unloader unloader = new Unloader ();
		unloader.unload ( msi2DB );
		
		em = emProvider.newEntityManager ();
		
		AccessibleDAO<BioSample> sampleDao = new AccessibleDAO<BioSample> ( BioSample.class, em );
		assertTrue ( "Unloading removed smp3!", sampleDao.contains ( smp3.getAcc () ) );
		assertTrue ( "Unloading removed smp1!", sampleDao.contains ( m1.smp1.getAcc () ) );
		assertFalse ( "Unloading didn't remove smp7!", sampleDao.contains ( smp7.getAcc () ) );
		
		OntologyEntryDAO<OntologyEntry> oeDao = new OntologyEntryDAO<OntologyEntry> ( OntologyEntry.class, em );
		assertFalse ( "Unloading of oe3 failed!", oeDao.contains ( m2.oe3.getAcc (), m2.oe3.getSource ().getAcc (), m2.oe3.getSource ().getVersion () ) );
		assertTrue ( "Unloading removed oe1!", oeDao.contains ( m1.oe1.getAcc (), m1.oe1.getSource ().getAcc (), m1.oe1.getSource ().getVersion () ) );
		
		IdentifiableDAO<Identifiable> idDao = new IdentifiableDAO<Identifiable> ( Identifiable.class, em );
		assertFalse ( "Test Xref not deleted!", idDao.contains ( xrId, XRef.class ) );
		assertFalse ( "Test Pub not deleted!", idDao.contains ( pubId, Publication.class ) );
		assertFalse ( "Test Ref Source not deleted!", idDao.contains ( srcId, ReferenceSource.class ) );
		assertFalse ( "Test Pub Status not deleted!", 
			new CVTermDAO<PublicationStatus> ( PublicationStatus.class, em ).contains ( pubStat.getName () ) );
		
		// Verifies unloading log
		// 
		em = emProvider.newEntityManager ();
		jr = new JobRegisterDAO ( em );

		assertTrue ( "Unload log didn't work for smp3!", jr.hasEntry ( msi2DB, 1 ) );
		assertTrue ( "Unload log didn't work for smp7!", jr.hasEntry ( smp7, 1 ) );
		assertTrue ( "Unload log didn't work for sg1!", jr.hasEntry ( m2.sg1, 1 ) );
		
		int szAll = jr.find ( 1 ).size ();
		assertTrue ( "JobRegisterDAO.find() doesn't work!", szAll >= 3 );
		assertTrue ( "JobRegisterDAO.find() doesn't work!", jr.find ( 1, Operation.DELETE ).size () < szAll );

	}
}
