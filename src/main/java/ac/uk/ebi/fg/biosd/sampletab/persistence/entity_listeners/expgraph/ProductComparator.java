/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.expgraph;

import uk.ac.ebi.fg.core_model.expgraph.Product;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AccessibleComparator;

/**
 * Two biological product are considered the same if the share: the same accession, the set of attributes, the set of 
 * nodes it is derivedFrom. 
 * 
 * TODO: Only the accession used for the moment. 
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ProductComparator extends AccessibleComparator<Product<?>>
{

	@Override
	public int compare ( Product<?> p1, Product<?> p2 )
	{
		int cmp = super.compare ( p1, p2 );
		if ( cmp != 0 ) return cmp;
		// TODO
		return cmp;
	}
}
