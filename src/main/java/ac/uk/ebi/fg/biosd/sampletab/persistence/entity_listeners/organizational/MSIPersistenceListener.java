package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.organizational.Organization;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.ProductComparator;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.ProductPersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotatablePersistenceListener;

/**
 * Works pre-post processing operations about the {@link Product} objects.
 *
 * <dl><dt>date</dt><dd>Jan 15, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIPersistenceListener extends AnnotatablePersistenceListener<MSI>
{
  private Logger log = LoggerFactory.getLogger ( getClass() );

	public MSIPersistenceListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * Invokes the methods below.
	 */
	@Override
	public void prePersist ( MSI msi )
	{
		if ( msi == null ) {
			log.warn ( "Internal issue: MSI Peristence Listener got a null submission and that smell like a code bug" );
			return;
		}
		if ( msi.getId () != null ) return;
		
		super.prePersist ( msi );

		// TODO: we're trying to decide wether this should be done by the Limpopo parser
		//
		Map<String, ContactRole> allRoles = new HashMap<String, ContactRole> ();
		for ( Contact contact: msi.getContacts () ) normalizeContactRolesPreDB ( allRoles, contact.getContactRoles () );
		for ( Organization org: msi.getOrganizations () ) normalizeContactRolesPreDB ( allRoles, org.getOrganizationRoles () );

		ContactPersistenceListener conctactPersistenceListener = new ContactPersistenceListener ( entityManager );
		for ( Contact contact: msi.getContacts () )
			conctactPersistenceListener.prePersist ( contact );
		
		OrganizationPersistenceListener organizationPersistenceListener = new OrganizationPersistenceListener ( entityManager );
		for ( Organization org: msi.getOrganizations () )
			organizationPersistenceListener.prePersist ( org );
		
		PublicationPersistenceListener publicationPersistenceListener = new PublicationPersistenceListener ( entityManager );
		for ( Publication pub: msi.getPublications () )
			publicationPersistenceListener.prePersist ( pub );
		
		linkExistingRefSources ( ReferenceSource.class, msi.getReferenceSources () );
		linkExistingRefSources ( DatabaseRefSource.class, msi.getDatabases () );
		
		ProductPersistenceListener prodListener = new ProductPersistenceListener ( entityManager );		
		for ( BioSample sample: msi.getSamples () )
			prodListener.prePersist ( sample );
		
		BioSampleGroupPersistenceListener sgListener = new BioSampleGroupPersistenceListener ( entityManager );
		for ( BioSampleGroup sg: msi.getSampleGroups () )
			sgListener.prePersist ( sg );

		linkExistingSamples ( msi );
		linkExistingSampleGroups ( msi );		
	}
	
	/**
	 * Normalise all the roles in memory, making them the same object when they share the name  
	 * 
	 * TODO: This method would belong more in for ContactPersister and OrganizationPersister 
	 */
	private void normalizeContactRolesPreDB ( Map<String, ContactRole> allRoles, Set<ContactRole> roles ) 
	{
  	Set<ContactRole> addRoles = new HashSet<ContactRole> (), delRoles = new HashSet<ContactRole> ();
		for ( ContactRole role: roles ) 
		{
			String roleName = role.getName ();
			ContactRole oldRole = allRoles.get ( roleName );
			if ( oldRole == null ) {
				allRoles.put ( roleName, role );
				continue; 
			}
			delRoles.add ( role ); addRoles.add ( oldRole ); 
		}
		
		roles.removeAll ( delRoles ); roles.addAll ( addRoles );
	}
	
	
	/**
	 * Removes those contact roles that have null attributes and re-use those with the same name.
	 * 
	 * TODO: Remove, this has been moved to proper persistence listeners
	 */
	private void normalizeContactRolesFromDB ( Set<ContactRole> roles )
	{
		CVTermDAO<ContactRole> cvdao = new CVTermDAO<ContactRole> ( ContactRole.class, entityManager );

  	Set<ContactRole> addRoles = new HashSet<ContactRole> (), delRoles = new HashSet<ContactRole> ();
  	for ( ContactRole role: roles )
  	{
  		if ( role.getId () != null ) continue;

  		if ( role.getName () == null ) {
  			log.error ( "Ignoring a contact role with null name: " + role );
  			delRoles.add ( role );
  			continue;
  		}
  		  		
  		ContactRole roleDB = cvdao.find ( role.getName () );
  		if ( roleDB == null ) continue;
 
  		delRoles.add ( role ); addRoles.add ( roleDB );
  	}
  	
  	roles.removeAll ( delRoles ); roles.addAll ( addRoles );
	}
	

	
	/**
	 * Links those {@link ReferenceSource}s and {@link DatabaseRefSource}s that already exists in the DB, using 
	 * accession+version and equivalence criteria.
	 */
	private <R extends ReferenceSource> void linkExistingRefSources ( Class<R> targetEntityClass, Set<R> sources )
	{
		ReferenceSourceDAO<R> dao = new ReferenceSourceDAO<R> ( targetEntityClass, entityManager );
		
  	Set<R> addSrcs = new HashSet<R> (), delSrcs = new HashSet<R> ();
  	for ( R source: sources )
  	{
  		if ( source.getId () != null ) continue;
  		
  		R srcDB = dao.find ( source.getAcc (), source.getVersion () );
  		if ( srcDB == null ) continue;
 
  		delSrcs.add ( source ); addSrcs.add ( srcDB );
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
		AccessibleDAO<BioSample> smpDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
		ProductComparator smpCmp = new ProductComparator ();
		
		Set<BioSample> addSmps = new HashSet<BioSample> (), delSmps = new HashSet<BioSample> ();
		Set<BioSample> msiSamples = msi.getSamples ();
		for ( BioSample sample: msiSamples ) 
		{
			if ( sample == null || sample.getId () != null ) continue;
			BioSample smpDB = smpDao.find ( sample.getAcc () );
			if ( smpDB == null || smpCmp.compare ( smpDB, sample ) != 0 ) continue;
			addSmps.add ( smpDB ); delSmps.add ( sample );
		}
		msiSamples.removeAll ( delSmps );
		msiSamples.addAll ( addSmps );
	}
	
	/**
	 * Does the same as {@link #linkExistingSamples(MSI)} for {@link BioSampleGroup}s
	 */
	private void linkExistingSampleGroups ( MSI msi )
	{
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
		BioSampleGroupComparator sgCmp = new BioSampleGroupComparator ();

		Set<BioSampleGroup> addSGs = new HashSet<BioSampleGroup> (), delSGs = new HashSet<BioSampleGroup> ();
		Set<BioSampleGroup> msiSGs = msi.getSampleGroups ();
		for ( BioSampleGroup sg: msiSGs ) 
		{
			if ( sg == null || sg.getId () != null ) continue;
			BioSampleGroup sgDB = sgDao.find ( sg.getAcc () );
			if ( sgDB == null || sgCmp.compare ( sgDB, sg ) != 0 ) continue;
			
			// The current sample group is going to be replaced, let's move its samples from it to the new SG.
			for ( BioSample sgSmp: sg.getSamples () ) sgDB.addSample ( sgSmp );

			addSGs.add ( sgDB ); delSGs.add ( sg );
		}
		msiSGs.removeAll ( delSGs );
		msiSGs.addAll ( addSGs );
	}
}
