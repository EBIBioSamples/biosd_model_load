package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.Protocol;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;

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
		String hql = "DELETE FROM " + ExperimentalPropertyType.class.getName () + " prop WHERE\n"
			+ "prop NOT IN ( SELECT DISTINCT pv.type.id FROM " + ExperimentalPropertyValue.class.getName () + " pv WHERE pv.type IS NOT NULL)\n"
			+ "AND prop NOT IN ( SELECT DISTINCT ptype.id FROM " + Protocol.class.getName () + " proto JOIN proto.parameterTypes ptype)\n"
			+ ")";
		
		return this.entityManager.createQuery ( hql ).executeUpdate ();
	}
}
