package com.nzion.domain.product.pricing;


import java.util.Collection;

public final class PricingHelper {

    public static Pricing findEffectivePricing(Collection<Pricing> pricings) {
        for (Pricing pricing : pricings) {
            if (pricing.isCurrentlyEffective()) {
                return pricing;
            }
        }
        return null;
    }

    public static boolean hasOnlyOneEffectiveSalePrices(Collection<Pricing> pricings) {
        int i = 0;
        for (Pricing pricing : pricings) {
            if (pricing.isCurrentlyEffective()) {
                i++;
            }
        }
        return i == 1;
    }

}
