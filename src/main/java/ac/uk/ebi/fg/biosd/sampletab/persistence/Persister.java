/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational.MSIPersistenceListener;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.organizational.Organization;
import uk.ac.ebi.fg.core_model.resources.Resources;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Nov 5, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Persister
{
	private MSI msi;
	
	public Persister ( MSI msi ) {
		this.msi = msi;
	}

	public MSI persist ()
	{
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();
    
		new MSIPersistenceListener ( em ).prePersist ( msi );
		
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		dao.create ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		
		return msi;
	}
}
