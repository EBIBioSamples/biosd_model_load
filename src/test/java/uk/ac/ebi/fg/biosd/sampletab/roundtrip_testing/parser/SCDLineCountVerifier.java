/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.List;


/**
 * Check the exported SampleTab file for required fields in the SCD section.
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
		int inputScdSz = inputContent.size (), exportScdSz = exportContent.size ();
		
		// The zero-size case can be ignored here, cause there is another check into SCDValuesVerifier that takes this case
		// into account.
		//
		if ( inputScdSz == exportScdSz || inputScdSz == 0 || exportScdSz == 0 ) return; 
		
		ParserComparisonTest.report.writeNext ( new String[] {
			inputPath, "ERROR", "SCD row size mismatch", "", 
			"Input SampleTab file has " + inputScdSz + " [SCD] rows, exported SampleTab has " 
			+ exportScdSz + " rows instead"
		});
	}

}
