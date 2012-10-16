/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.List;


/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class LineCountVerifier
{
	public static void verify ( String inputPath, List<String[]> input, List<String[]> export )
	{
		if ( input.size () == export.size () ) return;
		
		ParserTest.report.writeNext ( new String[] {
			inputPath, "ERROR", "Row size mismatch", "", "Input SampleTab file has " + input.size () + " rows, exported SampleTab" + 
		  "has " + export.size () + " rows instead"
		});
		
	}
}
