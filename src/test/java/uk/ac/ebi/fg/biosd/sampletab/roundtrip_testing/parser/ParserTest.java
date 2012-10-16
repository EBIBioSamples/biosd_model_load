/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ParserTest
{
	public static final String TEST_FILES_PATH = "";
	
	public static CSVWriter report;

	@Test
	public void testParser () throws Exception
	{
		report = new CSVWriter ( new OutputStreamWriter ( new BufferedOutputStream ( 
			new FileOutputStream ( "target/sampletab_parser_test.csv" )), "UTF-8" ), '\t', '"' );
			
		report.writeNext ( new String[] { "FILE", "RESULT", "MESSAGE TYPE", "FIELD", "MESSAGE/NOTES" } );
		
		File inputDir = new File ( TEST_FILES_PATH );
		
		for ( File sampleTabFile: FileUtils.listFiles ( inputDir, new String [] { "sampletab.txt" }, true ) )
			new SampleTabVerifier ( sampleTabFile ).verify ();
	}
}
