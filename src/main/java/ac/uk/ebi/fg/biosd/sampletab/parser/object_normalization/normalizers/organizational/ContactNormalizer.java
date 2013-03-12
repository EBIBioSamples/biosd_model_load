/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.Store;
import ac.uk.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.toplevel.AnnotatableNormalizer;

/**
 * 
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Mar 11, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ContactNormalizer extends AnnotatableNormalizer<Contact>
{
	public ContactNormalizer ( Store store ) {
		super ( store );
	}

	@Override
	public void normalize ( Contact cnt ) 
	{
		if ( cnt == null || cnt.getId () != null ) return;
		super.normalize ( cnt );
		
		normalizeRoles ( store, cnt.getContactRoles () );
	}
	
	
	static void normalizeRoles ( Store store, Set<ContactRole> roles ) 
	{
		Set<ContactRole> delRoles = new HashSet<ContactRole> (), addRoles = new HashSet<ContactRole> ();
		
		for ( ContactRole role: roles )
		{
			if ( role == null ) {
				delRoles.add ( role );
				continue; 
			}
			String roleName = StringUtils.trimToNull ( role.getName () );
			if ( roleName == null ) {
				delRoles.add ( role );
				continue;
			}
			
			ContactRole roleS = store.find ( role, role.getName () );
			if ( roleS == null ) continue; 
			
			delRoles.add ( role ); addRoles.add ( roleS );
		}
		
		roles.removeAll ( delRoles );
		roles.removeAll ( addRoles );		
	}
}
