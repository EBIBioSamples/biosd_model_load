/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.organizational;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.EntityManager;

import org.hibernate.metamodel.relational.Database;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
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
			msi.deleteSampleGroup ( sg );
			
			// There are other MSIs linked to this
			if ( !sg.getMSIs ().isEmpty () ) continue;
			
			if ( !sgDao.delete ( sg ) ) continue;
			jrDao.create ( sg, Operation.DELETE );
			result++;
		}
		
		for ( BioSample smp: new LinkedList<BioSample> ( msi.getSamples () ) )
			msi.deleteSample ( smp );

		Collection<DatabaseRefSource> dbs = msi.getDatabases ();
		for ( DatabaseRefSource db: new LinkedList<DatabaseRefSource> ( dbs ) )
			dbs.remove ( db );

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
