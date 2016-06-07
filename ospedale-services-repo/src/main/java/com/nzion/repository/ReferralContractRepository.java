package com.nzion.repository;

import com.nzion.domain.ReferralContract;

/**
 * Created by Asus on 6/23/2015.
 */
public interface ReferralContractRepository  extends BaseRepository {
    ReferralContract getReferralContractByReferralAndReferee(long referralId,long refreeId) ;
}
