/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Value comparer specific to the SampleTab's SCD section.
 *
 * <dl><dt>date</dt><dd>Oct 24, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SCDValuesVerifier extends AbstractValuesVerifier
{
	public SCDValuesVerifier ( String inputPath, String exportPath, List<String[]> inputContent, List<String[]> exportContent )
	{
		super ( inputPath, exportPath, inputContent, exportContent );
	}

	/**
	 * Gets spreadsheet SCD values in the form of header-&gt;{ distinct values }. 
	 * Returns null if the table is null or empty.
	 */
	@Override
	protected Map<String, Set<String>> getValues ( List<String[]> table )
	{
		if ( table == null || table.size () == 0 )
			return null;

		Map<String, Set<String>> result = new HashMap<String, Set<String>> ();

		// Check for null-header columns, prepare the header->values map.
		//
		String[] headers = table.get ( 0 );
		for ( int i = 0; i < headers.length; i++ ) 
		{
			String header = headers [ i ] = StringUtils.trimToNull ( headers [ i ] );
			
			if ( header == null )
			{
				ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Columns without an header", "[" + getSectionName () + "]", 
						"The column " + i + " in the SampleTAB file has an empty header"
					});
				continue;
			}
			header = headers [ i ] = header.trim().toLowerCase ();
			
			Set<String> vals = result.get ( header );
			if ( vals == null ) result.put ( header, vals = new HashSet<String> () );
		}
		
		// Collect the values for every header and put them in the header->values map.
		//
		boolean lenghtErrorMatched = false;
		for ( int irow = 1; irow < table.size (); irow++ )
		{
			String[] line = table.get ( irow );
			for ( int icol = 0; icol < line.length; icol++ ) 
			{
				if ( line [ icol ] == null || "".equals ( line [ icol ] ) ) continue;

				if ( icol >= headers.length )
				{
					if ( !lenghtErrorMatched )
						ParserComparisonTest.report.writeNext ( new String[] {
							inputPath, "ERROR", "Mismatch of Line and Header lenghts", "[" + getSectionName () + "]", 
							"The SampleTAB file has lines longer than the number of headers"
						});
					lenghtErrorMatched = true;
					break;
				}
				if ( headers [ icol ] == null ) continue;
				
				result.get ( headers [ icol ] ).add ( line [ icol ] );
			}
		}
		return result;
	}

	@Override
	protected String getSectionName () {
		return "SCD";
	}
	
}
