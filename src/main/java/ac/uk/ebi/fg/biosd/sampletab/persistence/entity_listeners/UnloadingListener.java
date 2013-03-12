/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.persistence.Persister;

/**
 * A generic deletion listener. These are called by the {@link Persister submission persister}. They are named 
 * listeners, since they follow a similar mechanism used in JPA. We prefer to use them out of JPA, in order to avoid
 * to make all the applications depending on these annotations put at the level of the object model.
 *
 * <dl><dt>date</dt><dd>Feb 13, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class UnloadingListener<T extends Identifiable>
{
	protected final EntityManager entityManager;
	
	public UnloadingListener ( EntityManager entityManager )
	{
		super ();
		this.entityManager = entityManager;
	}
	
	/**
	 * Persisters can decide to call this before the corresponding JPA persist operation. 
	 */
	public abstract void postRemove ( T entity );
}
