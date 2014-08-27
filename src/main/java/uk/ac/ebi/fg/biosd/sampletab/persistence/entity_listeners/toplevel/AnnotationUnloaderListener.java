package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AnnotationDAO;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>14 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotationUnloaderListener extends UnloadingListener<Annotation>
{
	AnnotationDAO<Annotation> dao;
	
	public AnnotationUnloaderListener ( EntityManager entityManager )
	{
		super ( entityManager );
		dao = new AnnotationDAO<Annotation> ( Annotation.class, this.entityManager );
	}


	@Override
	public long postRemoveGlobally ()
	{
		return dao.purge ();
	}

}
