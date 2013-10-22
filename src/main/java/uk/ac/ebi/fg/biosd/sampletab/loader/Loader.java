package uk.ac.ebi.fg.biosd.sampletab.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractRelationshipAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.MemoryStore;
import uk.ac.ebi.fg.biosd.sampletab.parser.object_normalization.normalizers.organizational.MSINormalizer;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicType;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.expgraph.properties.UnitDimension;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

/**
 * Loads a SampleTab file (or equivalent) into memory, mapping it as a set of BioSD model objects.
 * 
 * <dl><dt>date</dt><dd>Apr 8, 2013</dd></dl>
 * @author Adam Falcounbridge
 *
 */
public class Loader {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public MSI fromSampleData(String filename) throws ParseException {
        return fromSampleData(new File(filename));
    }
    
    public MSI fromSampleData(File file) throws ParseException {
        try {
            return fromSampleData(file.toURI().toURL());
        }
        catch (MalformedURLException e) {
            throw new ParseException("File '" + file.getAbsolutePath() + " could not be resolved to a valid URL", e);
        }
    }
    
    public MSI fromSampleData(URL url) throws ParseException {
        SampleTabSaferParser parser = new SampleTabSaferParser();
        SampleData sampledata;
        sampledata = parser.parse(url);
        return fromSampleData(sampledata);
    }
    
    public synchronized MSI fromSampleData(SampleData st) {
        MSI msi = new MSI(st.msi.submissionIdentifier);
        msi.setTitle(st.msi.submissionTitle);
        msi.setDescription(st.msi.submissionDescription);
        msi.setUpdateDate(st.msi.submissionUpdateDate);
        msi.setReleaseDate(st.msi.submissionReleaseDate);
        msi.setFormatVersion(st.msi.submissionVersion);
        //TODO st.msi.submissionReferenceLayer
        
        for (uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization org : st.msi.organizations){
            convertOrganization(org, msi);
        }
        
        for (uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person per : st.msi.persons){
            convertPerson(per, msi);
        }

        for (uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication pub : st.msi.publications) {
            convertPublication(pub, msi);
        }
        
        for (uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource ts : st.msi.termSources) {
            convertTermSource(ts, msi);
        }

        for (uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database db : st.msi.databases) {
            convertDatabase(db, msi);
        }

        for (GroupNode g : st.scd.getNodes(GroupNode.class)) {
            BioSampleGroup bg = new BioSampleGroup ( g.getGroupAccession());
            
            bg.addPropertyValue(
                    new BioCharacteristicValue(g.getNodeName(), 
                            new BioCharacteristicType("Group Name")));
            
            if (g.getGroupDescription() != null) {
                bg.addPropertyValue(
                        new BioCharacteristicValue(g.getGroupDescription(), 
                                new BioCharacteristicType("Group Description")));
            }
            
            for(SCDNodeAttribute a: g.attributes) {
                BioCharacteristicValue v = convertAtttribute(a,st);
                bg.addPropertyValue(v);
            }
            
            //referenceLayer is in MSI in SampleTab, but group in SCD in DB
            bg.setInReferenceLayer(st.msi.submissionReferenceLayer);
            
            msi.addSampleGroup(bg);
        }
        
        for (Node n : st.scd.getRootNodes()) {
            if (SampleNode.class.isInstance(n)) {
                SampleNode sn = (SampleNode) n;
                BioSample bs = convertSampleNode(st, msi, sn);
            }
            
        }

        //put BioSamples into BioSampleGroups
        for (GroupNode g : st.scd.getNodes(GroupNode.class)) {
            BioSampleGroup bg = null;
            for (BioSampleGroup bg_test : msi.getSampleGroups()) {
                if (bg_test.getAcc().equals(g.getGroupAccession())) {
                    bg = bg_test;
                    break;
                }
            }
            //check bg is not null at this point
            if (bg == null) throw new RuntimeException("Unable to refind group "+g.getGroupAccession());
            
            for(Node p : g.getParentNodes()) {
                //check this is a sample;
                SampleNode s = (SampleNode) p;
                
                for (BioSample bs : msi.getSamples()) {
                    if (bs.getAcc().equals(s.getSampleAccession())) {
                        bg.addSample(bs);
                        break;
                    }
                }
            }
        }
        
        if (msi.getSamples().size()+msi.getSampleGroups().size() == 0) {
            throw new RuntimeException("No samples or groups");
        }
        
        // Merge equivalent objects together
        new MSINormalizer ( new MemoryStore () ).normalize ( msi );

        if (msi.getSamples().size()+msi.getSampleGroups().size() == 0) {
            throw new RuntimeException("No samples or groups");
        }
        
        return msi;
    }
    
    private BioCharacteristicValue convertAtttribute(SCDNodeAttribute a, SampleData st) {
        
        BioCharacteristicType h = new BioCharacteristicType ( a.getAttributeType() );
        BioCharacteristicValue v = new BioCharacteristicValue( a.getAttributeValue(), h);
        
        boolean isOntologyAttribute = false;
        synchronized (AbstractNodeAttributeOntology.class) {
            isOntologyAttribute = AbstractNodeAttributeOntology.class.isInstance(a);
        }
        boolean isRelationshipAttribute = false;
        synchronized (AbstractRelationshipAttribute.class) {
            isRelationshipAttribute = AbstractRelationshipAttribute.class.isInstance(a);
        }
        if (isOntologyAttribute) {
            AbstractNodeAttributeOntology ao = (AbstractNodeAttributeOntology) a;
            
            //ontology
            if (ao.getTermSourceID() != null && ao.getTermSourceREF() != null) {
                TermSource t = st.msi.getTermSource(ao.getTermSourceREF());
                if (t != null) {
                    ReferenceSource rs = new ReferenceSource(t.getURI(), t.getVersion());
                    rs.setUrl(t.getURI());
                    v.addOntologyTerm ( 
                        new OntologyEntry( ao.getTermSourceID(), rs ));
                    
                    log.trace ("Added ontology term");
                } else {
                    log.warn("Unable to find Term Source "+ao.getTermSourceREF());
                }
            }
            
            //unit
            UnitAttribute unit = null;
            if (CommentAttribute.class.isInstance(a) ) {
                CommentAttribute co = (CommentAttribute) a;
                unit = co.unit;
            }
            if (CharacteristicAttribute.class.isInstance(a) ) {
                CharacteristicAttribute co = (CharacteristicAttribute) a;
                unit = co.unit;
            }
            if (unit != null) {
                Unit u = new Unit();
                u.setDimension(new UnitDimension(unit.getAttributeValue()));
                v.setUnit(u);
                AbstractNodeAttributeOntology aou = (AbstractNodeAttributeOntology) unit;
                
                //unit ontology term
                if (aou.getTermSourceID() != null && aou.getTermSourceREF() != null) {
                    TermSource t = st.msi.getTermSource(aou.getTermSourceREF());
                    if (t != null && t.getURI() != null && t.getVersion() != null) {
                        ReferenceSource rs = new ReferenceSource(t.getURI(), t.getVersion());
                        rs.setUrl(t.getURI());
                        v.addOntologyTerm ( 
                            new OntologyEntry( ao.getTermSourceID(), rs ));
                    }
                }
            }
            
        }
        //Relationship attributes need no special processing
        //Database Attributes are processed elsewhere
        
        return v;
    }

    private BioSample convertSampleNode(SampleData st, MSI msi, SampleNode s) {
        if (s.getSampleAccession() == null || s.getSampleAccession().length() == 0){
            throw new IllegalArgumentException("SampleNode must be accessioned");
        }
        
        BioSample bs = new BioSample(s.getSampleAccession());
        
        bs.addPropertyValue(
                new BioCharacteristicValue(s.getNodeName(), 
                        new BioCharacteristicType("Sample Name")));
        
        if (s.getSampleDescription() != null) {
            bs.addPropertyValue(
                    new BioCharacteristicValue(s.getSampleDescription(), 
                            new BioCharacteristicType("Sample Description")));
        }
        
        for(SCDNodeAttribute a: s.attributes) {
            boolean isDatabaseAttribute = false;
            synchronized (DatabaseAttribute.class) {
                isDatabaseAttribute = DatabaseAttribute.class.isInstance(a);
            }
            if (isDatabaseAttribute) {
                DatabaseAttribute da = (DatabaseAttribute) a;
                DatabaseRefSource dbref = new DatabaseRefSource ( da.databaseID, null );
                dbref.setUrl ( da.databaseURI );
                dbref.setName ( da.getAttributeValue () );
                bs.addDatabase( dbref );
            } else {
                bs.addPropertyValue(convertAtttribute(a, st));
            }
        }
        
        //handle child nodes
        //NB these are currently (Jul 13) removed in toload step
        for (Node n : s.getChildNodes()) {
            if (SampleNode.class.isInstance(n)) {
                SampleNode sn = (SampleNode) n;
                BioSample derivedInto = convertSampleNode(st, msi, sn);
                bs.addDerivedInto(derivedInto);
            }
        }
        
        msi.addSample(bs);
        
        return bs;
        
    }
    
    public void convertOrganization(uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization org, MSI msi) {
        uk.ac.ebi.fg.core_model.organizational.Organization o = new uk.ac.ebi.fg.core_model.organizational.Organization();
        o.setName(org.getName());
        o.setEmail(org.getEmail());
        if (org.getRole() != null) {
            o.addOrganizationRole(new ContactRole(org.getRole()));
        }
        o.setUrl(org.getURI());
        msi.addOrganization(o);
    }
    
    public void convertPerson(uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person per, MSI msi) {
        uk.ac.ebi.fg.core_model.organizational.Contact con = new uk.ac.ebi.fg.core_model.organizational.Contact();
        con.setFirstName(per.getFirstName());
        con.setMidInitials(per.getInitials());
        con.setLastName(per.getLastName());
        con.setEmail(per.getEmail());
        if (per.getRole() != null) {
            con.addContactRole(new ContactRole(per.getRole()));
        }
        msi.addContact(con);
    }
    
    public void convertPublication(uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication pub, MSI msi) {
        uk.ac.ebi.fg.core_model.organizational.Publication p = new uk.ac.ebi.fg.core_model.organizational.Publication(pub.getDOI(), pub.getPubMedID());
        msi.addPublication(p);
    }
    
    public void convertTermSource(uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource termsource, MSI msi) {
        uk.ac.ebi.fg.core_model.xref.ReferenceSource r = new uk.ac.ebi.fg.core_model.xref.ReferenceSource(termsource.getName(), termsource.getVersion());
        r.setName(termsource.getName());
        r.setUrl(termsource.getURI());
        msi.addReferenceSource(r);
    }
    
    public void convertDatabase(uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database database, MSI msi) {
        //id to acc not an ideal match...
        DatabaseRefSource d = new DatabaseRefSource(database.getID(), null);
        d.setName(database.getName());
        d.setUrl(database.getURI());
        msi.addDatabase(d);
    }
}