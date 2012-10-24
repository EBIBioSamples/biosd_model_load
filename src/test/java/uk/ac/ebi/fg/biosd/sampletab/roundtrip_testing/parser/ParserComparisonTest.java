/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ParserComparisonTest
{
	public static final String TEST_FILES_PATH = "/Users/brandizi/Documents/Work/ebi/esd/data_sets/pride";
	
	public static CSVWriter report;

	@BeforeClass
	public static void createExportDir ()
	{
		File exportDir = new File ( "target/exports" );
		if ( !exportDir.exists () ) exportDir.mkdir ();
	}
	
	@Test
	public void testParser () throws Exception
	{
		report = new CSVWriter ( new OutputStreamWriter ( new BufferedOutputStream ( 
			new FileOutputStream ( "target/sampletab_parser_test.csv" )), "UTF-8" ), '\t', '"' );
			
		report.writeNext ( new String[] { "FILE", "RESULT", "MESSAGE TYPE", "FIELD", "MESSAGE/NOTES" } );
		
		File inputDir = new File ( TEST_FILES_PATH );
		
		for ( File sampleTabFile: FileUtils.listFiles ( 
			inputDir, new RegexFileFilter ( ".*sampletab\\.txt", Pattern.CASE_INSENSITIVE ), 
			TrueFileFilter.TRUE )
		)
			new SampleTabVerifier ( sampleTabFile ).verify ();
	}
}
