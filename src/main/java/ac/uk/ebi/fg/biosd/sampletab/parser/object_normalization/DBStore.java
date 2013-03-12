/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.OntologyEntryDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.xref.ReferenceSourceDAO;
import uk.ac.ebi.fg.core_model.terms.CVTerm;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.toplevel.Accessible;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * TODO: Comment me!
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
	private final OntologyEntryDAO<OntologyEntry> oeDao;
	
	
	public DBStore ( EntityManager entityManager )
	{
		accessibleDao = new AccessibleDAO<Accessible> ( Accessible.class, entityManager );
		cvTermDao = new CVTermDAO<CVTerm> ( CVTerm.class, entityManager );
		refSrcDao = new ReferenceSourceDAO<ReferenceSource> ( ReferenceSource.class, entityManager );
		oeDao = new OntologyEntryDAO<OntologyEntry> ( OntologyEntry.class, entityManager );
	}
	
	
	@Override
	@SuppressWarnings ( "unchecked" )
	public <T> T find ( T newObject, String... targetIds )
	{
		if ( newObject instanceof Accessible ) 
			return (T) findAccessible ( targetIds [ 0 ], (Accessible) newObject );
		
		if ( newObject instanceof CVTerm )	
			return (T) findCVTerm ( targetIds [ 0 ], (CVTerm) newObject );
		
		if ( newObject instanceof ReferenceSource ) 
			return (T) findRefSrc ( targetIds [ 0 ], targetIds [ 1 ], (ReferenceSource) newObject );
		
		if ( newObject instanceof OntologyEntry ) 
			return (T) findOE ( targetIds [ 0 ], targetIds [ 1 ], targetIds [ 2 ] );

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
	
	@SuppressWarnings ( "unchecked" )
	private <S extends ReferenceSource> S findRefSrc ( String accession, String version, S newObject ) {
		return (S) refSrcDao.find ( accession, version, newObject.getClass () );
	}

	private OntologyEntry findOE ( String acc, String srcAcc, String srcVer ) {
		return oeDao.find ( acc, srcAcc, srcVer );
	}

}
