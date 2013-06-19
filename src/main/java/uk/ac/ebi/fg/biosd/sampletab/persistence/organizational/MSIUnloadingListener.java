package uk.ac.ebi.fg.biosd.sampletab.persistence.organizational;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.BioSampleUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.CVTermUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.OntologyEntryUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.ReferenceSourceUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.XRefUnloadingListener;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;

/**
 * This is the entry point to all the listeners. Several objects linked to the MSI are taken care from here.
 *
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIUnloadingListener extends UnloadingListener<MSI>
{
	
	public MSIUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * Uses {@link JobRegisterDAO#delete(int)} to flush older entries (TODO: the constant parameter of 90 days to be moved
	 * to a configuration property).
	 * Removes the sample groups that are linked only to this submission.
	 * Tracks the operation by adding {@link JobRegisterEntry}s. 
	 */
	@Override
	public long preRemove ( MSI msi ) 
	{
		long result = 0;
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
		JobRegisterDAO jrDao = new JobRegisterDAO ( entityManager );

		// Flush old entries in the unload log.
		result += jrDao.clean ( 90 ); 

		for ( BioSampleGroup sg: new LinkedList<BioSampleGroup> ( msi.getSampleGroups () ) ) 
		{
			if ( sg.getMSIs ().size () > 1 ) continue;
			
			if ( !sgDao.delete ( sg ) ) continue;
			jrDao.create ( sg, Operation.DELETE );
			result++;
		}
		
		return result;
	}

	/**
	 * Removes linked objects.
	 * Tracks the operation using {@link JobRegisterEntry}. 
	 */
	@Override
	public long postRemove ( MSI msi )
	{
		long result = new BioSampleUnloadingListener ( entityManager ).postRemove ( null );
		result += new OntologyEntryUnloadingListener ( entityManager ).postRemove ( null );
		result += new XRefUnloadingListener ( entityManager ).postRemove ( null );
		result += new ReferenceSourceUnloadingListener ( entityManager ).postRemove ( null );
		result += new CVTermUnloadingListener ( entityManager ).postRemove ( null );
		
		JobRegisterDAO jrDao = new JobRegisterDAO ( entityManager );
		jrDao.create ( msi, Operation.DELETE );
		
		return result;
	}
}
