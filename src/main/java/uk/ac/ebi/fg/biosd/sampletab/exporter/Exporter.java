package uk.ac.ebi.fg.biosd.sampletab.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.ChildOfAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DerivedFromAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.MaterialAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SameAsAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.expgraph.properties.SampleCommentValue;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

public class Exporter {
    private Logger log = LoggerFactory.getLogger(getClass());
        
    
    private SCDNodeAttribute getAttribute(ExperimentalPropertyValue<ExperimentalPropertyType> v, SampleData sd) {

        ExperimentalPropertyType t = v.getType();
        SCDNodeAttribute attr = null;
        
        boolean isSampleCommentValue = false;
        synchronized (SampleCommentValue.class) {
            isSampleCommentValue = SampleCommentValue.class.isInstance(v);
        }
        boolean isBioCharacteristicValue = false;
        synchronized (BioCharacteristicValue.class) {
            isBioCharacteristicValue = BioCharacteristicValue.class.isInstance(v);
        }
        
        if (t.getTermText().equals("Sex")) {
            SexAttribute a = new SexAttribute(v.getTermText());
            
            OntologyEntry oe = v.getSingleOntologyTerm();
            if (oe != null) {
                ReferenceSource source = oe.getSource();
                String url = source.getUrl();
                String version = source.getVersion();
                String name = source.getName();
                TermSource ts = new TermSource(name, url, version);
                a.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                a.setTermSourceID(oe.getAcc());
            }
            attr = a;
            
        } else if (t.getTermText().equals("Organism")) {
            OrganismAttribute a = new OrganismAttribute(v.getTermText());
            
            OntologyEntry oe = v.getSingleOntologyTerm();
            if (oe != null) {
                ReferenceSource source = oe.getSource();
                String url = source.getUrl();
                String version = source.getVersion();
                String name = source.getName();
                TermSource ts = new TermSource(name, url, version);
                a.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                a.setTermSourceID(oe.getAcc());
            }
            attr = a;
            
        } else if (t.getTermText().equals("Material")) {
            MaterialAttribute a = new MaterialAttribute(v.getTermText());
            
            OntologyEntry oe = v.getSingleOntologyTerm();
            if (oe != null) {
                ReferenceSource source = oe.getSource();
                String url = source.getUrl();
                String version = source.getVersion();
                String name = source.getName();
                TermSource ts = new TermSource(name, url, version);
                a.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                a.setTermSourceID(oe.getAcc());
            }
            attr = a;
        } else if (t.getTermText().toLowerCase().equals("same as")) {
            attr = new SameAsAttribute(v.getTermText());
            
        } else if (t.getTermText().toLowerCase().equals("child of")) {
            attr = new ChildOfAttribute(v.getTermText());
            
        } else if (t.getTermText().toLowerCase().equals("derived from")) {
            attr = new DerivedFromAttribute(v.getTermText());
            
        } else if (isSampleCommentValue) {
            CommentAttribute a = new CommentAttribute(t.getTermText(), v.getTermText());
            
            Unit u = v.getUnit();
            if (u != null) {
                a.unit = new UnitAttribute();
                a.unit.setAttributeValue(u.getTermText());
                OntologyEntry oe = u.getSingleOntologyTerm();
                if (oe != null) {
                    ReferenceSource source = oe.getSource();
                    String url = source.getUrl();
                    String version = source.getVersion();
                    String name = source.getName();
                    TermSource ts = new TermSource(name, url, version);
                    
                    a.unit.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                    a.unit.setTermSourceID(oe.getAcc());
                }
            }
            
            OntologyEntry oe = v.getSingleOntologyTerm();
            if (oe != null) {
                ReferenceSource source = oe.getSource();
                String url = source.getUrl();
                String version = source.getVersion();
                String name = source.getName();
                TermSource ts = new TermSource(name, url, version);
                a.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                a.setTermSourceID(oe.getAcc());
            }
            
            attr = a;
            
        } else if (isBioCharacteristicValue) {
            CharacteristicAttribute a = new CharacteristicAttribute(t.getTermText(), v.getTermText());
            
            Unit u = v.getUnit();
            if (u != null) {
                a.unit = new UnitAttribute();
                a.unit.setAttributeValue(u.getTermText());
                OntologyEntry oe = u.getSingleOntologyTerm();
                if (oe != null) {
                    ReferenceSource source = oe.getSource();
                    String url = source.getUrl();
                    String version = source.getVersion();
                    String name = source.getName();
                    TermSource ts = new TermSource(name, url, version);
                    
                    a.unit.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                    a.unit.setTermSourceID(oe.getAcc());
                }
            }
            
            OntologyEntry oe = v.getSingleOntologyTerm();
            if (oe != null) {
                ReferenceSource source = oe.getSource();
                String url = source.getUrl();
                String version = source.getVersion();
                String name = source.getName();
                TermSource ts = new TermSource(name, url, version);
                a.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                a.setTermSourceID(oe.getAcc());
            }
            
            attr = a;
        } else {
            throw new RuntimeException("unknown attribute "+t);
        }
        
        //add any more attribute types that are used here
        
        return attr;
    }
    
    public SampleData fromMSI(MSI msi) throws ParseException{
        
        if (msi.getSamples().size()+msi.getSampleGroups().size() == 0) {
            throw new RuntimeException("No samples or groups");
        }
        
        SampleData sd = new SampleData();
        sd.msi.submissionIdentifier = msi.getAcc();
        sd.msi.submissionTitle = msi.getTitle();
        sd.msi.submissionDescription = msi.getDescription();
        sd.msi.submissionUpdateDate = msi.getUpdateDate();
        sd.msi.submissionReleaseDate = msi.getReleaseDate();
        
        for ( uk.ac.ebi.fg.core_model.organizational.Organization o  : msi.getOrganizations()){
            String name = o.getName();
            String address = o.getAddress();
            String uri = o.getUrl();
            String email = o.getEmail();
            if (o.getOrganizationRoles().size() > 0){
                for ( ContactRole c : o.getOrganizationRoles()){
                    String role = c.getName();
                    uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization o2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization(name, address, uri, email, role);
                    sd.msi.organizations.add(o2);
                }
            } else {
                uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization o2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization(name, address, uri, email, null);
                sd.msi.organizations.add(o2);
            }
        }
        
        for ( uk.ac.ebi.fg.core_model.organizational.Contact p  : msi.getContacts()){
            String firstName = p.getFirstName();
            String initials = p.getMidInitials();
            String lastName = p.getLastName();
            String email = p.getEmail();
            if (p.getContactRoles().size() > 0){
                for ( ContactRole c : p.getContactRoles()){
                    String role = c.getName();
                    // TODO: it would be more appropriate a constructor in the form firsts, mid, last, instead of this, 
                    // I leave this to Adam. Obviously this call needs to be changed after such a change.
                    uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person(lastName, initials, firstName, email, role);
                    sd.msi.persons.add(p2);
                }
            } else {
            		// TODO: same as above
                uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person(lastName, initials, firstName, email, null);
                sd.msi.persons.add(p2);
            }
        }
        
        for ( uk.ac.ebi.fg.core_model.organizational.Publication p  : msi.getPublications()){
            String pubmedID = p.getPubmedId();
            String doi = p.getDOI();
            uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication(pubmedID, doi);
            sd.msi.publications.add(p2);
        }
        
        for ( DatabaseRefSource d  : msi.getDatabases()){
            String name = d.getName();
            String id = d.getAcc();
            String url = d.getUrl();
            uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database d2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database(name, url, id);
            sd.msi.databases.add(d2);            
        }
        
        for ( ReferenceSource r  : msi.getReferenceSources()){
            String name = r.getName();
            String uri = r.getUrl();
            String version = r.getVersion();
            uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource(name, uri, version);
            sd.msi.termSources.add(p2);
        }
        
        //SCD section
        
        for( BioSampleGroup g : msi.getSampleGroups()) {
            GroupNode gn = new GroupNode();
            gn.setGroupAccession(g.getAcc());
            if (g.isInReferenceLayer()) {
                sd.msi.submissionReferenceLayer = true;
            }
            
            for (ExperimentalPropertyValue<ExperimentalPropertyType> v : g.getPropertyValues()) {
                ExperimentalPropertyType t = v.getType();
                SCDNodeAttribute attr = null;
                
                if (t.getTermText().equals("Group Name")) {
                    gn.setNodeName(v.getTermText());
                } else if (t.getTermText().equals("Group Description")) {
                    gn.setGroupDescription(v.getTermText());
                } else {
                    attr = getAttribute(v, sd);
                }

                if (attr != null) {
                    if (AbstractNodeAttributeOntology.class.isInstance(attr)) {
                        AbstractNodeAttributeOntology attrOnt = (AbstractNodeAttributeOntology) attr;
                        //this can have an ontology, check for it
                        OntologyEntry oe = v.getSingleOntologyTerm();
                        if (oe != null) {
                            ReferenceSource source = oe.getSource();
                            String url = source.getUrl();
                            String version = source.getVersion();
                            String name = source.getName();
                            TermSource ts = new TermSource(name, url, version);
                            
                            attrOnt.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                            attrOnt.setTermSourceID(oe.getAcc());
                        }
                    }
                    
                    gn.addAttribute(attr);
                }
            }

            //group database links
            for (DatabaseRefSource db : g.getDatabases()) {
                DatabaseAttribute dba = new DatabaseAttribute(db.getName(), db.getAcc(), db.getUrl());
                gn.addAttribute(dba);
            }  
            
            for (BioSample s : g.getSamples()) {
                //TODO check if this node already exists
                
                SampleNode sn = new SampleNode();
                sn.setSampleAccession(s.getAcc());
                                
                for (ExperimentalPropertyValue<ExperimentalPropertyType> v : s.getPropertyValues()) {
                    

                    ExperimentalPropertyType t = v.getType();
                    SCDNodeAttribute attr = null;

                    if (t.getTermText().equals("Sample Name")) {
                        sn.setNodeName(v.getTermText());
                        
                    } else if (t.getTermText().equals("Sample Description")) {
                        sn.setSampleDescription(v.getTermText());
                        
                    } else { 
                        attr = getAttribute(v, sd);
                    }
                        
                    //database attributes are below                    
                    
                    if (attr != null) {
                        if (AbstractNodeAttributeOntology.class.isInstance(attr)) {
                            AbstractNodeAttributeOntology attrOnt = (AbstractNodeAttributeOntology) attr;
                            //this can have an ontology, check for it
                            OntologyEntry oe = v.getSingleOntologyTerm();
                            if (oe != null) {
                                ReferenceSource source = oe.getSource();
                                String url = source.getUrl();
                                String version = source.getVersion();
                                String name = source.getName();
                                TermSource ts = new TermSource(name, url, version);
                                
                                attrOnt.setTermSourceREF(sd.msi.getOrAddTermSource(ts));
                                attrOnt.setTermSourceID(oe.getAcc());
                            }
                        }
                        
                        sn.addAttribute(attr);
                    }
                }
                                
                for (DatabaseRefSource db : s.getDatabases()) {
                    DatabaseAttribute dba = new DatabaseAttribute(db.getName(), db.getAcc(), db.getUrl());
                    sn.addAttribute(dba);
                }                
                
                sd.scd.addNode(sn);
                gn.addParentNode(sn);
                sn.addChildNode(gn);
            }
            
            sd.scd.addNode(gn);
        }
        
        if (sd.scd.getNodeCount() == 0) {
            throw new RuntimeException("No samples or groups");
        }
        
        return sd;
    }
}
