/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.terms.CVTermDAO;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
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
	
  private Logger log = LoggerFactory.getLogger ( getClass() );
	
	public Persister ( MSI msi ) {
		this.msi = msi;
	}

	public MSI persist () {
		return persist ( false );
	}

	public MSI persist ( boolean updateFlag )
	{
		// TODO: For the moment we put this patch to contacts and organisations here, it should probably lie in the 
		// parser.
		//
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();

		CVTermDAO<ContactRole> cvdao = new CVTermDAO<ContactRole> ( ContactRole.class, em );

    for ( Contact contact: msi.getContacts () ) 
    {
    	Set<ContactRole> newRoles = new HashSet<ContactRole> ();
    	for ( ContactRole role: contact.getContactRoles () )
    	{
    		if ( role == null || role.getName () == null ) {
    			log.error ( "Ignoring a contact role with null name: " + role );
    			continue;
    		}
    		ContactRole roleDB = cvdao.find ( role.getName () );
    		newRoles.add ( roleDB == null ?  role : roleDB );
    	}
    	contact.setContactRoles ( newRoles );
    }

    for ( Organization org: msi.getOrganizations () ) 
    {
    	Set<ContactRole> newRoles = new HashSet<ContactRole> ();
    	for ( ContactRole role: org.getOrganizationRoles () )
    	{
    		if ( role == null || role.getName () == null ) {
    			log.error ( "Ignoring an organization role with null name: " + role );
    			continue;
    		}
    		ContactRole roleDB = cvdao.find ( role.getName () );
    		newRoles.add ( roleDB == null ?  role : roleDB );
    	}
    	org.setOrganizationRoles ( newRoles );
    }
    
		
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		if ( updateFlag ) msi = dao.getOrCreate ( msi ); else dao.create ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		
		return msi;
	}
}
