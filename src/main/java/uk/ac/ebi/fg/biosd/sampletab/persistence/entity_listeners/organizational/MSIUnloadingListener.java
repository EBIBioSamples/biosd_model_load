package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import java.util.Date;
import java.util.LinkedList;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry;
import uk.ac.ebi.fg.biosd.model.application_mgmt.JobRegisterEntry.Operation;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.persistence.hibernate.application_mgmt.JobRegisterDAO;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.BioSampleUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.ExpPropTypeUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.ExpPropValUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.UnitDimUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.UnitUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph.properties.dataitems.DataItemUnloadingUnlistener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.CVTermUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms.OntologyEntryUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotationUnloaderListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.DatabaseRecRefUnloadingListener;
import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref.ReferenceSourceUnloadingListener;
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
	private boolean doPurge = false, doForcedPurge = false;

	public MSIUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	/**
	 * TODO: re-comment me! 
	 * 
	 * Uses {@link JobRegisterDAO#delete(int)} to flush older entries (TODO: the constant parameter of 90 days to be moved
	 * to a configuration property).
	 * Removes the sample groups that are linked only to this submission.
	 * Tracks the operation by adding {@link JobRegisterEntry}s.
	 */
	@Override
	public long preRemove ( MSI msi ) 
	{
		long result = 0;
		JobRegisterDAO jrDao = new JobRegisterDAO ( entityManager );

		// Flush old entries in the unload log.
		if ( isDoPurge () )
			result += jrDao.clean ( 90 ); 

		if ( msi == null ) return result;
		
		AccessibleDAO<BioSampleGroup> sgDao = new AccessibleDAO<BioSampleGroup> ( BioSampleGroup.class, entityManager );
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
	 * Removes linked objects, if {@link #isDoForcedPurge()} or {@link #isDoPurge()} and {@link #hasOldSubmissionsToBePurged()}.
	 * Tracks the operation using {@link JobRegisterEntry}. 
	 */
	@Override
	public long postRemove ( MSI msi )
	{
		long result = new BioSampleUnloadingListener ( entityManager ).postRemoveGlobally ();

		boolean doPurge = isDoForcedPurge () || isDoPurge () && ( msi != null || hasOldSubmissionsToBePurged () );
		if ( doPurge ) 
		{
			result += new ExpPropValUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new ExpPropTypeUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new UnitUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new UnitDimUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new OntologyEntryUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new DatabaseRecRefUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new CVTermUnloadingListener ( entityManager ).postRemoveGlobally ();
			result += new DataItemUnloadingUnlistener ( entityManager ).postRemoveGlobally ();
			result += new AnnotationUnloaderListener ( entityManager ).postRemoveGlobally ();
			result += new ReferenceSourceUnloadingListener ( entityManager ).postRemoveGlobally ();
			// Again for ref source's annotations?!
		}

		JobRegisterDAO jrDao = new JobRegisterDAO ( entityManager );

		if ( msi != null ) jrDao.create ( msi, Operation.DELETE );
		if ( doPurge ) jrDao.create ( null, Operation.DB_PURGE );

		return result;
	}

	
	public boolean isDoPurge ()
	{
		return doPurge;
	}

	public MSIUnloadingListener setDoPurge ( boolean doPurge )
	{
		this.doPurge = doPurge;
		return this;
	}
	
	
	
	
	public boolean isDoForcedPurge ()
	{
		return doForcedPurge;
	}

	public MSIUnloadingListener setDoForcedPurge ( boolean doForcedPurge )
	{
		this.doForcedPurge = doForcedPurge;
		return this;
	}

	
	
	private boolean hasOldSubmissionsToBePurged ()
	{
		JobRegisterDAO jrdao = new JobRegisterDAO ( this.entityManager );
		
		// When did we do the last purge?
		JobRegisterEntry jrLast = jrdao.findLast ( (String) null, null, Operation.DB_PURGE );

		// Did we do any unloads since then? If yes, we need to purge
		return !jrdao.find ( jrLast == null ? null : jrLast.getTimestamp (), new Date (), MSI.class, Operation.DELETE ).isEmpty ();
	}
}
