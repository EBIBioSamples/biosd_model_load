package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.persistence.hibernate.utils.HibernateUtils;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>7 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExpPropTypeUnloadingListener extends UnloadingListener<ExperimentalPropertyType>
{
	/**
	 * @param entityManager
	 */
	public ExpPropTypeUnloadingListener ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	@Override
	public long postRemoveGlobally ()
	{
		String hqlWhere = 
			"prop NOT IN ( SELECT DISTINCT pv.type.id FROM ExperimentalPropertyValue pv WHERE pv.type IS NOT NULL)\n"
			+ "AND prop NOT IN ( SELECT DISTINCT ptype.id FROM Protocol proto JOIN proto.parameterTypes ptype)\n"
			+ ")";
		
		String sqlSel = HibernateUtils.hql2sql (
			"SELECT id FROM ExperimentalPropertyType prop WHERE " + hqlWhere, true, this.entityManager 
		);

		long result = this.entityManager
			.createNativeQuery ( "DELETE FROM exp_prop_type_onto_entry WHERE owner_id IN (" + sqlSel + ")" )
			.executeUpdate ();

		result += this.entityManager
			.createQuery ( "DELETE FROM ExperimentalPropertyType prop WHERE " + hqlWhere )
			.executeUpdate ();
				
		return result;
	}
}
