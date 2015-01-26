package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;

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

	
	/**
	 * Removes dangling units
	 */	
	@Override
	public long postRemoveGlobally ()
	{
		//String sql = "DELETE FROM unit WHERE id not in ( select unit_id from exp_prop_val )";
		//long result = entityManager.createNativeQuery ( sql ).executeUpdate ();

		String hql = "DELETE FROM " + Unit.class.getName () + " uv\n"
			+ "WHERE uv NOT IN ( SELECT DISTINCT pv.unit.id FROM " + ExperimentalPropertyValue.class.getName () + " pv WHERE pv.unit IS NOT NULL)\n";
		
		long result = this.entityManager.createQuery ( hql ).executeUpdate ();
		
		// TODO: AOP
		log.trace ( String.format ( "%s.postRemoveGlobally(): returning %d", this.getClass().getSimpleName (), result ));
		return result;
	}
}
