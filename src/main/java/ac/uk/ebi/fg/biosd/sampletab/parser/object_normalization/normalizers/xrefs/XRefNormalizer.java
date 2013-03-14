/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.xrefs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import uk.ac.ebi.fg.core_model.xref.XRef;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * 
 * Allow to re-use the {@link ReferenceSource} linked to a {@link XRef}. This is used by {@link ReferrerNormalizer}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class XRefNormalizer extends Normalizer<XRef>
{
	public XRefNormalizer ( Store store ) {
		super ( store );
	}

	/** Check re-usability of Ref Sources */
	@Override
	public void normalize ( XRef xref )
	{
		if ( xref == null || xref.getId () != null ) return;
		
		ReferenceSource src = xref.getSource ();
		if ( src == null ) return; // This is actually an error and will pop-up later.
		ReferenceSource srcS = store.find ( src, src.getAcc (), src.getVersion () );
		if ( srcS == null || src == srcS ) return;
		
		Exception theEx = null;
		try
		{
			Method setPropM = xref.getClass ().getMethod ( "setSource", ReferenceSource.class );
			setPropM.setAccessible ( true );
			setPropM.invoke ( xref, srcS );
		} 
		catch ( SecurityException ex ) { theEx = ex; }
		catch ( NoSuchMethodException ex ) { theEx = ex; }
		catch ( IllegalArgumentException ex ) { theEx = ex; }
		catch ( IllegalAccessException ex ) { theEx = ex; }
		catch ( InvocationTargetException ex ) { theEx = ex; }
		finally {
			if ( theEx != null )
				throw new RuntimeException ( "Internal error while persisting " + xref + ": " + theEx.getMessage (), theEx );
		}
	}

}
