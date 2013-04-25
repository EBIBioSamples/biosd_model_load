/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.organizational;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.UnloadLogEntry;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.UnloadLogDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.BioSampleUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.CVTermUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.OntologyEntryUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.ReferenceSourceUnloadingListener;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.XRefUnloadingListener;

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
	 * Uses {@link UnloadLogDAO#delete(int)} to flush older entries (TODO: the constant parameter of 90 days to be moved
	 * to a configuration property).
	 * Removes the sample groups that are linked only to this submission.
	 * Tracks the operation by adding {@link UnloadLogEntry}s. 
	 */
	@Override
	public long preRemove ( MSI msi ) 
	{
		long result = 0;
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
		UnloadLogDAO unloadLogDao = new UnloadLogDAO ( entityManager );

		// Flush old entries in the unload log.
		result += unloadLogDao.delete ( 90 ); 

		for ( BioSampleGroup sg: msi.getSampleGroups () ) {
			// There are other MSIs linked to this
			if ( sg.getMSIs ().size () > 1 ) continue;
			if ( !sgDao.delete ( sg ) ) continue;
			unloadLogDao.create ( new UnloadLogEntry ( sg ) );
			result++;
		}
		
		return result;
	}

	/**
	 * Removes linked objects.
	 * Tracks the operation using {@link UnloadLogEntry}. 
	 */
	@Override
	public long postRemove ( MSI msi )
	{
		long result = new BioSampleUnloadingListener ( entityManager ).postRemove ( null );
		result += new OntologyEntryUnloadingListener ( entityManager ).postRemove ( null );
		result += new XRefUnloadingListener ( entityManager ).postRemove ( null );
		result += new ReferenceSourceUnloadingListener ( entityManager ).postRemove ( null );
		result += new CVTermUnloadingListener ( entityManager ).postRemove ( null );
		
		UnloadLogDAO unloadLogDao = new UnloadLogDAO ( entityManager );
		unloadLogDao.create ( new UnloadLogEntry ( msi ) );
		
		return result;
	}
}
