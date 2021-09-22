package dev.peterrhodes.optionpricing.contracts;

import dev.peterrhodes.optionpricing.enums.OptionStyle;
import dev.peterrhodes.optionpricing.enums.OptionType;

/**
 * TODO.
 */
public final class AmericanContract extends AbstractContract {

    /**
     * Creates a vanilla American option contract.&nbsp;The option's style defaults to {@link OptionStyle#AMERICAN}.&nbsp;For a description of the other arguments and exceptions thrown see {@link dev.peterrhodes.optionpricing.common.AbstractContract#AbstractContract(OptionStyle, OptionType, Number, Number, Number, Number, Number, Number)}.
     */
    public AmericanContract(OptionType type, Number S, Number K, Number τ, Number σ, Number r, Number q) throws IllegalArgumentException, NullPointerException {
        super(OptionStyle.EUROPEAN, type, S, K, τ, σ, r, q);
    }
}