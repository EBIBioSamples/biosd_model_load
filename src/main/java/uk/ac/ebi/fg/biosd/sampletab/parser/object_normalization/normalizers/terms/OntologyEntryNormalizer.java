/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.terms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * Normalises the {@link ReferenceSource} of an {@link OntologyEntry}.
 *
 * <dl><dt>date</dt><dd>Mar 12, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OntologyEntryNormalizer extends Normalizer<OntologyEntry>
{

	public OntologyEntryNormalizer ( Store store ) {
		super ( store );
	}

	/** Check re-usability of OEs and, in turn, of Ref Sources */
	@Override
	public boolean normalize ( OntologyEntry oe )
	{
		if ( oe == null || oe.getId () != null ) return false;
		
		ReferenceSource src = oe.getSource ();
		if ( src == null ) return true; // This is actually an error and will pop-up later.
		
		ReferenceSource srcS = store.find ( src, src.getAcc (), src.getVersion () );
		if ( srcS == null || src == srcS ) return true;
		
		// The setter is protected, so we need to tweak it via reflection.
		Exception theEx = null;
		try
		{
			Method setPropM = OntologyEntry.class.getDeclaredMethod ( "setSource", ReferenceSource.class );
			setPropM.setAccessible ( true );
			setPropM.invoke ( oe, srcS );
		} 
		catch ( SecurityException ex ) { theEx = ex; }
		catch ( NoSuchMethodException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new RuntimeException ( "Internal error while persisting " + oe + ": " + theEx.getMessage (), theEx );
		}
		
		return true;
	}

}
