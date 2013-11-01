package uk.ac.ebi.fg.biosd.sampletab.loader;

import static java.lang.System.out;

import java.sql.BatchUpdateException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.application_mgmt.LoadingDiagnosticEntry;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Persister;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

/**
 * 
 * The SampleTab loader command line manager. This will load a SampleTab submission into a relational database that
 * maps the BioSD object model.
 *
 * <dl><dt>date</dt><dd>Nov 5, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class LoaderCmd
{
	protected static Logger log = LoggerFactory.getLogger ( LoaderCmd.class );

	public static void main ( String[] args ) throws Throwable
	{
		if ( args == null || args.length == 0 ) printUsage ();

		String path = args [ 0 ];
		int exCode = 0;

		Long parsingTime = null, persistenceTime = null;
		Integer nitems = null;
		
		Throwable ex = null;
		
		try
		{
			// Parse the submission sampletab file.
			//
			out.println ( "\n\n >>> Loading '" + path + "'" );

			// Sometimes, when loadings occur in parallel (and in a cluster), some of them fail cause one saves something before
			// the other can learn an entity already exist in the database. Unfortunately, putting the normaliser inside a transaction
			// causes horrible Hibernate complaints, since it starts flushing updated before the normalisation has completed.
			// 
			// So, our brutal solution is to re-attempt the loading from scratch. We have already tried to re-persist the 
			// MSI in memory, so avoiding the re-load. Unfortunately, you face Hibernate problems this way as well, since it 
			// has objects coming from the DB in the MSI you try to re-persist and it believes you're attempting to re-create 
			// them. Maybe there is a smarter way to sort this out, but I have enough of struggling with it and it's not 
			// something that happens so often anyway.
			//
			for ( int attempts = 5; ; )
			{
				try 
				{
					long time0 = System.currentTimeMillis ();
					
					Loader loader = new Loader();
					MSI msi = loader.fromSampleData ( path );
					
					parsingTime = System.currentTimeMillis () - time0;
					nitems = msi.getSamples ().size () + msi.getSampleGroups ().size ();
					out.println ( 
						"\n" + nitems + " samples+groups loaded in " + formatTimeDuration ( parsingTime ) + ". Now persisting it in the DB" );
					
					// Now persist it
					//
					time0 = System.currentTimeMillis ();
					new Persister ().persist ( msi );
				
					persistenceTime = System.currentTimeMillis () - time0;
										
					out.println ( 
						"\nSubmission persisted in " + formatTimeDuration ( persistenceTime ) + ". Total time " +
						formatTimeDuration ( parsingTime + persistenceTime ) 
					);
					
					break;
				}
				catch ( RuntimeException aex ) 
				{
					if ( --attempts == 0 )
						throw new RuntimeException ( "Error while saving '" + path + "': " + aex.getMessage (), ex );

					Throwable aex1 = ExceptionUtils.getRootCause ( aex );
					if ( !( aex1 instanceof BatchUpdateException && StringUtils.contains ( aex1.getMessage (), "unique constraint" ) ) )
						throw new RuntimeException ( "Error while saving '" + path + "': " + aex.getMessage (), aex );
					
					log.warn ( "SQL exception: {} is likely due to concurrency, will retry {} more times", aex.getMessage (), attempts );
					
					// Have a random pause, minimises the likelihood to clash again 
					try {
						Thread.sleep ( RandomUtils.nextInt ( 2500 - 500 + 1 ) + 500 );
					} 
					catch ( InterruptedException sex ) { 
						throw new RuntimeException ( "Internal error while trying to get loader lock:" + sex.getMessage (), sex );
					}
				}
				
			} // for attempts
			
		} 
		catch ( Throwable ex1 ) 
		{
			ex = ex1;
			ex1.printStackTrace( System.err );
			log.error ( "Loader Command Error: {}", ex.getMessage (), ex  );
			exCode = 1;
		}
		finally 
		{
			saveLoadingDiagnostics ( path, ex, parsingTime, persistenceTime, nitems );
			
			EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
			if ( emf != null && emf.isOpen () ) emf.close ();
			
			System.exit ( exCode );
		}
	}
	
	
	private static void printUsage ()
	{
		out.println ();

		out.println ( "\n\n *** BioSD Relational Database Loader ***" );
		out.println ( "\nLoads a SampleTAB submission into the relational database" );
		
		out.println ( "Syntax:" );
		out.println ( "\n\tload.sh <path-to-biosampletab-file>\n\n" );
		out.println ( "See also hibernate.properites for the configuration of the target database.\n\n" );
		
		System.exit ( 1 ); // TODO: proper exit codes.
	}

	
	/**
	 * Format an amount of time into a string that reports both time in appropriate units (e.g. 1min 32sec) and in seconds.
	 */
	public static String formatTimeDuration ( long ms )
	{
	  long secs = Math.round ( ms * 1.0 / 1000 );

	  String timeStr = secs > 60
	  	? DurationFormatUtils.formatDurationWords ( ms, true, false )
	  	: null;
	  
	  return timeStr == null ? "" + secs +  " sec" : timeStr + " (or " + secs + " sec)";
	}
	
	private static void saveLoadingDiagnostics 
		( String sampleTabPath, Throwable ex, Long parsingMsec, Long persistenceMsec, Integer nitemsCount )
	{
		if ( !"true".equalsIgnoreCase ( System.getProperty ( "uk.ac.ebi.fg.biosd.sampletab.loader.debug" ) ) ) return;
		
		if ( ex != null ) ex = ExceptionUtils.getRootCause ( ex );
		
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		em.persist ( new LoadingDiagnosticEntry ( sampleTabPath, ex, parsingMsec, persistenceMsec, nitemsCount ));
		ts.commit ();
		em.close ();
	}
}
