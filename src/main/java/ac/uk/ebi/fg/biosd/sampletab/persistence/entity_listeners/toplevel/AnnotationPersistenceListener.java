/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.terms.AnnotationType;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotationPersistenceListener extends PersistenceListener<Annotation>
{
	private final CVTermDAO<AnnotationType> annTypeDao;

	public AnnotationPersistenceListener ( EntityManager entityManager )
	{
		super ( entityManager );
		annTypeDao = new CVTermDAO<AnnotationType> ( AnnotationType.class, entityManager );
	}

	@Override
	public void prePersist ( Annotation ann )
	{
		if ( ann == null || ann.getId () != null ) return;
		AnnotationType type = ann.getType ();
		if ( type == null || type.getId () != null ) return; 
		
		AnnotationType typeDB = annTypeDao.find ( type.getName () );
		if ( typeDB == null ) return; 
		
		ann.setType ( typeDB );
	}
	
}
