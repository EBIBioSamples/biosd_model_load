/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.MemoryStore;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational.MSINormalizer;

/**
 * Persists an instance of the BioSD model into the database that it is mapped to via JPA/Hibernate.
 * You find connection details about such database in {@link Resources}. In turn, this class gets parameters from 
 * the usual hibernate.properties file located in a .jar or in the application starting directory. 
 *
 * <dl><dt>date</dt><dd>Nov 5, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Persister
{
	public MSI persist ( MSI msi )
	{
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();
    
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
		EntityTransaction ts = em.getTransaction ();
		
		// Normalise (i.e., removes duplicates) in-memory duplicates
		new MSINormalizer ( new MemoryStore () ).normalize ( msi );
		
		// Normalise (i.e., removes duplicates) in-memory objects that appear to be duplicates of already-existing objects
		// on the database side.
		new MSINormalizer ( new DBStore ( em ) ).normalize ( msi );
		
		// Ready to go now.
		
		ts.begin ();
		dao.create ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		
		return msi;
	}
}
