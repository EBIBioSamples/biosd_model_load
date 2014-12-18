package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>18 Dec 2014</dd>
 *
 */
public class UnitUnloadingListener extends UnloadingListener<Unit>
{
	public UnitUnloadingListener ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	
	@Override
	public long postRemoveGlobally ()
	{
		String sql = "DELETE FROM unit WHERE id not in ( select unit_id from exp_prop_val )";
		long result = entityManager.createNativeQuery ( sql ).executeUpdate ();

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemoveGlobally(): returning %d", this.getClass().getSimpleName (), result ));
		return result;
	}
}
