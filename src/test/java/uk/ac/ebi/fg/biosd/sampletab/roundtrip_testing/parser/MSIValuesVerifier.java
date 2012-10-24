/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Oct 24, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class MSIValuesVerifier extends AbstractValuesVerifier
{
	public MSIValuesVerifier ( String inputPath, String exportPath, List<String[]> msiInputContent, List<String[]> msiExportContent )
	{
		super ( inputPath, exportPath, msiInputContent, msiExportContent );
	}

	/**
	 * Gets spreadsheet MSI values in the form of header-&gt;{ distinct values }. 
	 * Returns null if the table is null or empty.
	 */
	@Override
	protected Map<String, Set<String>> getValues ( List<String[]> table )
	{
		if ( table == null || table.size () == 0 )
			return null;
			
		Map<String, Set<String>> result = new HashMap<String, Set<String>> ();
		for ( String[] line: table )
		{
			String header = line [ 0 ].trim().toLowerCase ();
			Set<String> vals = result.get ( header );
			if ( vals == null ) result.put ( header, vals = new HashSet<String> () );
			for ( int i = 1; i < line.length; i++ )
				if ( line [ i ] != null && !"".equals ( line [ i ] ) ) 
					vals.add ( line [ i ] );
		}
		return result;
	}

	@Override
	protected String getSectionName () {
		return "MSI";
	}

}
