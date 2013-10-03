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
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

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
	 * from the {@link Loader}, since this invokes the normaliser automatically.
	 *  
	 */
	public MSI persist ( MSI msi )
	{  
		// Do multiple attempts in case of exceptions, see explanations below.
		//
		for ( int attempts = 5; ; )
		{
			try {
				return tryPersist ( msi );
			} 
			catch ( RuntimeException ex ) 
			{
				if ( --attempts == 0 )
					throw new RuntimeException ( "Error while saving '" + msi.getAcc () + "': " + ex.getMessage (), ex );

				Throwable ex1 = ExceptionUtils.getRootCause ( ex );
				if ( !( ex1 instanceof BatchUpdateException && StringUtils.contains ( ex1.getMessage (), "unique constraint" ) ) )
					throw new RuntimeException ( "error while saving '" + msi.getAcc () + "': " + ex.getMessage (), ex );
				
				log.debug ( "SQL exception: {} is likely due to concurrency, will retry {} more times", ex.getMessage (), attempts );
			}

			// Have a random pause, minimises the likelihood to clash again 
			try {
				Thread.sleep ( RandomUtils.nextInt ( 2500 - 500 + 1 ) + 500 );
			} 
			catch ( InterruptedException ex ) {
				throw new RuntimeException ( "Internal error while trying to get loader lock:" + ex.getMessage (), ex );
			}
		}
	}  //persist
	
	/**
	 * Single persistence is re-attempted a couple of times inside {@link #persist(MSI)}, in case it gets SQL exceptions
	 * or alike. Sometimes the persister fails just because of parallelism, e.g., a parallel loader first check if an 
	 * object is in the DB (i.e., uses the {@link Normalizer}s), then it persists the submission, but meanwhile another
	 * loader has created objects that the first still believes not to exist. It's not easy to wrap the normalization
	 * stage into a transaction, cause in such case Hibernate attempts to save new objects as part of update operations
	 * over existing ones. Because such cases are not frequent, we prefer to fix the problem by simply re-attempting. 
	 * 
	 */
	private MSI tryPersist ( MSI msi )
	{    		
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();

		// Normalise (i.e., removes duplicates) in-memory objects that appear to be duplicates of already-existing objects
		// on the database side.
		new MSINormalizer ( new DBStore ( em ) ).normalize ( msi );
			
		// Ready to go now.
		//
		AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );

		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		dao.create ( msi );
		ts.commit ();
		
		// Just to be sure, we've noted some timeouts on Oracle side
		//
		if ( em.isOpen () ) em.close ();
		return msi;
		
	}  //persist
}
