/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 26, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class FilterPersistenceListener<I extends Identifiable> extends PersistenceListener<I>
{
	protected List<PersistenceListener<?>> listeners = new LinkedList<PersistenceListener<?>> ();

	public FilterPersistenceListener ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	@SuppressWarnings ( { "rawtypes", "unchecked" } )
	@Override
	public void prePersist ( I entity )
	{
		if ( entity == null || entity.getId () != null ) return; 
		
		for ( PersistenceListener listener: listeners )
			listener.prePersist ( entity );
	}
	
}
