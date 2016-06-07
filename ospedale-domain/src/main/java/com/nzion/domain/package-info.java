@org.hibernate.annotations.FilterDefs(value = {
		@org.hibernate.annotations.FilterDef(name = "LocationFilter", defaultCondition = ":locationId=LOCATION_ID", parameters = {@org.hibernate.annotations.ParamDef(type = "long", name = "locationId")}),
		@org.hibernate.annotations.FilterDef(name = "DateFilterDef", defaultCondition = "THRU_DATETIME IS NULL"),
		@org.hibernate.annotations.FilterDef(name = "EnabledFilter", defaultCondition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)"),
		@org.hibernate.annotations.FilterDef(name = "ICD9Filter", defaultCondition = ":ICD-9-CM=ICD_VERSION") })
@org.hibernate.annotations.TypeDefs({
        @TypeDef(
                name="encryptedString",
                typeClass=EncryptedStringType.class,
                parameters= {
                        @Parameter(name="encryptorRegisteredName", value="myHibernateStringEncryptor")
                }
        ),@TypeDef(defaultForType = Character.class, typeClass = EmptyCharacterType.class),
        @TypeDef(
                name="encryptedDate",
                typeClass=EncryptedDateAsStringType.class,
                parameters= {
                        @Parameter(name="encryptorRegisteredName", value="myHibernateStringEncryptor")
                }
        )
})

package com.nzion.domain;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;

import com.nzion.hibernate.ext.EmptyCharacterType;
import org.hibernate.annotations.TypeDefs;
import org.jasypt.hibernate3.type.EncryptedDateAsStringType;
import org.jasypt.hibernate3.type.EncryptedStringType;

