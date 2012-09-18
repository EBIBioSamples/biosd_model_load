/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.test;

import static java.lang.System.out;

import java.io.File;
import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import ac.uk.ebi.fg.biosd.sampletab.Load;

/**
 * Uses to perform loading tests, one submission at a time. See load_test.sh for details.
 *
 * <dl><dt>date</dt><dd>Sep 17, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class LoadTestCmd
{

	public static void main ( String[] args ) throws Throwable
	{
		String exStr = "", exMsg = "";
		int exCode = 0;
		
		try
		{
			// Parse the submission sampletab file.
			//
			String path = args [ 0 ];
			out.println ( "\n\n >>> Loading '" + path + "'" );
			
			Load load = new Load();
			MSI msi = load.fromSampleData ( path );
			
			// Now persist it
			//
			EntityManager em = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();
			AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
			EntityTransaction ts = em.getTransaction ();
			ts.begin ();
			dao.getOrCreate ( msi );
			ts.commit ();
		} 
		catch ( Throwable ex ) 
		{
			ex.printStackTrace();
			ex = getExceptionRootCause ( ex );
			exStr = ex.getClass ().getName ();
			exMsg = ex.getMessage ();
			exCode = 1;
		}
		finally 
		{
			out.println ( ">>>> Writing Results to Communication File" );
			
			// This is for load_test.sh, it will get the error message and add to a summery file, which lists results of each
			// input file.
			//
			PrintStream rout = new PrintStream ( new File ( "/tmp/biosd.loadTest_out.sh" ) );
			rout.printf ( "BIOSD_LOAD_RESULT_EXCEPTION=\"%s\"\n", exStr );
			rout.printf ( "BIOSD_LOAD_RESULT_MESSAGE=\"%s\"\n", exMsg );
			rout.close ();
			
			// This si yielded for sake of completeness, but not much used.
			System.exit ( exCode );
		}
	}
	
	private static Throwable getExceptionRootCause ( Throwable ex )
	{
		for ( Throwable cause; ; ex = cause )
			if ( ( cause = ex.getCause () ) == null ) return ex;
	}
}
