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
public class SCDLineCountVerifier extends AbstractSampleTabVerifier
{
	public SCDLineCountVerifier ( String inputPath, String exportPath, List<String[]> scdInputContent, List<String[]> scdExportContent )
	{
		super ( inputPath, exportPath, scdInputContent, scdExportContent );
	}

	@Override
	public void verify ()
	{
		int inputScdLines = inputContent.size (), exportScdLines = exportContent.size ();
		if ( inputScdLines == exportScdLines ) return; 
		
		ParserComparisonTest.report.writeNext ( new String[] {
			inputPath, "ERROR", "SCD row size mismatch", "", 
			"Input SampleTab file has " + inputScdLines + " [SCD] rows, exported SampleTab has " 
			+ exportScdLines + " rows instead"
		});
	}

}
