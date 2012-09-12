package uk.ac.ebi.fg.biosd.sampletab;

import java.io.File;
import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.io.PatternFilenameFilter;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.core_model.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;
import uk.ac.ebi.utils.test.junit.TestEntityMgrProvider;

import ac.uk.ebi.fg.biosd.sampletab.Load;

import static junit.framework.Assert.*;
import static java.lang.System.out;


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
	  EntityTransaction ts = em.getTransaction ();
	  AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( MSI.class,  emProvider.newEntityManager () );
	  
	  ts.begin ();
	  dao.getOrCreate ( msi );
	  ts.commit ();
	  
	  return msi;
	}
   
	private void loadDir ( String path ) throws ParseException
	{
		File dir = new File ( path );
		for ( String filePath: dir.list ( new PatternFilenameFilter ( ".*\\.sampletab.txt" )  ) )
			persistSampleTab ( path + "/" + filePath );
	}
	
	@Ignore ( "This is a heavy time-consuming test and it's usually disabled" )
	@Test
	public void testIMSR () throws ParseException
	{
		loadDir ( "/Users/brandizi/Documents/Work/ebi/esd/data_sets/IMSR" );
	}
}
