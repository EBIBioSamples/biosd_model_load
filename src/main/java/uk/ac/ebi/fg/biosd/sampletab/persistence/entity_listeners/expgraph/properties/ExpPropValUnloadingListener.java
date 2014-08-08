package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.Product;
import uk.ac.ebi.fg.core_model.expgraph.ProtocolApplication;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

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
		String hql = "DELETE FROM " + ExperimentalPropertyValue.class.getName () + " pv WHERE\n"
			+ " pv NOT IN ( SELECT DISTINCT pv1.id FROM " + Product.class.getName () + " prod JOIN prod.propertyValues pv1 )\n"
			+ " AND pv NOT IN ( SELECT DISTINCT pv1.id FROM " + BioSampleGroup.class.getName () + " sg JOIN sg.propertyValues pv1 )\n"
			+ " AND pv NOT IN ( SELECT DISTINCT pv1.id FROM " + ProtocolApplication.class.getName () + " papp JOIN papp.parameterValues pv1 )";
		
		return this.entityManager.createQuery ( hql ).executeUpdate ();
	}
}
