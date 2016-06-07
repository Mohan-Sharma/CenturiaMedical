INSERT  INTO `password_policy`(`CREATED_BY`,`CREATE_TX_TIMESTAMP`,`VERSION`,`PWD_MAX_REPEATED_CHARS`,`PWD_EXPIRE_WARNING`,`PWD_GRACE_LOGIN_LIMIT`,`PWD_CHECK_SYNTAX`,`PWD_FAILURE_COUNT_INTERVAL`,`PWD_IN_HISTORY`,`PWD_MAX_AGE`,`PWD_MAX_FAILURE`,`PWD_MIN_LENGTH`,`PWD_MUST_CHANGE`,`SELECTED_PATTERN`,`SESSION_TIME_OUT`) VALUES ('SEED',NOW(),2,NULL,NULL,NULL,TRUE,NULL,NULL,100,3,8,'','[\\p{Alnum}]+',600);

INSERT  INTO `person`(`ID`,`IS_ACTIVE`,`CREATED_BY`,`CREATE_TX_TIMESTAMP`,`DEACTIVATION_REASON`,`UPDATE_BY`,`UPDATED_TX_TIMESTAMP`,`VERSION`,`ACCOUNT_NUMBER`,`PARTY_TYPE`,`ALIAS_NAME`,`ALTERNATE_PHONE`,`EMAIL`,`FAX_NUMBER`,`HOME_PHONE`,`MOBILE_NUMBER`,`OFFICE_EXT`,`OFFICE_PHONE`,`PAGER_NUMBER`,`ADDRESS1`,`ADDRESS2`,`ATTN_NAME`,`CITY`,`COUNTRY_GEO`,`COUNTY_GEO`,`DIRECTIONS`,`GEO_POINT`,`POSTAL_CODE`,`POSTAL_CODE_EXT`,`STATE_PROVINCE_GEO`,`TO_NAME`,`DATE_OF_BIRTH`,`DATE_OF_DEATH`,`FIRST_NAME`,`LAST_NAME`,`MIDDLE_NAME`,`PERSONAL_TITLE`,`PREFIX`,`SALUTATION`,`SCHEDULABLE`,`SSN_NUMBER`,`SUFFIX`,`DEACTIVATEDBY`,`GENDER_CODE`,`PARTY_LANGUAGE`,`MARITAL_CODE`,`PROFILE_PICTURE`) VALUES (1,'','seed','2011-07-07 18:21:39',NULL,'jadmin','2011-07-08 13:30:45',2,'0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'United States of America',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'THE','Administrator',NULL,NULL,NULL,NULL,'\0',NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT  INTO `user_login`(`ID`,`IS_ACTIVE`,`CREATED_BY`,`CREATE_TX_TIMESTAMP`,`DEACTIVATION_REASON`,`UPDATE_BY`,`UPDATED_TX_TIMESTAMP`,`VERSION`,`ACCEPTED_TERMS_AND_CONDITIONS`,`ACCOUNT_NON_EXPIRED`,`ACCOUNT_NON_LOCKED`,`ROLES`,`CREDENTIALS_NON_EXPIRED`,`IMPERSONATED`,`LOCKED`,`PASSWORD`,`PWD_CHANGED_TIME`,`PWD_FAILURE_TIME`,`IS_REQUIRE_PASSWORD_CHANGE`,`SECRET_QUESTION`,`SECRET_QUESTION_ANSWER`,`SUCCESSIVE_FAILED_LOGINS`,`USERNAME`,`DEACTIVATEDBY`,`ORIGINAL_PERSON_ID`,`PERSON_ID`) VALUES (101,'','seed','2011-07-07 18:21:39',NULL,'jadmin','2011-07-08 13:30:45',2,'\0','','',36028797018963968,'','\0','\0','test123@','2011-07-07 18:21:39',NULL,'\0',NULL,NULL,0,'jadmin',NULL,NULL,1);