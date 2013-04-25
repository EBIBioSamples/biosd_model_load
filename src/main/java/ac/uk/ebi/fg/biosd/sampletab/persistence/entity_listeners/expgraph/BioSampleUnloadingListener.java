/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.UnloadLogEntry;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.UnloadLogDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;

/**
 * Unloading listener for {@link BioSample}s.
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

	/**
	 * Does nothing.
	 */
	@Override
	public long preRemove ( BioSample entity ) { return 0; }
	
	/**
	 * Deletes samples not linked to any sample group or {@link MSI submission}.
	 * Tracks the operation using {@link UnloadLogEntry}. 
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public long postRemove ( BioSample foo )
	{
		long result = 0;
		
		AccessibleDAO<BioSample> smpDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
		UnloadLogDAO unloadLogDao = new UnloadLogDAO ( entityManager );

		String hql = String.format ( "FROM %s smp WHERE smp.MSIs IS EMPTY AND smp.groups IS EMPTY", BioSample.class.getCanonicalName () );
		for ( BioSample smp: ( List<BioSample> ) entityManager.createQuery ( hql ).getResultList () )
			if ( smpDao.delete ( smp ) ) {
				unloadLogDao.create ( new UnloadLogEntry ( smp ) );
				result++;
			}
		return result;
	}
	
}
