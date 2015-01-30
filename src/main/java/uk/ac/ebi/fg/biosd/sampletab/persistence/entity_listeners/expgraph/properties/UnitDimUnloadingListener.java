package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import uk.ac.ebi.fg.persistence.hibernate.utils.HibernateUtils;

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
		String hqlWhere = 
			"u NOT IN ( SELECT DISTINCT u.dimension.id FROM Unit u WHERE u.dimension IS NOT NULL )";
		
		String sqlSel = HibernateUtils.hql2sql ( "SELECT id FROM UnitDimension u WHERE " + hqlWhere, true, entityManager );
		
		long result = this.entityManager
			.createNativeQuery ( "DELETE FROM unit_dim_onto_entry WHERE owner_id IN ( " + sqlSel + ")" )
			.executeUpdate ();

		result += this.entityManager.createQuery ( "DELETE FROM UnitDimension u WHERE " + hqlWhere ).executeUpdate ();
		
		return result;
	}
}
