package ac.uk.ebi.fg.biosd.sampletab;

import static java.lang.System.out;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.biosd.model.persistence.hibernate.access_control.cli.AccessControlCLI;
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
public class PermsCmd
{

	public static void main ( String[] args ) throws Throwable
	{
		if ( args == null || args.length == 0 )
			printUsage ();

		int exCode = 0;
		
		try
		{
			String cmdStr = StringUtils.join ( args, ' ' );
			
			EntityManagerFactory emf = Resources.getInstance ().getEntityManagerFactory ();
			EntityManager em = emf.createEntityManager ();

			AccessControlCLI cli = new AccessControlCLI ( em );
			cli.run ( cmdStr );
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

		out.println ( "\n\n *** BioSD Relational Database Access Control Command ***" );
		out.println ( "\nAllows to manipulate the database users and entity permissions" );
		
		out.println ( "Syntax:" );
		
		out.println ( "\n\tperms <command>\n" );
		
		out.println ( "where <command> is one of:\n" );
		out.println ( AccessControlCLI.getSyntax () );
		
		out.println ( "See also hibernate.properites for the configuration of the target database.\n\n" );
		
		System.exit ( 1 ); // TODO: proper exit codes.
	}

}
