/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;


/**
 * This is a base class to compare logically values in input/exported files. Subclasses of this are defined for specific
 * sections of the SampleTab file. What such subclasses do is essentially implementing {@link #getValues(List)}. 
 *
 * <dl><dt>date</dt><dd>Oct 22, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class AbstractValuesVerifier extends AbstractSampleTabVerifier
{
	public AbstractValuesVerifier ( String inputPath, String exportPath, List<String[]> inputContent, List<String[]> exportContent )
	{
		super ( inputPath, exportPath, inputContent, exportContent );
	}
	
	/**
	 * Extract MSI values via {@link #getValues(List)} and compares input to output logically, reporting differences, 
	 * e.g., if a field is in both sides and with the same values
	 *  
	 */
	public void verify ()
	{
		String sectionName = getSectionName ();
		
		Map<String, Set<String>> inputValues = getValues ( inputContent ), exportValues = getValues ( exportContent );
		
		if ( inputValues == null )
		{
			// export table might be non-null, but this is rather normal, means some defaults were put in it
			//
			if ( exportValues != null )
				ParserComparisonTest.report.writeNext ( new String[] {
					inputPath, "WARNING", "Input " + sectionName + " is null and Exporter is not", "[" + sectionName + "]", 
					sectionName + " section is empty in the input file, while is not null in the exported file"
				});
			return;
		}
		
		if ( exportValues == null )
		{
			// export is null and input is not, it's an error
			ParserComparisonTest.report.writeNext ( new String[] {
				inputPath, "ERROR", "No " + sectionName + " export", "[" + sectionName +"]", 
				sectionName + " section is empty in the export file, while is not null in the input file"
			});
			return;
		}
		
		
		for ( String header: inputValues.keySet () )
		{
			Set<String> hInputVals = inputValues.get ( header );
			int hInputValsSz = hInputVals == null ? 0 : hInputVals.size ();
			
			Set<String> hExportVals = exportValues.get ( header );
			int hExportValsSz = hExportVals == null ? 0 : hExportVals.size ();
			
			if ( hInputValsSz == 0 && hExportValsSz == 0 ) continue;
			
			if ( hExportValsSz == 0 ) 
			{
				// input has some value, let's see if it's only 1 or more
				//
				
				if ( hInputValsSz == 1 )
				{
					String displayVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
						StringUtils.abbreviate ( hInputVals.iterator ().next (), 40 ) ) );
					ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Value Not Exported", "[" + sectionName + "] " + header, 
						"The value '" + displayVal + "' in the input file is not exported"
					});
				}
				else
					ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Header Not Exported", "[" + sectionName + "] " + header, 
						"The header '" + header + "' and its values in the input file are not exported"
					});
				
				continue;
			}
				

			// Output has some value, let's see the input
			// 
			if ( hInputValsSz == 0 )
			{
				// Exported values that are not in the input, let's look at the size
				//
				if ( hExportValsSz == 1 ) 
				{
					String displayVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
						StringUtils.abbreviate ( hExportVals.iterator ().next (), 40 ) ) );
					ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Extra-value in the Exporter", "[" + sectionName + "] " + header, 
						"The value '" + displayVal + "' in the export file doesn't come from the input"
					});
				}
				else
					ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Header in the Exporter only", "[" + sectionName + "] " + header, 
						"The header '" + header + "' and its values in the export don't come from the input"
					});
				continue;
			}
				
			
			// Both input and exported values are non-empty, check the values
			//
			
			if ( hInputValsSz == hExportValsSz )
			{
				// Check the values are the same
				//
				
				if ( hInputValsSz == 1 )
				{
					// 1-value only on both sides
					// 
					
					String inval = hInputVals.iterator ().next (), xval = hExportVals.iterator ().next ();
					if ( !inval.equals ( xval ) )
					{
						String displayInVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
							StringUtils.abbreviate ( inval, 40 ) ) );
						String displayXVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
							StringUtils.abbreviate ( xval, 40 ) ) );
						String msg = inval.startsWith ( xval ) 
							? "The exported sample-tab contains a truncated version of the input value '" + inval + "'" 
							: "The input value '" + displayInVal + "' and the exported value '" + displayXVal + "' are different";
						ParserComparisonTest.report.writeNext ( new String[] {
							inputPath, "ERROR", "Mismatching Values", "[" + sectionName + "] " + header, msg
						});
					}

					continue;
				}
				
				// multiple values on either side (and same-size value sets)
				// 
				
				for ( String inval: hInputVals )
					if ( !hExportVals.contains ( inval ) )
					{
						String displayVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
							StringUtils.abbreviate ( inval, 40 ) ) );
						ParserComparisonTest.report.writeNext ( new String[] {
							inputPath, "ERROR", "Extra-value in the Exporter", "[" + sectionName + "] " + header, 
							"The value '" + displayVal + "for the header '" + header + "' is not exported"
						});
					}
					else
						hExportVals.remove ( inval );
				
				for ( String xval: hExportVals )
				{
					String displayVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
						StringUtils.abbreviate ( xval, 40 ) ) );
					ParserComparisonTest.report.writeNext ( new String[] {
						inputPath, "ERROR", "Extra-value in the Exporter", "[" + sectionName + "] " + header, 
						"The value '" + displayVal + "' in the export file doesn't come from the input"
					});
				}	
				
				continue;
			} // hInputValsSz == hExportValsSz
			
			// Value sets with different sizes, report the mismatch without details. 
			ParserComparisonTest.report.writeNext ( new String[] {
				inputPath, "ERROR", "Mismatching Values", "[" + sectionName + "] " + header, 
				"The header has different sets of values in the input and exported file"
			});
			
		} // for each header
		
		
		// Now check that those exported headers that don't appear in the input
		//
		for ( String header: exportValues.keySet () )
		{
			if ( inputValues.containsKey ( header ) ) continue;

			// This header is not found in the input, so there's extra-stuff in the export
			// 
			Set<String> hExportVals = exportValues.get ( header );

			// OK, it's empty, so let's ignore it.
			if ( hExportVals == null || hExportVals.size () == 0 ) continue;
			
			
			if ( hExportVals.size () == 1 )
			{
				// A single extra-value
				// 
				String displayVal = StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( 
					StringUtils.abbreviate ( hExportVals.iterator ().next (), 40 ) ) );
				
				ParserComparisonTest.report.writeNext ( new String[] {
					inputPath, "ERROR", "Extra-value in the Exporter", "[" + sectionName + "] " + header, 
					"The value '" + displayVal + "' in the export file doesn't come from the input"
				});
				
				continue;
			}
			
			// Multiple extra values 
			// 
			ParserComparisonTest.report.writeNext ( new String[] {
				inputPath, "ERROR", "Extra-header in the Exporter", "[" + sectionName + "] " + header, 
				"The header in the exported file but not in the input"
			});
			
		} // for export headers
	}

	
	/**
	 * Returns "MSI" or "SCD" and it's used in {@link #verify()} to format output messages  
	 */
	protected abstract String getSectionName ();
	
	
	/**
	 * Gets spreadsheet values in the form of header-&gt;{ distinct values }. 
	 * Returns null if the table is null or empty. This method has specific implementations
	 * for the MSI and SCD sections.
	 */
	protected abstract Map<String, Set<String>> getValues ( List<String[]> table );
}
