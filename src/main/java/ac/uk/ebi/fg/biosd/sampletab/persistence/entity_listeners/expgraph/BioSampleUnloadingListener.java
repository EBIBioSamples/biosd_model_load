/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleUnloadingListener extends UnloadingListener<BioSample>
{
	public BioSampleUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( BioSample entity ) { return 0; }
	
	
	@Override
	@SuppressWarnings ( "unchecked" )
	public long postRemove ( BioSample foo )
	{
		long result = 0;
		
		AccessibleDAO<BioSample> smpDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
		String hql = String.format ( "FROM %s smp WHERE smp.MSIs IS EMPTY AND smp.groups IS EMPTY", BioSample.class.getCanonicalName () );
		for ( BioSample smp: ( List<BioSample> ) entityManager.createQuery ( hql ).getResultList () )
			if ( smpDao.delete ( smp ) ) result++;
		return result;
	}
	
}
