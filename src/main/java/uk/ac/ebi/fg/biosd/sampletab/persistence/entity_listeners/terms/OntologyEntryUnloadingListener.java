/*
 * 
 */
package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.terms;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;

/**
 * Unloading pre/post processor for {@link OntologyEntry} entities.
 *
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OntologyEntryUnloadingListener extends UnloadingListener<OntologyEntry>
{
	public OntologyEntryUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( OntologyEntry oe ) { return 0; }

	/**
	 * Removes all dangling {@link OntologyEntry} records.
	 */
	@Override
	public long postRemove ( OntologyEntry foo )
	{
		// do not add oe.id or remove oeX.id. While this would be a more proper syntax, this not-so-correct syntax is the
		// only way I can make Hibernate to translate correctly the query into SQL
		// TODO: use classes for entity names
		//
		String hql = "DELETE FROM OntologyEntry oe" +
			"  WHERE oe NOT IN ( SELECT oeA.id FROM ExperimentalPropertyType termA JOIN termA.ontologyTerms oeA )" +
			"  AND oe NOT IN ( SELECT oeB.id FROM ExperimentalPropertyValue termB JOIN termB.ontologyTerms oeB )" +
			"  AND oe NOT IN ( SELECT oeC.id FROM Unit termC JOIN termC.ontologyTerms oeC )" +
			"  AND oe NOT IN ( SELECT oeD.id FROM UnitDimension termD JOIN termD.ontologyTerms oeD )";
		
		long result = entityManager.createQuery ( hql ).executeUpdate ();

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemove( null ): returning %d", OntologyEntry.class.getSimpleName (), result ));
		return result;
	}

}
