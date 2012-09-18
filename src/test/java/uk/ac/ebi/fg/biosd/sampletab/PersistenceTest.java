package uk.ac.ebi.fg.biosd.sampletab;

import static java.lang.System.out;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;
import ac.uk.ebi.fg.biosd.sampletab.Load;


public class PersistenceTest
{
	@Rule
	public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( Resources.getInstance ().getEntityManagerFactory () );
	private EntityManager em;
	
	private MSI persistSampleTab ( String path ) throws ParseException
	{
		out.println ( "\n\n >>> Loading '" + path + "'" );
		
	  Load load = new Load();
	  MSI msi = load.fromSampleData ( path );
	  
	  em = emProvider.newEntityManager ();
	  AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  em );
	  EntityTransaction ts = em.getTransaction ();
	  ts.begin ();
	  dao.getOrCreate ( msi );
	  ts.commit ();
	  
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
