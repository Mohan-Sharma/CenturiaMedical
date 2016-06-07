package com.nzion.repository.impl;

import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
import com.nzion.domain.ReferralContract;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.repository.DrugRepository;
import com.nzion.repository.ReferralContractRepository;
import com.nzion.util.UtilDateTime;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

/**
 * Created by Asus on 6/23/2015.
 */
public class HibernateReferralContractRepository extends HibernateBaseRepository implements ReferralContractRepository {

    public ReferralContract getReferralContractByReferralAndReferee(long referralId,long refreeId) {

        Criteria criteria = getSession().createCriteria(ReferralContract.class);
        criteria.add(Restrictions.eq("referralId", referralId));
        criteria.add(Restrictions.eq("refereeId", refreeId));
        List<ReferralContract> list = criteria.list();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
