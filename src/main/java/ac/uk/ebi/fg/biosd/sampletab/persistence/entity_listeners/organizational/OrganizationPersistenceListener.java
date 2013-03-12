/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.organizational;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.organizational.Organization;
import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.terms.CVTermDAO;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.toplevel.AnnotatablePersistenceListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Feb 25, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OrganizationPersistenceListener extends AnnotatablePersistenceListener<Organization>
{
	private final CVTermDAO<ContactRole> roleDao;

	public OrganizationPersistenceListener ( EntityManager entityManager ) 
	{
		super ( entityManager );
		roleDao = new CVTermDAO<ContactRole> ( ContactRole.class, entityManager );		
	}

	@Override
	public void prePersist ( Organization org ) 
	{
		if ( org == null || org.getId () != null ) return;
		super.prePersist ( org );
		
		ContactPersistenceListener.prePersistRoles ( roleDao, org.getOrganizationRoles () );
	}
	
}
