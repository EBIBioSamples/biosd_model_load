/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Performs sample-tab parsing tests, by comparing original SampleTab files to what the exporter produced after having loaded
 * such files.
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ParserComparisonTest
{
	public static final String TEST_FILES_PATH = "/Users/brandizi/Documents/Work/ebi/esd/data_sets/pride";
	public static final String BIOSD_FTP_PATH = "/ebi/microarray/home/biosamples/ftp";
	
	public static CSVWriter report;

	@BeforeClass
	public static void createExportDir ()
	{
		File exportDir = new File ( "target/exports" );
		if ( !exportDir.exists () ) exportDir.mkdir ();
	}
	
	@Before
	public void initReportFile () throws Exception
	{
		report = new CSVWriter ( new OutputStreamWriter ( new BufferedOutputStream ( 
			new FileOutputStream ( "target/sampletab_parser_test.csv" )), "UTF-8" ), '\t', '"' );
			
		report.writeNext ( new String[] { "FILE", "RESULT", "MESSAGE TYPE", "FIELD", "MESSAGE/NOTES" } );
	}
	
	@After
	public void closeReportFile() throws Exception {
		report.close ();
	}
	
	/**
	 * Performs a comparison test over the files located at {@link #TEST_FILES_PATH}.
	 */
	@Test @Ignore ( "Time-consuming test, normally disabled" )
	public void testParser () throws Exception
	{
		File inputDir = new File ( TEST_FILES_PATH );
		//DEBUG inputDir = new File ( "/tmp" );
		
		for ( File sampleTabFile: FileUtils.listFiles ( 
			inputDir, new RegexFileFilter ( ".*sampletab\\.txt", Pattern.CASE_INSENSITIVE ), 
			TrueFileFilter.TRUE )
		)
			new SampleTabVerifier ( sampleTabFile ).verify ();
	}
	
	/**
	 * Performs a comparison test over the files located at {@link #BIOSD_FTP_PATH}.
	 */
	@Test @Ignore ( "Time-consuming test, normally disabled" )
	public void testParserAgainstProduction () throws Exception
	{
		File inputDir = new File ( BIOSD_FTP_PATH );
		
		Random rnd = new Random ( System.currentTimeMillis () );
		
		for ( File sampleTabFile: FileUtils.listFiles ( 
			inputDir, new RegexFileFilter ( ".*sampletab\\.txt", Pattern.CASE_INSENSITIVE ), 
			TrueFileFilter.TRUE )
		)
			// We're testing only a random sample of files, cause they're a lot (about 45000) 
			if ( rnd.nextFloat () < 10f/100 )
				new SampleTabVerifier ( sampleTabFile ).verify ();
	}
	
}
