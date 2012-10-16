/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;


import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import ac.uk.ebi.fg.biosd.sampletab.Export;
import ac.uk.ebi.fg.biosd.sampletab.Load;
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
	private IOException _exportedFileThreadEx;

	
	public SampleTabVerifier ( File inputFile )
	{
		super ();
		this.inputFile = inputFile;
	}

	public void verify ()
	{
		try
		{
			List<String[]> input = getInputFile (), export = getExportedFile ();
			LineCountVerifier.verify ( inputFile.getAbsolutePath (), input, export );
		}
		catch ( Exception ex )
		{ 
			StringWriter sw = new StringWriter ();
			ex.printStackTrace ( new PrintWriter ( sw ) );
			
			// "FILE", "RESULT", "MESSAGE TYPE", "FIELD", "MESSAGE/NOTES"
			ParserTest.report.writeNext ( new String[] { 
				inputFile.getAbsolutePath (),
				"EXCEPTION",
				ex.getClass ().getName (),
				"",
				StringEscapeUtils.escapeCsv ( sw.toString () ) 
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
		Load load = new Load();
		MSI msi = load.fromSampleData ( inputFile );

		Export exporter = new Export();
    final SampleData xdata = exporter.fromMSI ( msi );

    // Reads what the Limpopo-baed renderer has to write, faster and memory-saving 
    //
    PipedWriter pipew = new PipedWriter ();
    PipedReader piper = new PipedReader ( pipew );
		final SampleTabWriter stw = new SampleTabWriter ( pipew );
    
		
    new Thread ( new Runnable() 
    {
			@Override
			public void run () 
			{
				try {
					stw.write ( xdata );
				} 
				catch ( IOException ex ) {
					_exportedFileThreadEx = ex;
				}
			}
		}).start ();
    
    if ( _exportedFileThreadEx != null ) throw _exportedFileThreadEx;
		return readAndCleanUpSampleTab ( piper );
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
}
