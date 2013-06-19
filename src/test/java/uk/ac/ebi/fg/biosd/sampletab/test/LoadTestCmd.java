/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.test;

import static java.lang.System.out;

import java.io.File;
import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.Loader;
import uk.ac.ebi.fg.biosd.sampletab.persistence.Persister;
import uk.ac.ebi.fg.core_model.resources.Resources;

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
		String path = args [ 0 ];
		String pid = args [ 1 ]; 
				
		String exStr = "", exMsg = "";
		int exCode = 0;
		long parsingTime = 0, persistenceTime = 0;
		int nitems = 0;
		
		EntityManager em = null;
		try
		{
			em = Resources.getInstance ().getEntityManagerFactory ().createEntityManager ();

			// Parse the submission sampletab file.
			//
			out.println ( "\n\n >>> Loading '" + path + "'" );
			
			long time0 = System.currentTimeMillis ();
			
			Loader loader = new Loader();
			MSI msi = loader.fromSampleData ( path );
			parsingTime = System.currentTimeMillis () - time0; 

			nitems = msi.getSamples ().size () + msi.getSampleGroups ().size ();
			
			
			// Now persist it
			//
			time0 = System.currentTimeMillis ();
			new Persister ().persist ( msi );
			persistenceTime = System.currentTimeMillis () - time0;
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
			exMsg = StringEscapeUtils.escapeJava ( exMsg );
			PrintStream rout = new PrintStream ( new File ( "target/biosd.loadTest_out_" + pid + ".sh" ) );
			rout.printf ( "BIOSD_LOAD_RESULT_EXCEPTION=\"%s\"\n", exStr );
			rout.printf ( "BIOSD_LOAD_RESULT_MESSAGE=\"%s\"\n", exMsg );
			rout.printf ( "PARSING_TIME=%f\n", parsingTime / 1000.0 );
			rout.printf ( "PERSISTENCE_TIME=%f\n", persistenceTime / 1000.0 );
			rout.printf ( "N_ITEMS=%d\n", nitems );
			rout.close ();

			// Just to be sure, we've noted some timeouts on Oracle side
			//
			if ( em != null && em.isOpen () )
				em.close ();
			
			EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
			if ( emf != null && emf.isOpen () ) emf.close ();
			
			// This is yielded for sake of completeness, but not much used.
			System.exit ( exCode );
		}
	}
	
	private static Throwable getExceptionRootCause ( Throwable ex )
	{
		for ( Throwable cause; ; ex = cause )
			if ( ( cause = ex.getCause () ) == null ) return ex;
	}
}
