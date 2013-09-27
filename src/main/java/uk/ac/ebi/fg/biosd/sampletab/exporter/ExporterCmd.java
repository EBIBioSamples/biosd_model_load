package uk.ac.ebi.fg.biosd.sampletab.exporter;

import static java.lang.System.out;

import java.io.FileWriter;
import java.io.Writer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.time.DurationFormatUtils;

import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Persister;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;

/**
 * 
 * The SampleTab exporter command line manager. This will export a SampleTab file from a relational database that
 * maps the BioSD object model.
 *
 * <dl><dt>date</dt><dd>Sep 27, 2013</dd></dl>
 * @author Adam Faulconbridge
 *
 */
public class ExporterCmd
{

	public static void main ( String[] args ) throws Throwable
	{
		if ( args == null || args.length != 2 )
			printUsage ();

		String accession = args [ 0 ];
        String path = args [ 1 ];
		int exCode = 0;
		
		try
		{
			long parsingTime = 0, persistenceTime = 0;

			// Parse the submission sampletab file.
			//
			out.println ( "\n\n >>> Exporting '" + accession + "' to '" + path + "'" );
			
			long time0 = System.currentTimeMillis ();
			
			EntityManager   em = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();
			AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );
			MSI msi = msiDao.find ( accession ); // Or ID

			Exporter exporter = new Exporter();
			SampleData xdata = exporter.fromMSI ( msi );
			SampleTabWriter stwr = new SampleTabWriter (new FileWriter ( path ));
			stwr.write ( xdata );
			
			
			persistenceTime = System.currentTimeMillis () - time0;
			out.println ( 
				"\nSubmission exported in " + formatTimeDuration ( persistenceTime ) + ". Total time " +
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
