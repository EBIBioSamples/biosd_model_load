/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import ac.uk.ebi.fg.biosd.sampletab.Exporter;
import ac.uk.ebi.fg.biosd.sampletab.Loader;
import au.com.bytecode.opencsv.CSVReader;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SampleTabVerifier
{
	private File inputFile;
	
	public SampleTabVerifier ( File inputFile )
	{
		super ();
		this.inputFile = inputFile;
	}

	public void verify ()
	{
		try
		{
			String inputPath = inputFile.getAbsolutePath ();
			
			System.out.println ( ">>>>>>> Working on '" + inputPath + "'" );
			List<String[]> input = getInputFile (), export = getExportedFile ();
			
			int iInScd = getSCDStartIndex ( input, inputPath );
			List<String[]> inputMSI = input.subList ( 0, iInScd ), inputSCD = input.subList ( iInScd + 1, input.size () );
			
			String exportPath = getExportedPath ();
			int iExScd = getSCDStartIndex ( export, exportPath );
			List<String[]> exportMSI = export.subList ( 0, iExScd ), exportSCD = export.subList ( iExScd + 1, export.size () );
			
			new SCDLineCountVerifier ( inputPath, exportPath, inputSCD, exportSCD ).verify ();
			new MSIValuesVerifier ( inputPath, exportPath, inputMSI, exportMSI ).verify ();
			new SCDValuesVerifier ( inputPath, exportPath, inputSCD, exportSCD ).verify ();
		}
		catch ( Exception ex )
		{ 
			StringWriter sw = new StringWriter ();
			ex.printStackTrace ( new PrintWriter ( sw ) );
			
			// "FILE", "RESULT", "MESSAGE TYPE", "FIELD", "MESSAGE/NOTES"
			ParserComparisonTest.report.writeNext ( new String[] { 
				inputFile.getAbsolutePath (),
				"EXCEPTION",
				ex.getClass ().getName (),
				"",
				StringEscapeUtils.escapeCsv ( StringEscapeUtils.escapeJava ( sw.toString () ) ) 
			});
		}
	}
	
	
	private List<String[]> getInputFile () throws Exception
	{
		return readAndCleanUpSampleTab ( new InputStreamReader ( new BufferedInputStream ( new FileInputStream ( 
			inputFile ) ), "UTF-8" ) );
	}
	
	private List<String[]> getExportedFile () throws Exception
	{
		Loader loader = new Loader();
		MSI msi = loader.fromSampleData ( inputFile );
		
		Exporter exporter = new Exporter();
    final SampleData xdata = exporter.fromMSI ( msi );

    // Write to disk, might be needed for manual checking
    //
    String outFilePath = getExportedPath ();
    Writer stwr = new FileWriter ( outFilePath );
    new SampleTabWriter ( stwr ).write ( xdata );
    
    // Now, reimport it, for in-memory verification
    Reader rdr = new FileReader ( outFilePath );
    return readAndCleanUpSampleTab ( rdr );
	}
	
	private String getExportedPath ()
	{
    String outFileName = inputFile.getAbsolutePath ().replace ( '/', '_' );
    return "target/exports/" + outFileName + ".csv";
	}
	
	
	private List<String[]> readAndCleanUpSampleTab ( Reader input ) throws Exception
	{
		CSVReader csvReader = new CSVReader ( input, '\t', '"' );
		List<String[]> result = new ArrayList<String[]> ();
		for ( String[] line; ( line = csvReader.readNext () ) != null; )
		{
			if ( line.length == 0 ) continue;
			boolean allNull = true; 
			for ( String cell: line )
				if ( StringUtils.trimToNull ( cell ) != null ) {
					allNull = false; break;
			}
			if ( allNull ) continue;
			if ( line [ 0 ].startsWith ( "#" ) ) continue;
			result.add ( line );
		}
		return result;
	}
	
	
	private int getSCDStartIndex ( List<String[]> sampleTabLines, String filePath )
	{
		int i = 0;
		for ( String[] line: sampleTabLines )
			if ( "[SCD]".equalsIgnoreCase ( line [ 0 ] ) ) return i;
			else i++;
		throw new RuntimeException ( "Error while reading file '" + filePath + "', cannot find the [SCD] header" );
	}
}
