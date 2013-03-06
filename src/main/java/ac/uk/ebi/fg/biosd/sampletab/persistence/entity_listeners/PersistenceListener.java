/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners;

import javax.persistence.EntityManager;

import ac.uk.ebi.fg.biosd.sampletab.persistence.Persister;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

/**
 * A generic creation listener. These are called by the {@link Persister submission persister}. They are named 
 * listeners, since they follow a similar mechanism used in JPA. We prefer to use them out of JPA, in order to avoid
 * to make all the applications depending on these annotations put at the level of the object model.
 * 
 * TODO: DAOs are created inside the callbak methods, should be moved to the constructors. 
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class PersistenceListener<T extends Identifiable>
{
	protected final EntityManager entityManager;
	
	public PersistenceListener ( EntityManager entityManager )
	{
		super ();
		this.entityManager = entityManager;
	}
	
	/**
	 * Persisters can decide to call this before the corresponding JPA persist operation.
	 * This method should return true if it has actually changed something about the parameter.
	 * This method can assume that if entity.getId() != null it has nothing to do cause the entity is already in the DB. 
	 */
	public abstract void prePersist ( T entity );
}
