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
import ac.uk.ebi.fg.biosd.sampletab.Persister;


public class PersistenceTest
{	
	private MSI persistSampleTab ( String path ) throws ParseException
	{
		out.println ( "\n\n >>> Loading '" + path + "'" );
		
	  Loader loader = new Loader();
	  MSI msi = loader.fromSampleData ( path );
	  
	  new Persister ( msi ).persist ( true );	  
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
		loadDir ( "/ebi/microarray/home/biosamples/ftp/pride" );
	}
	
}
