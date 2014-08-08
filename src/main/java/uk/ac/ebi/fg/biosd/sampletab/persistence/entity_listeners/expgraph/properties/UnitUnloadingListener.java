package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>7 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnitUnloadingListener extends UnloadingListener<Unit>
{
	/**
	 * @param entityManager
	 */
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
		String hql = "DELETE FROM " + Unit.class.getName () + " uv\n"
			+ "WHERE uv NOT IN ( SELECT DISTINCT pv.unit.id FROM " + ExperimentalPropertyValue.class.getName () + " pv WHERE pv.unit IS NOT NULL)\n";
		
		return this.entityManager.createQuery ( hql ).executeUpdate ();
	}
}
