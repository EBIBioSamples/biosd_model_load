/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.fg.core_model.organizational.Contact;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotatablePersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ContactPersistenceListener extends AnnotatablePersistenceListener<Contact>
{
	private final CVTermDAO<ContactRole> roleDao;

	public ContactPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		roleDao = new CVTermDAO<ContactRole> ( ContactRole.class, entityManager );		
	}

	@Override
	public void prePersist ( Contact cnt ) 
	{
		if ( cnt == null || cnt.getId () != null ) return;
		super.prePersist ( cnt );
		
		Set<ContactRole> delRoles = new HashSet<ContactRole> (), addRoles = new HashSet<ContactRole> ();
		Set<ContactRole> roles = cnt.getContactRoles ();
		
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
			
			ContactRole roleDB = roleDao.find ( role.getName () );
			if ( roleDB == null ) continue; 
			
			delRoles.add ( role ); addRoles.add ( roleDB );
		}
		
		roles.removeAll ( delRoles );
		roles.removeAll ( addRoles );
	}
	
}
