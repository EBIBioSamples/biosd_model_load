/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.ObjectNormalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 26, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class FilterNormalizer<I extends Identifiable> extends ObjectNormalizer<I>
{
	protected List<ObjectNormalizer<?>> normalizers = new LinkedList<ObjectNormalizer<?>> ();

	public FilterNormalizer ( Store store )
	{
		super ( store );
	}

	@Override
	@SuppressWarnings ( { "rawtypes", "unchecked" } )
	public void normalize ( I target )
	{
		if ( target == null || target.getId () != null ) return; 
		
		for ( ObjectNormalizer n: normalizers )
			n.normalize ( target );
	}
	
}
