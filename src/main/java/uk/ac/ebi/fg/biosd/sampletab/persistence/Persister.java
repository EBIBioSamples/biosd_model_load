package uk.ac.ebi.fg.biosd.sampletab.persistence;

import java.sql.BatchUpdateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.loader.Loader;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.DBStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.MemoryStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational.MSINormalizer;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;

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
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * WARNING: this doesn't not invoke new {@link MSINormalizer} ( new {@link MemoryStore} () ).normalize ( msi ), 
	 * i.e., you have to normalise in-memory objects that look duplicate. You don't need to do that if your objects come
	 * from the {@link Loader}, since this invokes such normaliser automatically.
	 * 
	 * Instead, this method takes care of invoking the normaliser with the {@link DBStore}, i.e., it takes entities 
	 * that already exists in the DB into account.
	 *  
	 */
	public MSI persist ( MSI msi )
	{  
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();

		try 
		{
			// Normalise (i.e., removes duplicates) in-memory objects that appear to be duplicates of already-existing objects
			// on the database side.
			new MSINormalizer ( new DBStore ( em ) ).normalize ( msi );
				
			// Ready to go now.
			//
			AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
	
			// Unfortunately the transaction cannot start before normalisation (see Javadoc)
			EntityTransaction ts = em.getTransaction ();
			ts.begin ();
			dao.create ( msi );
			ts.commit ();

			return msi;
		}
		finally
		{
			// Just to be sure, we've noted some timeouts on Oracle side
			//
			if ( em != null && em.isOpen () ) em.close ();
		}
	}  //persist
}
