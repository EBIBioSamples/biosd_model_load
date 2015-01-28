package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.persistence.hibernate.utils.HibernateUtils;

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
		String hqlWhere = "uv NOT IN ( SELECT DISTINCT pv.unit.id FROM ExperimentalPropertyValue pv WHERE pv.unit IS NOT NULL)\n";
		String sqlSel = HibernateUtils.hql2sql ( "SELECT id FROM Unit uv WHERE " + hqlWhere, true, this.entityManager ); 
		
		long result = this.entityManager
			.createNativeQuery ( "DELETE FROM unit_onto_entry WHERE owner_id IN ( " + sqlSel + ")" )
			.executeUpdate ();
		
		result += this.entityManager.createQuery ( "DELETE FROM Unit uv WHERE " + hqlWhere ).executeUpdate ();
		
		return result;
	}
}
