/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Normalizer;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 26, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class FilterNormalizer<I extends Identifiable> extends Normalizer<I>
{
	protected List<Normalizer<?>> normalizers = new LinkedList<Normalizer<?>> ();

	public FilterNormalizer ( Store store )
	{
		super ( store );
	}

	@Override
	@SuppressWarnings ( { "rawtypes", "unchecked" } )
	public void normalize ( I target )
	{
		if ( target == null || target.getId () != null ) return; 
		
		for ( Normalizer n: normalizers )
			n.normalize ( target );
	}
	
}
