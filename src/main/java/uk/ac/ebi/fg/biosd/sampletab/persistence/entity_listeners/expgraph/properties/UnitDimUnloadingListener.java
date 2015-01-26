package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>7 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnitDimUnloadingListener extends UnloadingListener<UnitDimension>
{
	/**
	 * @param entityManager
	 */
	public UnitDimUnloadingListener ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 * Dangling unit dimensions
	 */
	@Override
	public long postRemoveGlobally ()
	{
		String hql = "DELETE FROM " + UnitDimension.class.getName () + " u\n"
				+ "WHERE u NOT IN ( SELECT DISTINCT u.dimension.id FROM " + Unit.class.getName () + " u WHERE u.dimension IS NOT NULL )\n";
		
		return this.entityManager.createQuery ( hql ).executeUpdate ();
	}
}
