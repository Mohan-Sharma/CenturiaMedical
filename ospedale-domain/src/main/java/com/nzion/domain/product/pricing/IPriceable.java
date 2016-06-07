package com.nzion.domain.product.pricing;

import java.util.Set;

public interface IPriceable {

    Set<Pricing> addPricingAndInvalidatePresentEffectivePricing(Pricing pricing);

    boolean invalidatePresentEffectivePricing();

    Pricing findEffectivePricing();

    boolean hasOnlyOneEffectiveSalePrices();


}
