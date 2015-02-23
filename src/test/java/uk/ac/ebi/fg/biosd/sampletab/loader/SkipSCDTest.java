package uk.ac.ebi.fg.biosd.sampletab.loader;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>5 Sep 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class SkipSCDTest
{
	@Test
	public void testSkipSCD () throws ParseException
	{
		Loader loader = new Loader ();
		loader.setSkipSCD ( true );
		
    URL url = this.getClass().getClassLoader ().getResource( "GAE-MTAB-27/sampletab.txt" );
		MSI msi = loader.fromSampleData ( url );
		
		assertEquals ( "MSI not loaded!", "GAE-MTAB-27", msi.getAcc () );
		assertTrue ( "We got samples anyway!", msi.getSamples ().isEmpty () );
		assertTrue ( "We got sample groups anyway!", msi.getSampleGroups ().isEmpty () );
	}
}
