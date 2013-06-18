package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.persistence.Unloader;

/**
 * A generic deletion listener. These are called by the {@link Unloader submission unloader}. They are named 
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
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public UnloadingListener ( EntityManager entityManager )
	{
		super ();
		this.entityManager = entityManager;
	}
	
	/**
	 * Persisters can decide to call this before the corresponding JPA persist operation, i.e., what to do before
	 * committing an undeletion operation. Bear in mind that the undeletion of an entity set (e.g., a submission)
	 * is a unique transaction.
	 *  
	 * @return the number of entities that were changed.
	 */
	public abstract long preRemove ( T entity );

	/**
	 * Persisters can decide to call this after the corresponding JPA persist operation. Bear in mind that the undeletion 
	 * of an entity set (e.g., a submission) is a unique transaction.
	 *  
	 * @return the number of entities that are changed.
	 */
	public abstract long postRemove ( T entity );
}
