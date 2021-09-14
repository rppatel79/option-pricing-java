package dev.peterrhodes.optionpricing.core;

import dev.peterrhodes.optionpricing.models.AnalyticalCalculationModel;

/**
 * Interface for an option that has an analytical solution, i.e. can analytically calculate the option's value and its greeks.
 * If the specific option doesn't have an analytical solution then it will extend {@link Option}.
 */
public interface AnalyticalOption extends Option {

    //region calculations
    //----------------------------------------------------------------------

    /**
     * Calculates the fair value of the option.
     *
     * @return option price
     */
    double price();

    /**
     * Calculates the delta (Δ) of the option.
     * First derivative of the option value with respect to the underlying asset price.
     *
     * @return option delta
     */
    double delta();

    /**
     * Calculates the gamma (Γ) of the option.
     * Second derivative of the option value with respect to the underlying asset price.
     *
     * @return option gamma
     */
    double gamma();

    /**
     * Calculates the vega (ν) of the option.
     * First derivative of the option value with respect to the underlying asset volatility.
     *
     * @return option vega
     */
    double vega();

    /**
     * Calculates the theta (Θ) of the option.
     * Negative first derivative of the option value with respect to the time to maturity.
     *
     * @return option theta
     */
    double theta();

    /**
     * Calculates the rho (ρ) of the option.
     * First derivative of the option value with respect to the risk free interest rate.
     *
     * @return option rho
     */
    double rho();

    //----------------------------------------------------------------------
    //endregion

    //region LaTex formulas
    //----------------------------------------------------------------------

    /**
     * TODO.
     */
    String deltaLatexFormula();

    //----------------------------------------------------------------------
    //endregion

    //region LaTex calculations
    //----------------------------------------------------------------------

    /**
     * TODO.
     */
    String deltaLatexCalculation();

    //----------------------------------------------------------------------
    //endregion

    /**
     * Performs all of the calculations necessary to populate an {@link AnalyticalCalculationModel}.
     *
     * @return model object populated with the results of the calculations
     */
    AnalyticalCalculationModel calculation();
}
