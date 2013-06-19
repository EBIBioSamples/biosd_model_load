package uk.ac.ebi.fg.biosd.sampletab;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.ChildOfAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DerivedFromAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.MaterialAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SameAsAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
import uk.ac.ebi.fg.biosd.model.organizational.MSI;
import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
import uk.ac.ebi.fg.core_model.expgraph.properties.Unit;
import uk.ac.ebi.fg.core_model.organizational.ContactRole;
import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
import uk.ac.ebi.fg.core_model.toplevel.Annotation;
import uk.ac.ebi.fg.core_model.xref.ReferenceSource;

public class Exporter {

    
    public SampleData fromMSI(MSI msi) throws ParseException{
        SampleData sd = new SampleData();
        sd.msi.submissionIdentifier = msi.getAcc();
        sd.msi.submissionTitle = msi.getTitle();
        sd.msi.submissionDescription = msi.getDescription();
        sd.msi.submissionUpdateDate = msi.getUpdateDate();
        sd.msi.submissionReleaseDate = msi.getReleaseDate();
        //TODO sd.msi.submissionReferenceLayer = msi. ???
        
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
                    uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person(firstName, initials, lastName, email, role);
                    sd.msi.persons.add(p2);
                }
            } else {
                uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person p2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person(firstName, initials, lastName, email, null);
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
            //id to acc not an ideal match...
            String id = d.getAcc();
            String url = d.getUrl();
            uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database d2 = new uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database(name, id, url);
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
        
        for( BioSampleGroup g : msi.getSampleGroups()){
            GroupNode gn = new GroupNode();
            gn.setGroupAccession(g.getAcc());
            //TODO gn.setNodeName();
            //TODO gn.setGroupDescription();
            
            for (BioSample s : g.getSamples()){
                //TODO check if this node already exists
                
                SampleNode sn = new SampleNode();
                sn.setSampleAccession(s.getAcc());
                //TODO sn.setNodeName()
                //TODO sn.setSampleDescription();
                                
                for (ExperimentalPropertyValue<ExperimentalPropertyType> v : s.getPropertyValues()){
                    ExperimentalPropertyType t = v.getType();
                    SCDNodeAttribute attr = null;
                    if (t.getTermText().equals("Sex")){
                        attr = new SexAttribute(v.getTermText());
                        
                    } else if (t.getTermText().equals("Organism")){
                        attr = new OrganismAttribute(v.getTermText());
                        
                    } else if (t.getTermText().equals("Material")){
                        attr = new MaterialAttribute(v.getTermText());
                        
                    } else if (t.getTermText().toLowerCase().equals("same as")){
                        attr = new SameAsAttribute(v.getTermText());
                        
                    } else if (t.getTermText().toLowerCase().equals("child of")){
                        attr = new ChildOfAttribute(v.getTermText());
                        
                    } else if (t.getTermText().toLowerCase().equals("derived from")){
                        attr = new DerivedFromAttribute(v.getTermText());
                        
                    } else if (t.getTermText().toLowerCase().startsWith("comment[")){
                        Pattern commentPattern = Pattern.compile("[Cc]omment\\[(.*)\\]");
                                               
                        Matcher matcher = commentPattern.matcher(t.getTermText());
                        
                        CommentAttribute a = new CommentAttribute(matcher.group(1), v.getTermText());
                        
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
                        attr = a;
                        
                    } else if (t.getTermText().toLowerCase().startsWith("characteristic[")) {
                        Pattern commentPattern = Pattern.compile("[Cc]haracteristic\\[(.*)\\]");
                                               
                        Matcher matcher = commentPattern.matcher(t.getTermText());
                        
                        CharacteristicAttribute a = new CharacteristicAttribute(matcher.group(1), v.getTermText());
                        
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
                        attr = a;
                    }
                    //add any more attribute types that are used here
                    //TODO database attribute
                    
                    if (attr != null) {
                        if (AbstractNodeAttributeOntology.class.isInstance(attr)) {
                            AbstractNodeAttributeOntology attrOnt = (AbstractNodeAttributeOntology) attr;
                            //this can have an ontology, check for it
                            OntologyEntry oe = v.getSingleOntologyTerm();
                            if (oe != null){
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
                
                //what is an annotation and what is an experimentalpropertyvalue?
                for (Annotation a : s.getAnnotations()){
                    //TODO finish
                    a.getText();
                }
                gn.addParentNode(sn);
            }
            
            sd.scd.addNode(gn);
        }
        
        return sd;
    }
}
