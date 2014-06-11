package uk.ac.ebi.fg.biosd.sampletab.loader;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Unloader;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;

/**
 * The SampleTab unload command line manager. This will remove a SampleTab submission from a relational database that
 * maps the BioSD object model. Only those objects that are not linked to other submissions will be removed.
 *
 * <dl><dt>date</dt><dd>Apr 19, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class UnloaderCmd
{
	protected static Logger log = LoggerFactory.getLogger ( LoaderCmd.class );

	public static void main ( String[] args ) throws Throwable
	{
		CommandLineParser clparser = new GnuParser ();
		CommandLine cli = clparser.parse ( getOptions(), args );
		
		if ( cli.hasOption ( 'h' ) ) printUsage ();
		
		String msiAcc = null;
		
		if ( (args = cli.getArgs ()) == null || args.length == 0 ) {
			if ( !cli.hasOption ( 'g' ) ) printUsage ();
		}
		else
			msiAcc = args [ 0 ];
		
		int exCode = 0;
		
		try
		{
			long persistenceTime = 0;
			int nitems = 0;			
			
			EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
			EntityManager em = emf.createEntityManager ();
			
			MSI msi = null; 
			
			if ( msiAcc !=null )
			{
				log.info ( " >>> Unloading '" + msiAcc + "'" );

				AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );
				msi = msiDao.find ( msiAcc );
				if ( msi == null ) {
					exCode = 2;
					throw new RuntimeException ( "Submission with accession '" + msiAcc + "' not found" );
				}
			
				nitems = msi.getSamples ().size () + msi.getSampleGroups ().size ();
				log.info ( "Unloading " + nitems + " samples+groups" );
			}
			else
				log.info ( " >>> Purging the database" );
			
			// Do it
			//
			long time0 = System.currentTimeMillis ();
			new Unloader().setDoPurge ( cli.hasOption ( 'g' ) ).unload ( msi );
			
			persistenceTime = System.currentTimeMillis () - time0;
			log.info ( "Unloading/Purging done in " + LoaderCmd.formatTimeDuration ( persistenceTime ) + "." );
		} 
		catch ( Throwable ex ) 
		{
			log.error ( "Execution failed with the error: " + ex.getMessage (), ex  );
			if ( exCode != 0 ) exCode = 3;
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

		out.println ( "\n\n *** BioSD Relational Database Unloader ***" );
		out.println ( "\nUnloads a SampleTAB submission from the relational database" );
		
		out.println ( "\nSyntax:" );
		out.println ( "\n\tunload.sh <submission accession> | <--purge|-g> [submission accession]" );		
		
		out.println ( "\nOptions:" );
		HelpFormatter helpFormatter = new HelpFormatter ();
		PrintWriter pw = new PrintWriter ( err, true );
		helpFormatter.printOptions ( pw, 100, getOptions (), 2, 4 );
		
		err.println ( "\nSee also hibernate.properites for the configuration of the target database.\n\n" );
		
		System.exit ( 1 ); // TODO: proper exit codes.
	}

	/**
	 * The command line options used for this command.  
	 */
	@SuppressWarnings ( { "static-access" } )
	private static Options getOptions ()
	{
		Options opts = new Options ();

		opts.addOption ( OptionBuilder
			.withDescription ( "Purge the database from dangling (i.e., unreferred) objects, such as ontology terms. See the documentation for details" )
			.withLongOpt ( "purge" )
			.create ( 'g' )
		);

		opts.addOption ( OptionBuilder
			.withDescription ( "Prints out this message" )
			.withLongOpt ( "help" )
			.create ( 'h' )
		);
		
		return opts;		
	}
}
