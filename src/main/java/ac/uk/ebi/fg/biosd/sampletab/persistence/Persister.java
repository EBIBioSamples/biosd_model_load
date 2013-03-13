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
    
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
		EntityTransaction ts = em.getTransaction ();
		new MSINormalizer ( new MemoryStore () ).normalize ( msi );
		new MSINormalizer ( new DBStore ( em ) ).normalize ( msi );
		ts.begin ();
		dao.create ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		
		return msi;
	}
}
