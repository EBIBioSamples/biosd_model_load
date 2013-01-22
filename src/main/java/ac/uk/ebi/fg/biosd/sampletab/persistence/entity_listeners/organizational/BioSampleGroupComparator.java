/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AccessibleComparator;

/**
 * Tells when two sample groups are equivalent, i.e., when they have the same accession and the same set of attributes.
 * 
 * TODO: only the accession used for the moment.
 *
 * <dl><dt>date</dt><dd>Jan 14, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BioSampleGroupComparator extends AccessibleComparator<BioSampleGroup>
{

	@Override
	public int compare ( BioSampleGroup sg1, BioSampleGroup sg2 )
	{
		int cmp = super.compare ( sg1, sg2 );
		if ( cmp != 0 ) return cmp;
		// TODO
		return cmp;
	}

}
