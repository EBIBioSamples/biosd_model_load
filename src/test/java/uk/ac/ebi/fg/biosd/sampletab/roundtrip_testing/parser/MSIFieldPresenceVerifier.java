/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;


/**
 * Check the exported SampleTab file for required fields in the MSI section.
 *  
 * <dl><dt>date</dt><dd>Oct 18, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIFieldPresenceVerifier extends AbstractSampleTabVerifier
{
	public MSIFieldPresenceVerifier ( String inputPath, String exportPath, List<String[]> inputContent, List<String[]> exportContent )
	{
		super ( inputPath, exportPath, inputContent, exportContent );
	}

	@Override
	public void verify ()
	{
		@SuppressWarnings ( "serial" )
		Set<String> requiredHeaders = new HashSet<String>() 
		{{
			// add ( "Submission Title".toLowerCase () );
			// TODO: for the moment there is no really required field. 
		}};
		
		for ( String[] line: exportContent )
		{
			if ( line.length < 2 ) continue;
			String header = StringUtils.trimToNull ( line [ 0 ] ), value = StringUtils.trimToNull ( line [ 1 ] );
			if ( header == null || value == null ) continue;
			requiredHeaders.remove ( header.toLowerCase () );
		}
		
		for ( String header: requiredHeaders )
			ParserComparisonTest.report.writeNext ( new String[] {
				inputPath, "ERROR", "Missing Required Field", header, "Required field is missing from the exported SampleTab"
			});
	}
}
