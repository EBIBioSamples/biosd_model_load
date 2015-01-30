package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.persistence.hibernate.utils.HibernateUtils;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>7 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExpPropValUnloadingListener extends UnloadingListener<ExperimentalPropertyValue<ExperimentalPropertyType>>
{
	/**
	 * @param entityManager
	 */
	public ExpPropValUnloadingListener ( EntityManager entityManager )
	{
		super ( entityManager );
	}

	/**
	 *	Dangling PVs
	 */
	@Override
	public long postRemoveGlobally ()
	{
		String hqlWhere = 
			" pv NOT IN ( SELECT DISTINCT pv1.id FROM Product prod JOIN prod.propertyValues pv1 )\n"
			+ " AND pv NOT IN ( SELECT DISTINCT pv1.id FROM BioSampleGroup sg JOIN sg.propertyValues pv1 )\n"
			+ " AND pv NOT IN ( SELECT DISTINCT pv1.id FROM ProtocolApplication papp JOIN papp.parameterValues pv1 )";

		String sqlSel = HibernateUtils.hql2sql (
			"SELECT id FROM ExperimentalPropertyValue pv WHERE " + hqlWhere, true, this.entityManager 
		);
		
		long result = this.entityManager
			.createNativeQuery ( "DELETE FROM exp_prop_val_onto_entry WHERE owner_id IN (" + sqlSel + ")" )
			.executeUpdate ();
		
		result += this.entityManager
			.createQuery ( "DELETE FROM ExperimentalPropertyValue pv WHERE " + hqlWhere )
			.executeUpdate ();
		
		return result;
	}
}
