package com.nzion.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.domain.person.*;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.nzion.domain.Location;
import com.nzion.domain.Person;
import com.nzion.domain.PersonDelegation;
import com.nzion.domain.Provider;
import com.nzion.domain.UserLogin;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.messaging.Message;
import com.nzion.repository.PersonRepository;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

@SuppressWarnings("unchecked")
public class HibernatePersonRepository extends HibernateBaseRepository implements PersonRepository {

    public List<VitalSign> searchPersonVitalSigns(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(VitalSign.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.createCriteria("vitalSign").add(Restrictions.like("name", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    public List<PersonDrug> getPersonFavouriteDrugs(Person person) {
        return unify(findByCriteria(PersonDrug.class, new String[] { "person" }, new Object[] { person }));
    }

    @Override
    public List<ProviderDrug> searchPersonFavouriteDrugs(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(ProviderDrug.class).createAlias("drug", "d");
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("d.tradeName", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<PersonLab> searchPersonFavouriteLabs(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(PersonLab.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("testName", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<PersonProcedure> searchPersonFavouriteProcedures(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(PersonProcedure.class).createAlias("procedure", "p");
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("p.description", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<DrugGroup> searchPersonFavouriteDrugGroup(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(DrugGroup.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("drugGroup", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<LabGroup> searchPersonFavouriteLabGroup(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(LabGroup.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("labGroupName", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<ProcedureGroup> searchPersonFavouriteProcedureGroup(String searchString, Person person) {
        Criteria criteria = getSession().createCriteria(ProcedureGroup.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.like("procedureGroupName", searchString, MatchMode.ANYWHERE));
        return criteria.setCacheable(true).list();
    }

    @Override
    public PersonDrug getPersonDrugsByPersonAndDrug(Person person, Drug drug) {
        Criteria criteria = getSession().createCriteria(PersonDrug.class);
        criteria.add(Restrictions.eq("person", person));
        criteria.add(Restrictions.eq("drug", drug));
        return (PersonDrug) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public List<PersonChiefComplaint> getPersonChiefComplaints(Person person) {
        Criteria criteria = getSession().createCriteria(PersonChiefComplaint.class);
        criteria.add(Restrictions.eq("person", person));
        return criteria.setCacheable(true).list();
    }

    @Override
    public <T> List<T> getPersonFavourites(Person person, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(Restrictions.eq("person", person));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<Person> getPersonsFor(String personFirstName, String personLastName) {
        Criteria criteria = getSession().createCriteria(Person.class);
        criteria.add(Restrictions.eq("firstName", personFirstName)).add(Restrictions.eq("lastName", personLastName));
        // criteria.add(Restrictions.or(Restrictions.eq("firstName", personName), Restrictions.eq("lastName", personName)));
        return criteria.setCacheable(true).list();
    }

    @Override
    public Set<Message> getPersonsMessages(Person person, Long personRole, Date from, Date thru) {
        Set<Message> messages = new HashSet<Message>();
        if (person != null) {
            Criteria personCriteria = getSession().createCriteria(Message.class);
            addFromThruToCriteria(personCriteria, from, thru);
            personCriteria.createCriteria("persons").add(Restrictions.idEq(person.getId()));
            messages.addAll(personCriteria.list());
        }
        if (personRole != null) {
            Criteria roleCriteria = getSession().createCriteria(Message.class).add(
                    Restrictions.sqlRestriction("{alias}.roles & " + String.valueOf(personRole) + " > 0"));
            addFromThruToCriteria(roleCriteria, from, thru);
            messages.addAll(roleCriteria.list());
        }
        return messages;
    }

    @Override
    public List<PersonIcd> searchPersonIcdBy(Person person, String icdCode, String icdDescription) {
        Criteria criteria = getSession().createCriteria(PersonIcd.class);
        List<PersonIcd> personIcds = new ArrayList<PersonIcd>();
        criteria.add(Restrictions.eq("person", person));
        if (icdCode!=null && icdDescription!=null) {
            criteria.createCriteria("icd").add(Restrictions.like("code", icdCode, MatchMode.START)).add(Restrictions.like("description", icdDescription, MatchMode.START));
            personIcds.addAll(criteria.list());
            return personIcds;
        }
        if (icdCode!=null  && icdDescription == null) {
            criteria.createCriteria("icd").add(Restrictions.like("code", icdCode, MatchMode.START));
            personIcds.addAll(criteria.list());
            return personIcds;
        }

        return criteria.setCacheable(true).list();
    }

    @Override
    public List<PersonDrug> searchPersonDrugBy(String genericName, String tradeName, Person person) {
        List<PersonDrug> personDrugs = new ArrayList<PersonDrug>();
        Criteria criteria = getSession().createCriteria(PersonDrug.class);
        criteria.add(Restrictions.eq("person", person));
        if(genericName!=null && tradeName!=null){
            criteria.createCriteria("drug").add(Restrictions.like("tradeName", tradeName,MatchMode.START)).add(Restrictions.like("genericName", genericName));
            personDrugs.addAll(criteria.list());
            return personDrugs;
        }
        if(genericName!=null && tradeName ==null){
            criteria.createCriteria("drug").add(Restrictions.like("genericName", genericName,MatchMode.START));
            personDrugs.addAll(criteria.list());
            return personDrugs;
        }
        criteria.createCriteria("drug").add(Restrictions.like("tradeName", tradeName,MatchMode.START));
        return criteria.list();
    }

    @Override
    public List<Person> searchSchedulablePerson(String searchField, String value,Collection<Location> locations) {
        List<Long> locationIds = new ArrayList<Long>();
        for(Location location : locations)
            locationIds.add(location.getId());
        if ("specialities".equals(searchField)) {
            Criteria criteria = getSession().createCriteria(Provider.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.createCriteria("specialities").add(Restrictions.ilike("description", value, MatchMode.START));
            criteria.add(Restrictions.eq("schedulable", true));
            if(!locationIds.isEmpty())
                criteria.createCriteria("locations").add(Restrictions.in("id", locationIds));
            return criteria.list();
        }
        Criteria criteria = getSession().createCriteria(Person.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.like(searchField, value,MatchMode.START));
        criteria.add(Restrictions.eq("schedulable", true));
        if(!locationIds.isEmpty())
            criteria.createCriteria("locations").add(Restrictions.in("id", locationIds));
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<PersonDelegation> getProviderDelegationFor(Person person,Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(PersonDelegation.class);
        criteria.add(Restrictions.eq("person", person));
        if(fromDate!=null && thruDate!=null){
            criteria.add(Restrictions.le("fromDate", thruDate));
            criteria.add(Restrictions.ge("thruDate", fromDate));
        }
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<Person> getAllPersonsAccordingToUserLoginRole(Long personRole) {
        Criteria criteria = getSession().createCriteria(UserLogin.class);
        criteria.add(Restrictions.sqlRestriction("{alias}.roles & " + String.valueOf(personRole) + " > 0"));
        criteria.setProjection(Projections.property("person"));
        return criteria.setCacheable(true).list();
    }
    /*@Override
    public Set<Message> getDateRangeMessage(Person person,Long personRole, Date fromDate,Date thruDate) {
        Criteria criteria = getSession().createCriteria(Message.class);
        if (fromDate != null) criteria.add(Restrictions.ge("createdTxTimestamp",UtilDateTime.getDayStart(fromDate)));
        if (thruDate != null) criteria.add(Restrictions.le("createdTxTimestamp",UtilDateTime.getDayEnd(thruDate)));
        return criteria.list();
    }*/

    public int getUnreadMessageCount(Person person, Long personRole){
        /*Criteria criteria = getSession().createCriteria(Message.class);
        criteria.add(Restrictions.eq("read_msg",false));
        List<Message> msgList = criteria.list();
        return msgList.size();*/

        Set<Message> messages = new HashSet<Message>();
        if (person != null) {
            Criteria personCriteria = getSession().createCriteria(Message.class);
            personCriteria.add(Restrictions.eq("read_msg",false));
            personCriteria.createCriteria("persons").add(Restrictions.idEq(person.getId()));
            messages.addAll(personCriteria.list());
        }
        if (personRole != null) {
            Criteria roleCriteria = getSession().createCriteria(Message.class).add(
                    Restrictions.sqlRestriction("{alias}.roles & " + String.valueOf(personRole) + " > 0"));
            roleCriteria.add(Restrictions.eq("read_msg",false));
            messages.addAll(roleCriteria.list());
        }
        return messages.size();
    }

    @Override
    public Person getPersonById(Long id){
        Criteria criteria = getSession().createCriteria(Person.class);
        criteria.add(Restrictions.eq("id", id));
        return (Person) criteria.setCacheable(true).uniqueResult();

    }
}