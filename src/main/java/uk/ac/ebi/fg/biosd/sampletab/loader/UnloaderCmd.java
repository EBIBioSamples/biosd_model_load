/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.loader;

import static java.lang.System.out;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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

	public static void main ( String[] args ) throws Throwable
	{
		if ( args == null || args.length == 0 )
			printUsage ();

		String msiAcc = args [ 0 ];
		int exCode = 0;
		
		try
		{
			long persistenceTime = 0;
			int nitems = 0;

			// Parse the submission sampletab file.
			//
			out.println ( "\n\n >>> Unloading '" + msiAcc + "'" );
			
			
			// Get the number of items
			//
			EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
			EntityManager em = emf.createEntityManager ();
			AccessibleDAO<MSI> msiDao = new AccessibleDAO<MSI> ( MSI.class, em );
			MSI msi = msiDao.find ( msiAcc );
			if ( msi == null ) {
				exCode = 2;
				throw new RuntimeException ( "Submission with accession '" + msiAcc + "' not found" );
			}
			
			nitems = msi.getSamples ().size () + msi.getSampleGroups ().size ();
			out.println ( "\nUnloading " + nitems + " samples+groups" );
			
			// Now persist it
			//
			long time0 = System.currentTimeMillis ();
			new Unloader().unload ( msi );
			
			persistenceTime = System.currentTimeMillis () - time0;
			out.println ( "\nSubmission unloaded in " + LoaderCmd.formatTimeDuration ( persistenceTime ) + "." );
		} 
		catch ( Throwable ex ) 
		{
			ex.printStackTrace( System.err );
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
		
		out.println ( "Syntax:" );
		out.println ( "\n\tunload.sh <submission accession>\n\n" );
		out.println ( "See also hibernate.properites for the configuration of the target database.\n\n" );
		
		System.exit ( 1 ); // TODO: proper exit codes.
	}

}
