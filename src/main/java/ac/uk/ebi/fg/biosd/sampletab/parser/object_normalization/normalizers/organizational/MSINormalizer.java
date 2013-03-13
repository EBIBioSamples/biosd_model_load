package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.Organization;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.BioSampleNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.expgraph.ProductComparator;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;

/**
 * 
 * TODO: Comment me!
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
	
	private ProductComparator smpCmp = new ProductComparator ();
	private BioSampleGroupComparator sgCmp = new BioSampleGroupComparator ();


	public MSINormalizer ( Store store ) 
	{
		super ( store );
	  contactNormalizer = new ContactNormalizer ( store );
		organizationNormalizer = new OrganizationNormalizer ( store );
		publicationNormalizer = new PublicationNormalizer ( store );
	}

	/**
	 * Invokes the methods below.
	 */
	@Override
	public void normalize ( MSI msi )
	{
		if ( msi == null ) {
			log.warn ( "Internal issue: MSI Peristence Listener got a null submission and that smell like a code bug" );
			return;
		}
		if ( msi.getId () != null ) return;
		
		super.normalize ( msi );

		for ( Contact contact: msi.getContacts () ) contactNormalizer.normalize ( contact );
		for ( Organization org: msi.getOrganizations () ) organizationNormalizer.normalize ( org );
		for ( Publication pub: msi.getPublications () ) publicationNormalizer.normalize ( pub );

		normalizeReferenceSources ( ReferenceSource.class, msi.getReferenceSources () );
		normalizeReferenceSources ( DatabaseRefSource.class, msi.getDatabases () );

		BioSampleNormalizer sampleNormalizer = new BioSampleNormalizer ( store );		
		for ( BioSample sample: msi.getSamples () ) sampleNormalizer.normalize ( sample );
		
		BioSampleGroupNormalizer sgNormalizer = new BioSampleGroupNormalizer ( store );
		for ( BioSampleGroup sg: msi.getSampleGroups () ) sgNormalizer.normalize ( sg );

		linkExistingSamples ( msi );
		linkExistingSampleGroups ( msi );		
	}
	
	private <R extends ReferenceSource> void normalizeReferenceSources ( Class<R> targetEntityClass, Set<R> sources )
	{
  	Set<R> addSrcs = new HashSet<R> (), delSrcs = new HashSet<R> ();
  	for ( R source: sources )
  	{
  		if ( source.getId () != null ) continue;
  		
  		R srcS = store.find ( source, source.getAcc (), source.getVersion () );
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
