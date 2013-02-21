/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel;

import java.util.Comparator;

import uk.ac.ebi.fg.core_model.toplevel.Accessible;

/**
 * A base comparator, used for other comparators of entities derived from {@link Accessible}.
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class AccessibleComparator<A extends Accessible> implements Comparator<A>
{

	@Override
	public int compare ( A a1, A a2 )
	{
		if ( a1 == null ) return a2 == null ? 0 : -1;
		if ( a2 == null ) return a1 == null ? 0 : +1;
		if ( a1.equals ( a2 ) ) return 0;
		String acc1 = a1.getAcc (), acc2 = a2.getAcc ();
		if ( acc1 == null ) return acc2 == null ? 0 : -1;
		if ( acc2 == null ) return acc1 == null ? 0 : +1;
		return acc1.compareTo ( acc2 );
	}

}

