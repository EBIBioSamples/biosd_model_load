package uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.xref;

import javax.persistence.EntityManager;

import uk.ac.ebi.fg.biosd.sampletab.persistence.entity_listeners.UnloadingListener;
import uk.ac.ebi.fg.core_model.xref.XRef;

/**
 * Unloading pre/post processor for {@link XRef} entities.
 *
 * <dl><dt>date</dt><dd>Apr 18, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class XRefUnloadingListener extends UnloadingListener<XRef>
{
	public XRefUnloadingListener ( EntityManager entityManager ) {
		super ( entityManager );
	}

	@Override
	public long preRemove ( XRef entity ) {
		return 0;
	}


	/**
	 * Removes all dangling {@link XRef} records.
	 */
	@Override
	public long postRemove ( XRef entity )
	{
		// do not add oe.id or remove oeX.id. While this would be a more proper syntax, this not-so-correct syntax is the
		// only way I can make Hibernate to translate correctly the query into SQL
		// TODO: use classes for entity names
		//
		String hql = "DELETE FROM XRef xr WHERE xr NOT IN ( SELECT xrA.id FROM Publication r JOIN r.references xrA )";
		
		long result = entityManager.createQuery ( hql ).executeUpdate ();

		// TODO: AOP
		log.trace ( String.format ( "%s.postRemove( null ): returning %d", XRef.class.getSimpleName (), result ));
		return result;
	}

}
