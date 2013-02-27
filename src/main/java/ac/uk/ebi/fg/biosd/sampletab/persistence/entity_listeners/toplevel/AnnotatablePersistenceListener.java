/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.toplevel.Annotatable;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.PersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AnnotatablePersistenceListener<A extends Identifiable> extends PersistenceListener<A>
{
	private final AnnotationPersistenceListener annPersistenceListener;
	
	public AnnotatablePersistenceListener ( EntityManager entityManager )
	{
		super ( entityManager );
		annPersistenceListener = new AnnotationPersistenceListener ( entityManager );
	}

	@Override
	public void prePersist ( A annotatable )
	{
		if ( annotatable == null || annotatable.getId () != null ) return;
		if ( ! ( annotatable instanceof Annotatable ) ) throw new RuntimeException (
			"Internal Error: " + annotatable.getClass ().getName () + " can only be used with an instance of Annotatable"
		);
		
		for ( Annotation ann: ( (Annotatable) annotatable ).getAnnotations () )
			annPersistenceListener.prePersist ( ann );
	}
}
