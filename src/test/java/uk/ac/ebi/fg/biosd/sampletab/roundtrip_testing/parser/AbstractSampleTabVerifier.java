/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.roundtrip_testing.parser;

import java.util.List;

/**
 * {@link ParserComparisonTest} uses subclasses of this, which perform several types of comparison between an input 
 * SampleTab file and one parsed and exported from the input.
 *
 * <dl><dt>date</dt><dd>Oct 24, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class AbstractSampleTabVerifier
{
	protected String inputPath, exportPath;
	protected List<String[]> inputContent, exportContent;
	
	
	public AbstractSampleTabVerifier 
		( String inputPath, String exportPath, List<String[]> inputContent, List<String[]> exportContent )
	{
		super ();
		this.inputPath = inputPath;
		this.exportPath = exportPath;
		this.inputContent = inputContent;
		this.exportContent = exportContent;
	}
	
	public abstract void verify();
}
