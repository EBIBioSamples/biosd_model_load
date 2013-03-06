/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.organizational.PublicationStatus;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.FilterPersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotatablePersistenceListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xrefs.ReferrerPersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class PublicationPersistenceListener extends FilterPersistenceListener<Publication>
{
	private final CVTermDAO<PublicationStatus> statusDao;

	public PublicationPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );

		listeners.add ( new AnnotatablePersistenceListener<Publication> ( entityManager ) );
		listeners.add ( new ReferrerPersistenceListener<Publication> ( entityManager ) );
		
		statusDao = new CVTermDAO<PublicationStatus> ( PublicationStatus.class, entityManager );		
	}

	@Override
	public void prePersist ( Publication pub ) 
	{
		if ( pub == null || pub.getId () != null ) return;
		super.prePersist ( pub );
		
		PublicationStatus status = pub.getStatus ();
		if ( status == null ) return;
		String statusName = StringUtils.trimToNull ( status.getName () );
			if ( statusName == null ) return;
			
		PublicationStatus statusDB = statusDao.find ( statusName );
		if ( statusDB == null ) return;
		
		pub.setStatus ( statusDB );
	}
	
}
