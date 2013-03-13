/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization;

import uk.ac.ebi.fg.core_model.toplevel.Identifiable;


/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class Normalizer<I extends Identifiable>
{
	protected Store store;
	
	public Normalizer ( Store store )
	{
		super ();
		this.store = store;
	}

	public abstract void normalize ( I target );
	
	public Store getObjectStore ()
	{
		return store;
	}
}
