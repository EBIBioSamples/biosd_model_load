/*
 * 
 */
package ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
import ac.uk.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;

/**
 * Unloading pre/post processor for {@link ReferenceSource} entities.
 *
 * <dl><dt>date</dt><dd>Apr 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ReferenceSourceUnloadingListener extends UnloadingListener<ReferenceSource>
{
	public ReferenceSourceUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( ReferenceSource entity )
	{
		return 0;
	}

	/**
	 * Removes all dangling {@link ReferenceSource} records.
	 */
	@Override
	public long postRemove ( ReferenceSource entity )
	{
		// do not add oe.id or remove oeX.id. While this would be a more proper syntax, this not-so-correct syntax is the
		// only way I can make Hibernate to translate correctly the query into SQL
		// TODO: use classes for entity names
		//
		String hql = "DELETE FROM ReferenceSource src WHERE\n" +
			"  src NOT IN ( SELECT srcA.id FROM OntologyEntry oe JOIN oe.source srcA )\n" + 
		  "  AND src NOT IN ( SELECT srcB.id FROM XRef xr JOIN xr.source srcB )\n" +
			"  AND src NOT IN ( SELECT db.id FROM MSI msi JOIN msi.databases db )";
		
		long result = entityManager.createQuery ( hql ).executeUpdate ();

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemove( null ): returning %d", ReferenceSource.class.getSimpleName (), result ));
		return result;
	}

}
