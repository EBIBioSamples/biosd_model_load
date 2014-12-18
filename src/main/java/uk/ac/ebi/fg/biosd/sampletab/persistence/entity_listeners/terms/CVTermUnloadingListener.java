/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.organizational.Publication;
import uk.ac.ebi.fg.core_model.terms.CVTerm;

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


	/**
	 * Removes all dangling {@link CVTerm} records.
	 */
	@Override
	public long postRemoveGlobally ()
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
		log.trace ( String.format ( "%s.postRemoveGlobally(): returning %d", this.getClass().getSimpleName (), result ));
		return result;
	}

}
