package uk.ac.ebi.fg.biosd.sampletab;

import static java.lang.System.out;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import ac.uk.ebi.fg.biosd.sampletab.Loader;
import ac.uk.ebi.fg.biosd.sampletab.persistence.Persister;

/**
 * Performs some real-submission DB-persistence test.
 */
public class PersistenceTest
{	
	private MSI persistSampleTab ( String path ) throws ParseException
	{
		out.println ( "\n\n >>> Loading '" + path + "'" );
		
	  Loader loader = new Loader();
	  MSI msi = loader.fromSampleData ( path );
	  
	  new Persister ( msi ).persist ();	  
	  return msi;
	}
   
	private void loadDir ( String path ) throws ParseException
	{
		File dir = new File ( path );
		
		for ( File file: FileUtils.listFiles ( 
			dir, new RegexFileFilter ( "^.*\\.?sampletab\\.txt$", IOCase.INSENSITIVE ), 
			DirectoryFileFilter.DIRECTORY 
		))
			persistSampleTab ( file.getAbsolutePath () );
	}
	
	@Ignore ( "Not currently working" )
	@Test
	public void testIMSR () throws ParseException {
		loadDir ( "/Users/brandizi/Documents/Work/ebi/esd/data_sets/IMSR" );
	}
	
	@Ignore ( "Not currently working" )
	@Test
	public void testPride () throws ParseException {
		loadDir ( "/ebi/ftp/pub/databases/biosamples/pride" );
	}
	
	/**
	 * Tests a case where there is an organisation without a contact role 
	 * (https://www.pivotaltracker.com/projects/116186#!/stories/37588597)
	 * 
	 */
	@Test
	public void testNullContactRole () throws ParseException {
		persistSampleTab ( "target/test-classes/GAE-MTAB-27_null_org.sampletab.csv" );
	}
	
}
