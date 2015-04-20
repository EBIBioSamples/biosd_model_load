package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.xref.DatabaseRecRefDAO;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
import uk.ac.ebi.fg.core_model.expgraph.properties.dataitems.DataItem;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.expgraph.properties.dataitems.DataItemDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.OntologyEntryDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.fg.core_model.terms.CVTerm;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * A Store implementation that check for object existence against the BioSD relational database (via Hibernate). 
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DBStore implements Store
{
	
	private final AccessibleDAO<Accessible> accessibleDao;
	private final CVTermDAO<CVTerm> cvTermDao;
	private final ReferenceSourceDAO<ReferenceSource> refSrcDao;
	private final DatabaseRecRefDAO dbRecDao;
	private final OntologyEntryDAO<OntologyEntry> oeDao;
	private final DataItemDAO dataItemDao;
	private final EntityManager entityManager;
	
	/**
	 * @param entityManager, of course you need this to get into the database and it should be initialised the usual way, e.g., 
	 * via {@link Resources#getEntityManagerFactory()}.
	 * 
	 */
	public DBStore ( EntityManager entityManager )
	{
		this.entityManager = entityManager;
		accessibleDao = new AccessibleDAO<Accessible> ( Accessible.class, entityManager );
		cvTermDao = new CVTermDAO<CVTerm> ( CVTerm.class, entityManager );
		refSrcDao = new ReferenceSourceDAO<ReferenceSource> ( ReferenceSource.class, entityManager );
		dbRecDao = new DatabaseRecRefDAO ( entityManager );
		oeDao = new OntologyEntryDAO<OntologyEntry> ( OntologyEntry.class, entityManager );
		dataItemDao = new DataItemDAO ( DataItem.class, entityManager );
	}
	
	/**
	 * Implements {@link Store#find(Object, String...)} by means of specific delegates, which in turn use DAOs and Hibernate.
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public <T> T find ( T newObject, String... targetIds )
	{
		if ( newObject instanceof Accessible ) 
			return (T) findAccessible ( targetIds [ 0 ], (Accessible) newObject );
		
		if ( newObject instanceof CVTerm )	
			return (T) findCVTerm ( targetIds [ 0 ], (CVTerm) newObject );

		if ( newObject instanceof ReferenceSource ) 
			return (T) findRefSrc ( targetIds [ 0 ], targetIds [ 1 ], targetIds [ 2 ] );

		if ( newObject instanceof DatabaseRecordRef ) 
			return (T) findDbRefSrc ( targetIds [ 0 ], targetIds [ 1 ], targetIds [ 2 ] );
		
		if ( newObject instanceof OntologyEntry ) 
			return (T) findOE ( targetIds [ 0 ], targetIds [ 1 ], targetIds [ 2 ], targetIds [ 3 ] );

		if ( newObject instanceof DataItem )
			return (T) dataItemDao.find ( (DataItem) newObject );
		
		if ( newObject instanceof Annotation )
			// TODO: For the moment we don't recycle any annotation
			return null;
		
		throw new IllegalArgumentException ( 
			"Internal Error with BioSD persistence layer, don't know how to handle an object of type " 
			+ newObject.getClass () + ": " + newObject 
		);
	}

	@SuppressWarnings ( "unchecked" )
	private <A extends Accessible> A findAccessible ( String acc, A newObject ) {
		return (A) accessibleDao.find ( acc, newObject.getClass () );
	}

	@SuppressWarnings ( "unchecked" )
	private <CV extends CVTerm> CV findCVTerm ( String label, CV newObject ) {
		return (CV) cvTermDao.find ( label, newObject.getClass () );
	}
	
	private ReferenceSource findRefSrc ( String accession, String version, String url ) {
		return refSrcDao.find ( accession, version, url );
	}

	private DatabaseRecordRef findDbRefSrc ( String dbName, String accession, String version ) {
		return dbRecDao.find ( dbName, accession, version );
	}

	private OntologyEntry findOE ( String acc, String srcAcc, String srcVer, String srcUrl ) {
		return oeDao.find ( acc, srcAcc, srcVer, srcUrl );
	}

	
	public EntityManager getEntityManager () {
		return entityManager;
	}
	
}
