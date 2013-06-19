/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.organizational.MSIUnloadingListener;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;

/**
 * The submission unloader. Manages the removal of a sampletab-based submission.
 *
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Unloader
{
	protected final Logger log = LoggerFactory.getLogger ( this.getClass () );

	public long unload ( MSI msi ) {
		return unload ( msi.getAcc () );
	}
	
	public long unload ( String msiAcc )
	{
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();
    
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
		EntityTransaction ts = em.getTransaction ();
		
		ts.begin ();
		MSI msi = dao.find ( msiAcc );
		if ( msi == null ) {
			log.warn ( "Unloading request for a non-existing submission: '" + msiAcc + "'" );
			return 0;
		}
		
		MSIUnloadingListener unloadListener = new MSIUnloadingListener ( em );
		
		long result = unloadListener.preRemove ( msi );
		result += dao.delete ( msi ) ? 1 : 0;
		result += new MSIUnloadingListener ( em ).postRemove ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		
		return result;
	}
}
