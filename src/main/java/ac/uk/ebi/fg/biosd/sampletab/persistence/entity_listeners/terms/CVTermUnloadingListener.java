/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.terms.CVTerm;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;

/**
 * Unloading pre/post processor for {@link CVTerm} entities.
 *
 * <dl><dt>date</dt><dd>Apr 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class CVTermUnloadingListener extends UnloadingListener<CVTerm>
{
	public CVTermUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( CVTerm entity ) {
		return 0;
	}

	/**
	 * Removes all dangling {@link CVTerm} records.
	 */
	@Override
	public long postRemove ( CVTerm entity )
	{
		// do not add oe.id or remove oeX.id. While this would be a more proper syntax, this not-so-correct syntax is the
		// only way I can make Hibernate to translate correctly the query into SQL
		// TODO: use classes for entity names
		//
		String hqls[] = new String[] {
			"DELETE FROM AnnotationType cv WHERE cv NOT IN ( SELECT cvA.id FROM Annotation ann JOIN ann.type cvA )",
			"DELETE FROM ContactRole cv WHERE\n" +
			  "  cv NOT IN ( SELECT cvA.id FROM Contact cnt JOIN cnt.contactRoles cvA )" +
			  "  AND cv NOT IN ( SELECT cvA.id FROM Organization org JOIN org.organizationRoles cvA )",
			"DELETE FROM PublicationStatus cv WHERE cv NOT IN ( SELECT cvA.id FROM Publication pub JOIN pub.status cvA )"
		};
		
		long result = 0;
		for ( String hql: hqls ) 
			result += entityManager.createQuery ( hql ).executeUpdate ();

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemove( null ): returning %d", Publication.class.getSimpleName (), result ));
		return result;
	}

}
