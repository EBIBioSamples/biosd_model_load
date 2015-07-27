package uk.ac.ebi.fg.biosd.sampletab.loader;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.application_mgmt.LoadingDiagnosticEntry;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Persister;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Unloader;
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
		CommandLineParser clparser = new GnuParser ();
		CommandLine cli = clparser.parse ( getOptions(), args );
		
		if ( cli.hasOption ( 'h' ) ) printUsage ();
		if ( ( args = cli.getArgs () ) == null || args.length == 0 ) printUsage ();

		String path = args [ 0 ];
		int exCode = 0;

		Long parsingTime = null, persistenceTime = null;
		Integer nitems = null;
		
		Throwable ex = null;
		
		String msiAcc = null;
				
		try
		{
			// Parse the submission sampletab file.
			//
			log.info ( " >>> Loading '" + path + "'" );

			// Sometimes, when loadings occur in parallel (and in a cluster), some of them fail cause one saves something before
			// the other can learn an entity already exists in the database. Unfortunately, putting the normaliser inside a transaction
			// causes horrible Hibernate complaints, since it starts flushing updates before the normalisation has completed.
			// 
			// So, our brutal solution is to re-attempt the loading from scratch. We have already tried to re-persist the 
			// MSI in memory, to avoid the re-load step. Unfortunately, this causes other Hibernate problems, since it 
			// gets objects coming from the DB, linked to the MSI you try to re-persist and that makes Hibernate to believe 
			// you're attempting to re-create such objects. Maybe there is a smarter way to sort this out, but I have enough 
			// of struggling with it and it's not something that happens so often anyway.
			//

			MSI msi = null;
			
			if ( cli.hasOption ( 'u' ) )
			{
				for ( int attempts = 5; ; )
				{
					try 
					{
						msi = loadSampleTab ( path );
						msiAcc = msi.getAcc ();
					
						log.info ( "Unloading previous version of " + msiAcc + " (if any)" );
						new Unloader()
							.setDoForcedPurge ( cli.hasOption ( 'f' ) )
							.setDoPurge ( cli.hasOption ( 'g' ) )
							.unload ( msi );
						log.info ( "done." );
						break;
					}
					catch ( RuntimeException aex ) {
						handleFailedPersistenceAttempt ( --attempts, aex, path );
					}
				} // for attempts
			} // if cli.hasOption ( 'u' )
			
			
			// Now persist it, using the same approach
			//
			for ( int attempts = 5; ; )
			{
				try 
				{
					if ( msi == null )
					{
						msi = loadSampleTab ( path );
						msiAcc = msi.getAcc ();
					}
					
					// only persist if there is something worth it persisting
          if (msi.getSamples().size() + msi.getSampleGroups().size() > 0) 
          {
          	log.info ( "Now persisting data" );
						long time0 = System.currentTimeMillis ();
						new Persister ().persist ( msi );
						persistenceTime = System.currentTimeMillis () - time0;
											
						log.info ( 
							"Submission persisted in " + formatTimeDuration ( persistenceTime ) + "."
						);
          }
					break;
				}
				catch ( RuntimeException aex ) {
					handleFailedPersistenceAttempt ( --attempts, aex, path );
				}
				
			} // for attempts
			
		} 
		catch ( Throwable ex1 ) 
		{
			ex = ex1;
			log.error ( "Execution failed with the error: " + ex.getMessage (), ex  );
			exCode = 1;
		}
		finally 
		{
			saveLoadingDiagnostics ( msiAcc, path, ex, parsingTime, persistenceTime, nitems );
			
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
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\tload.sh [options] <submission path>" );		
		
		out.println ( "\nOptions:" );
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( out, true );
		helpFormatter.printOptions ( pw, 100, getOptions (), 2, 4 );
		
		out.println ( "\nSee also hibernate.properites for the configuration of the target database.\n\n" );
		
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
	
	/**
	 * Saves loading diagnostics data, if System.getProperty ( "uk.ac.ebi.fg.biosd.sampletab.loader.debug" ) is true. 
	 * 
	 * @param msiAcc the submission accession being loaded
	 * @param sampleTabPath the file being loaded
	 * @param ex	the exception that made the command to fail, null if success
	 * @param parsingMsec  how long time the parser took to build a BioSD model instance, null in case of exception
	 * @param persistenceMsec how long time it took to store the submission, null in case of exception
	 * @param nitemsCount how many samples, sample groups, the submission being loaded has
	 */
	private static void saveLoadingDiagnostics 
		( String msiAcc, String sampleTabPath, Throwable ex, Long parsingMsec, Long persistenceMsec, Integer nitemsCount )
	throws IOException
	{
		if ( !"true".equalsIgnoreCase ( System.getProperty ( "uk.ac.ebi.fg.biosd.sampletab.loader.debug" ) ) ) return;
		
		if ( ex != null ) ex = ExceptionUtils.getRootCause ( ex );
		
		sampleTabPath = new File ( sampleTabPath ).getCanonicalPath ();
		
		EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
		EntityManager em = emf.createEntityManager ();
		EntityTransaction ts = em.getTransaction ();
		ts.begin ();
		em.persist ( new LoadingDiagnosticEntry ( msiAcc, sampleTabPath, ex, parsingMsec, persistenceMsec, nitemsCount ));
		ts.commit ();
		em.close ();
	}
	
	/**
	 * The command line options used for this command.  
	 */
	@SuppressWarnings ( { "static-access" } )
	private static Options getOptions ()
	{
		Options opts = new Options ();

		opts.addOption ( OptionBuilder
			.withDescription ( "Deletes the current submission before (re)loading it, if there is already a version in the DB" )
			.withLongOpt ( "update" )
			.create ( 'u' )
		);
		
		opts.addOption ( OptionBuilder
			.withDescription ( 
				"Only used with -u. Purge the database from dangling (i.e., unreferred) objects, such as ontology terms. " +
				"See the documentation for details." )
			.withLongOpt ( "purge" )
			.create ( 'g' )
		);
		
		opts.addOption ( OptionBuilder
			.withDescription ( "Do the purge operation, even when last purge in the job register is recent" )
			.withLongOpt ( "force-purge" )
			.create ( 'f' )
		);
		
		opts.addOption ( OptionBuilder
			.withDescription ( "Prints out this message" )
			.withLongOpt ( "help" )
			.create ( 'h' )
		);
		
		return opts;		
	}
	
	/**
	 * Loads a sample tab, prints logging messages and alike
	 * @throws ParseException 
	 */
	private static MSI loadSampleTab ( String path ) throws ParseException 
	{
		Loader loader = new Loader();

		long time0 = System.currentTimeMillis ();
		MSI msi = loader.fromSampleData ( path );

		long parsingTime = System.currentTimeMillis () - time0;
		long nitems = msi.getSamples ().size () + msi.getSampleGroups ().size ();
		log.info ( 
			"" + nitems + " samples+groups loaded in " + formatTimeDuration ( parsingTime ) + "." 
		);
		
		return msi;
	}
	
	/**
	 * Deal with a failed persistence attempt, to report error messages and start another attempt, after a random delay,
	 * so that concurrency issues can hopefully be avoided again.
	 * 
	 */
	private static void handleFailedPersistenceAttempt ( int attemptNo, RuntimeException attemptEx, String sampleTabPath )
	{
		if ( attemptNo == 0 )
			throw new RuntimeException ( "Error while saving '" + sampleTabPath + "': " + attemptEx.getMessage (), attemptEx );

		Throwable aex1 = ExceptionUtils.getRootCause ( attemptEx );
		if ( !(aex1 instanceof SQLException) ) throw new RuntimeException ( 
			"Error while saving '" + sampleTabPath + "': " + attemptEx.getMessage (), attemptEx 
		);
		
		log.warn ( "SQL exception: {}, this is likely due to concurrency, will retry {} more times", 
			attemptEx.getMessage (), attemptNo 
		);
		
		// Have a random pause, minimises the likelihood to clash again 
		try {
			Thread.sleep ( RandomUtils.nextInt ( 2500 - 500 + 1 ) + 500 );
		} 
		catch ( InterruptedException sex ) { 
			throw new RuntimeException ( "Internal error while trying to get loader lock:" + sex.getMessage (), sex );
		}		
	}
	
}
