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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import com.opencsv.CSVReader;

import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.sampletab.exporter.Exporter;
import uk.ac.ebi.fg.biosd.sampletab.loader.Loader;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.AccessibleDAO;
import uk.ac.ebi.fg.core_model.resources.Resources;

/**
 * Compares an original SampleTab file to the one exported after SampleTab parsing.
 * This is a root component that invokes other {@link AbstractSampleTabVerifier}. 
 * 
 * Results are reported in a {@link ParserComparisonTest#report CSV file}.
 *
 * <dl><dt>date</dt><dd>Oct 16, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SampleTabVerifier
{
	private File inputFile;
	private boolean useDb = false;
	
	public SampleTabVerifier ( File inputFile, boolean useDb )
	{
		super ();
		this.inputFile = inputFile;
		this.useDb = useDb;
	}
	
	public SampleTabVerifier ( File inputFile )
	{
		this ( inputFile, false );
	}
	

	public void verify ()
	{
		try
		{
			// Get the files to compare
			//
			String inputPath = inputFile.getAbsolutePath ();
			
			System.out.println ( ">>>>>>> Working on '" + inputPath + "'" );
			List<String[]> input = getInputFile (), export = getExportedFile ();
			
			int iInScd = getSCDStartIndex ( input, inputPath );
			List<String[]> inputMSI = input.subList ( 0, iInScd ), inputSCD = input.subList ( iInScd + 1, input.size () );
			
			String exportPath = getExportedPath ();
			int iExScd = getSCDStartIndex ( export, exportPath );
			List<String[]> exportMSI = export.subList ( 0, iExScd ), exportSCD = export.subList ( iExScd + 1, export.size () );
			
			// Specific verifications
			//
			new SCDLineCountVerifier ( inputPath, exportPath, inputSCD, exportSCD ).verify ();
			new MSIValuesVerifier ( inputPath, exportPath, inputMSI, exportMSI ).verify ();
			new SCDValuesVerifier ( inputPath, exportPath, inputSCD, exportSCD ).verify ();
		}
		catch ( Exception ex )
		{ 
			// Reports 
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
	
	/**
	 * Gets the original file from {@link #inputFile}.
	 */
	private List<String[]> getInputFile () throws Exception
	{
		return readAndCleanUpSampleTab ( new InputStreamReader ( new BufferedInputStream ( new FileInputStream ( 
			inputFile ) ), "UTF-8" ) );
	}

	/**
	 * Parse the input and export it to a list of strings. 
	 */
	private List<String[]> getExportedFile () throws Exception
	{
		Loader loader = new Loader();
		MSI msi = loader.fromSampleData ( inputFile );
		
		if ( useDb ) 
		{
			AccessibleDAO<MSI> dao = new AccessibleDAO<MSI> ( 
				MSI.class, Resources.getInstance ().getEntityManagerFactory ().createEntityManager () );
			String acc = msi.getAcc ();
			msi = dao.find ( acc );
			if ( msi == null ) throw new IllegalArgumentException ( String.format ( 
				"Cannot find the accession '%s' in the database", acc 
			));
		}
		
		
		Exporter exporter = new Exporter();
    final SampleData xdata = exporter.fromMSI ( msi );

    // Write to disk, might be needed for manual checking
    //
    String outFilePath = getExportedPath ();
    Writer stwr = new FileWriter ( outFilePath );
    
    SampleTabWriter stwrWrap = new SampleTabWriter ( stwr );
    stwrWrap.write ( xdata );
    stwrWrap.close ();
    
    // Now, re-import it, for in-memory verification
    Reader rdr = new FileReader ( outFilePath );
    return readAndCleanUpSampleTab ( rdr );
	}
	
	private String getExportedPath ()
	{
    String outFileName = inputFile.getAbsolutePath ().replace ( '/', '_' );
    return ParserComparisonTest.TEST_EXPORT_PATH + "/" + outFileName + ".csv";
	}
	
	
	/**
	 * Some clean-up of a SampleTab structure: 
	 * <ul>
	 * 	<li>Removes lines that have only blank cell values.</li>
	 * 	<li>Removes comment lines (i.e., starting with a '#').</li>  
	 * </ul>  
	 */
	private List<String[]> readAndCleanUpSampleTab ( Reader input ) throws Exception
	{
		CSVReader csvReader = new CSVReader ( input, '\t', '"' );
		List<String[]> result = new ArrayList<String[]> ();
		
		for ( String[] line; ( line = csvReader.readNext () ) != null; )
		{
			// All-blank lines
			if ( line.length == 0 ) continue;
			boolean allNull = true; 
			for ( String cell: line )
				if ( StringUtils.trimToNull ( cell ) != null ) {
					allNull = false; break;
			}
			if ( allNull ) continue;
			
			// Comment lines
			if ( line [ 0 ].startsWith ( "#" ) ) continue;
			result.add ( line );
		}
		
		csvReader.close ();
		return result;
	}
	
	/**
	 * Tells where the line with '[SCD]' in the first cell is located. 
	 */
	private int getSCDStartIndex ( List<String[]> sampleTabLines, String filePath )
	{
		int i = 0;
		for ( String[] line: sampleTabLines )
			if ( "[SCD]".equalsIgnoreCase ( line [ 0 ] ) ) return i;
			else i++;
		throw new RuntimeException ( "Error while reading file '" + filePath + "', cannot find the [SCD] header" );
	}
}
