package com.nzion.service;

import com.nzion.domain.*;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.screen.ScheduleConfig;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.service.impl.ScheduleScanner;
import com.nzion.view.MultiBookValueObject;
import com.nzion.view.ScheduleSearchValueObject;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Asus on 6/23/2015.
 */
public interface ReferralContractService {

   ReferralContract  getReferralContractByReferralAndReferee(long referralId,long refreeId) ;
}
