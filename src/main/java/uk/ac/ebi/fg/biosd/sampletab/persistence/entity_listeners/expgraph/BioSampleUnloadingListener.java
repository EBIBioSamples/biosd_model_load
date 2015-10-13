package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import java.util.List;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

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
	 * Deletes samples not linked to any sample group or {@link MSI submission}.
	 * Tracks the operation using {@link JobRegisterEntry}. 
	 */
	@Override
	@SuppressWarnings ( "unchecked" )
	public long postRemoveGlobally ()
	{
		long result = 0;
		
		AccessibleDAO<BioSample> smpDao = new AccessibleDAO<BioSample> ( BioSample.class, entityManager );
		JobRegisterDAO jrDao = new JobRegisterDAO ( entityManager );

		String hql = String.format ( "FROM %s smp WHERE smp.MSIs IS EMPTY AND smp.groups IS EMPTY", BioSample.class.getCanonicalName () );
		for ( BioSample smp: ( List<BioSample> ) entityManager.createQuery ( hql ).getResultList () )
			if ( smpDao.delete ( smp ) ) {
				jrDao.create ( smp, Operation.DELETE );
				result++;
			}
		return result;
	}
	
}
