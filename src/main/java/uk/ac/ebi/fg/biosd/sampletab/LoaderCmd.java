/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab;

import static java.lang.System.out;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.time.DurationFormatUtils;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Persister;
import uk.ac.ebi.fg.core_model.resources.Resources;

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

	public static void main ( String[] args ) throws Throwable
	{
		if ( args == null || args.length == 0 )
			printUsage ();

		String path = args [ 0 ];
		int exCode = 0;
		
		try
		{
			long parsingTime = 0, persistenceTime = 0;
			int nitems = 0;

			// Parse the submission sampletab file.
			//
			out.println ( "\n\n >>> Loading '" + path + "'" );
			
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
		} 
		catch ( Throwable ex ) 
		{
			ex.printStackTrace( System.err );
			exCode = 1;
		}
		finally 
		{
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
}
