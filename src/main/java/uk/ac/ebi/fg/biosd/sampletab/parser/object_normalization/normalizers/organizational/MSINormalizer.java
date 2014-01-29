package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.MemoryStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.BioSampleNormalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.ProductComparator;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.Organization;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * This is the root normaliser. A submission is parsed, the resulting {@link MSI} is passed to this class with an 
 * instance of {@link MemoryStore} and re-passed to this class again, this time with an instance of {@link DBStore}.  
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSINormalizer extends AnnotatableNormalizer<MSI>
{
  private Logger log = LoggerFactory.getLogger ( getClass() );
  
  private final ContactNormalizer contactNormalizer;
	private final OrganizationNormalizer organizationNormalizer;
	private final PublicationNormalizer publicationNormalizer;
	
	private final BioSampleNormalizer sampleNormalizer;
	private final BioSampleGroupNormalizer sgNormalizer;

	
	private ProductComparator smpCmp = new ProductComparator ();
	private BioSampleGroupComparator sgCmp = new BioSampleGroupComparator ();


	public MSINormalizer ( Store store ) 
	{
		super ( store );
	  contactNormalizer = new ContactNormalizer ( store );
		organizationNormalizer = new OrganizationNormalizer ( store );
		publicationNormalizer = new PublicationNormalizer ( store );
		sampleNormalizer = new BioSampleNormalizer ( store );
		sgNormalizer = new BioSampleGroupNormalizer ( store );
	}

	/**
	 * Starts normalisation propagating to the objects linked to the submission.
	 */
	@Override
	public boolean normalize ( MSI msi )
	{
		if ( msi == null ) {
			log.warn ( "Internal issue: MSI Peristence Listener got a null submission and that smells like a code bug" );
			return false;
		}
		if ( !super.normalize ( msi ) ) return false;
		
		for ( Contact contact: msi.getContacts () ) contactNormalizer.normalize ( contact );
		for ( Organization org: msi.getOrganizations () ) organizationNormalizer.normalize ( org );
		for ( Publication pub: msi.getPublications () ) publicationNormalizer.normalize ( pub );

		normalizeDatabaseRecordRefs ( this.store, msi.getDatabaseRecordRefs () );
		normalizeReferenceSources ( this.store, msi.getReferenceSources () );

		for ( BioSample sample: msi.getSamples () ) sampleNormalizer.normalize ( sample );
		for ( BioSampleGroup sg: msi.getSampleGroups () ) sgNormalizer.normalize ( sg );

		linkExistingSamples ( msi );
		linkExistingSampleGroups ( msi );	

		// mark the time the object creation occurs 
		if ( store instanceof DBStore ) 
		{ 
			msi.setUpdateDate ( new Date () );
			JobRegisterDAO jrDao = new JobRegisterDAO ( ((DBStore) store).getEntityManager () );
			jrDao.create ( msi, Operation.ADD, msi.getUpdateDate () );
			
			for ( BioSample sample: msi.getSamples () ) 
				if ( sample.getId () == null ) jrDao.create ( sample, Operation.ADD, sample.getUpdateDate () );
			for ( BioSampleGroup sg: msi.getSampleGroups () )  
				if ( sg.getId () == null ) jrDao.create ( sg, Operation.ADD, sg.getUpdateDate () );
		}
		
		return true;
	}
	
	/**
	 * Removes those ref sources that already exists in store and replaces them with the existing
	 * ones.
	 */
	public static void normalizeReferenceSources ( Store store, Set<ReferenceSource> sources )
	{
  	Set<ReferenceSource> addSrcs = new HashSet<ReferenceSource> (), delSrcs = new HashSet<ReferenceSource> ();
  	for ( ReferenceSource source: sources )
  	{
  		if ( source.getId () != null ) continue;
  		
  		ReferenceSource srcS = store.find ( source, source.getAcc (), source.getVersion () );
  		if ( srcS == null || source == srcS ) continue;
 
  		delSrcs.add ( source ); addSrcs.add ( srcS );
  	}
  	
  	sources.removeAll ( delSrcs ); sources.addAll ( addSrcs );
	}

	/**
	 * Removes those {@link DatabaseRecordRef}s that already exist in the store and replaces them with the existing ones.
	 */
	public static void normalizeDatabaseRecordRefs ( Store store, Set<DatabaseRecordRef> sources )
	{
  	Set<DatabaseRecordRef> addSrcs = new HashSet<DatabaseRecordRef> (), delSrcs = new HashSet<DatabaseRecordRef> ();
  	for ( DatabaseRecordRef source: sources )
  	{
  		if ( source.getId () != null ) continue;
  		
  		DatabaseRecordRef srcS = store.find ( source, source.getDbName (), source.getAcc (), source.getVersion () );
  		if ( srcS == null || source == srcS ) continue;
 
  		delSrcs.add ( source ); addSrcs.add ( srcS );
  	}
  	
  	sources.removeAll ( delSrcs ); sources.addAll ( addSrcs );
	}

	
	
	
	/**
	 * Replace those MSI's samples that have an equivalent in the DB with such equivalent. Uses {@link ProductComparator}
	 * for deciding such equivalence.
	 * 
	 */
	private void linkExistingSamples( MSI msi )
	{
		Set<BioSample> addSmps = new HashSet<BioSample> (), delSmps = new HashSet<BioSample> ();
		Set<BioSample> msiSamples = msi.getSamples ();
		for ( BioSample sample: msiSamples ) 
		{
			if ( sample == null || sample.getId () != null ) continue;
			BioSample smpS = store.find ( sample, sample.getAcc () );
			if ( smpS == null || sample == smpS || smpCmp.compare ( smpS, sample ) != 0 ) continue;
			addSmps.add ( smpS ); delSmps.add ( sample );
		}
		msiSamples.removeAll ( delSmps );
		msiSamples.addAll ( addSmps );
	}
	
	/**
	 * Does the same as {@link #linkExistingSamples(MSI)} for {@link BioSampleGroup}s
	 */
	private void linkExistingSampleGroups ( MSI msi )
	{
		Set<BioSampleGroup> addSGs = new HashSet<BioSampleGroup> (), delSGs = new HashSet<BioSampleGroup> ();
		Set<BioSampleGroup> msiSGs = msi.getSampleGroups ();
		for ( BioSampleGroup sg: msiSGs ) 
		{
			if ( sg == null || sg.getId () != null ) continue;
			BioSampleGroup sgS = store.find ( sg, sg.getAcc () );
			if ( sgS == null || sg == sgS || sgCmp.compare ( sgS, sg ) != 0 ) continue;
			
			// The current sample group is going to be replaced, let's move its samples from it to the new SG.
			for ( BioSample sgSmp: sg.getSamples () ) sgS.addSample ( sgSmp );

			addSGs.add ( sgS ); delSGs.add ( sg );
		}
		msiSGs.removeAll ( delSGs );
		msiSGs.addAll ( addSGs );
	}

}
