
package com.nzion.domain.inpatient;

import com.nzion.domain.Location;
import com.nzion.domain.UserLogin;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.base.LocationAware;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nth Demi
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "GROUP_NAME")
@Filters( { @Filter(name = "LocationFilter", condition = "( :locationId=LOCATION_ID OR LOCATION_ID IS NULL )"),
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class EmployeeGroup extends IdGeneratingBaseEntity implements LocationAware {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private Location location;
	private Set<UserLogin> userLogins;
	
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	@ManyToOne(targetEntity = Location.class)
	@JoinColumn(name = "LOCATION_ID", nullable = false,updatable=false)
	public Location getLocation() {
	return location;
	}
	
	public void setLocation(Location location) {
	 this.location = location;
	}
	
	@ManyToMany
	@JoinTable(name = "USER_LOGIN_EMPLOYEE_GROUP",joinColumns = {@JoinColumn(name="EMPLOYEE_GROUP_ID")},
			inverseJoinColumns={@JoinColumn(name="USER_LOGIN_ID")})
	@Cascade(CascadeType.ALL)
	public Set<UserLogin> getUserLogins() {
	return userLogins;
	}
	
	public void setUserLogins(Set<UserLogin> userLogins) {
	this.userLogins = userLogins;
	}
	
	@Transient
	public void addUserLogin(UserLogin userLogin){
	userLogins = userLogins == null ? new HashSet<UserLogin>() : userLogins;
	userLogins.add(userLogin);	
	}
	
	
}
