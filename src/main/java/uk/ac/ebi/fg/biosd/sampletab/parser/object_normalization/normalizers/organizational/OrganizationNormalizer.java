package uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;
import uk.ac.ebi.fg.core_model.organizational.Organization;

/**
 * 
 * Re-use {@link Organization#getOrganizationRoles()}.
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OrganizationNormalizer extends AnnotatableNormalizer<Organization>
{
	public OrganizationNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public boolean normalize ( Organization org ) 
	{
		if ( !super.normalize ( org ) ) return false;

		ContactNormalizer.normalizeRoles ( store, org.getOrganizationRoles () );
		return true;
	}
}
