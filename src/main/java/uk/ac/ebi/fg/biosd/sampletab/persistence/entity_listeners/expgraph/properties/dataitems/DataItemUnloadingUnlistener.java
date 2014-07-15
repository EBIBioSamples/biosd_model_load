package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.dataitems;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.expgraph.properties.dataitems.DataItem;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.expgraph.properties.dataitems.DataItemDAO;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>14 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class DataItemUnloadingUnlistener extends UnloadingListener<DataItem>
{
	private DataItemDAO dao;
	
	public DataItemUnloadingUnlistener ( EntityManager entityManager )
	{
		super ( entityManager );
		this.dao = new DataItemDAO ( DataItem.class, entityManager );
	}

	@Override
	public long postRemoveGlobally ()
	{
		return dao.purge ();
	}

}
