package dev.peterrhodes.optionpricing.options;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.withPrecision;

import dev.peterrhodes.optionpricing.enums.OptionType;
import dev.peterrhodes.optionpricing.enums.RoundingMethod;
import dev.peterrhodes.optionpricing.models.CalculationModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link #EuropeanOption}.
 * References:
 * <ul>
 *   <li>Hull (2014): Hull, J. (2014) Options, Futures and Other Derivatives. 9th Edition, Prentice Hall, Upper Saddle River.</li>
 *   <li>Hull SSM (2014): Hull, J. (2014) Student Solutions Manual for Options, Futures, and Other Derivatives. 9th Edition, Prentice Hall, Upper Saddle River.</li>
 * </ul>
 */
@SuppressWarnings("checkstyle:multiplevariabledeclarations")
class EuropeanOptionTest {

    private void assertCalculation(CalculationModel result, int[] expectedStepLengths, String[][] expectedStepSubstitutionContains, String[] expectedStepAnswers, double expectedAnswer, double answerPrecision) {
        int expectedStepsLength = expectedStepLengths.length;

        String[][] steps = result.getSteps();

        assertThat(steps.length)
            .as("number of steps")
            .isEqualTo(expectedStepLengths.length);

        for (int i = 0; i < expectedStepLengths.length; i++) {
            // number of step parts
            assertThat(steps[i].length)
                .as(String.format("number of parts in step %d", i))
                .isEqualTo(expectedStepLengths[i]);

            // values substituted into equation
            int substitutionPartIndex = steps[i].length - 2;
            String substitutionPart = steps[i][substitutionPartIndex];
            for (String chars : expectedStepSubstitutionContains[i]) {
                assertThat(substitutionPart.contains(chars))
                    .as(String.format("step %d, part %d '%s', contains '%s'", i, substitutionPartIndex, substitutionPart, chars))
                    .isTrue();
            }

            // answer (null if not given in example)
            if (expectedStepAnswers[i] != null) {
                assertThat(steps[i][expectedStepLengths[i] - 1])
                    .as(String.format("step %d last element (answer)", i))
                    .isEqualTo(expectedStepAnswers[i]);
            }
        }

        assertThat(result.getAnswer()).isEqualTo(expectedAnswer, withPrecision(answerPrecision));
    }

    //region throws tests
    //----------------------------------------------------------------------

    @Test
    void Invalid_argument_values_should_throw_IllegalArgumentException() {
        // Arrange
        double S = 100.0, K = 100.0, τ = 1.0, σ = 0.25, r = 0.1, q = 0.05;
        Class exClass = IllegalArgumentException.class;
        String greaterThanZeroMessage = "must be greater than zero";

        // Act Assert
        assertThatThrownBy(() -> {
            EuropeanOption ex = EuropeanOption.createCall(0, K, τ, σ, r, q);
        })
            .as("zero spot price")
            .isInstanceOf(exClass)
            .hasMessageContaining(greaterThanZeroMessage);

        assertThatThrownBy(() -> {
            EuropeanOption ex = EuropeanOption.createCall(S, 0, τ, σ, r, q);
        })
            .as("zero strike price")
            .isInstanceOf(exClass)
            .hasMessageContaining(greaterThanZeroMessage);

        assertThatThrownBy(() -> {
            EuropeanOption ex = EuropeanOption.createCall(S, K, 0, σ, r, q);
        })
            .as("zero time to maturity")
            .isInstanceOf(exClass)
            .hasMessageContaining(greaterThanZeroMessage);

        assertThatThrownBy(() -> {
            EuropeanOption ex = EuropeanOption.createCall(S, K, τ, 0, r, q);
        })
            .as("zero volatiliy")
            .isInstanceOf(exClass)
            .hasMessageContaining(greaterThanZeroMessage);
    }

    //----------------------------------------------------------------------
    //endregion

    //region price tests
    //----------------------------------------------------------------------

    /**
     * Hull SSM (2014): page 166, Problem 15.13.
     */
    @Test
    void Price_for_call_with_no_dividend_HullSsm2014P1513() {
        // Arrange
        EuropeanOption call = EuropeanOption.createCall(52, 50, 0.25, 0.3, 0.12, 0);
        call.setCalculationStepPrecision(4, RoundingMethod.DECIMAL_PLACES);

        // Act
        CalculationModel result = call.priceCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 4, 3, 3, 4 }; // d₁, d₂, N(d₁), N(d₂), price
        String[][] expectedStepSubstitutionContains = {
            { " 52 ", " 50 ", " 0.25 ", " 0.3 ", " 0.12 ", " 0 " },
            { " 52 ", " 50 ", " 0.25 ", " 0.3 ", " 0.12 ", " 0 " },
            { " 0.5365 " }, // d₁
            { " 0.3865 " }, // d₂
            { " 52 ", " 50 ", " 0.25 ", " 0.12 ", " 0 ", " 0.5365 ", " 0.3865" }, // S, K, τ, r, q, d₁, d₂
        };
        String[] expectedStepAnswers = { "0.5365", "0.3865", "0.7042", "0.6504", null }; // price not given to 4.d.p.

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 5.06, 0.01);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(call.price());

        /*for (String[] step : result.getSteps()) {
            System.out.println("step");
            for (String part : step) {
                System.out.println(part);
            }
        }*/
    }

    /**
     * Hull (2014): page 360, section 15.9, Example 15.6.
     */
    @Test
    void Price_for_put_with_no_dividend_Hull2014Ex156() {
        // Arrange
        EuropeanOption put = EuropeanOption.createPut(42, 40, 0.5, 0.2, 0.1, 0);
        put.setCalculationStepPrecision(4, RoundingMethod.DECIMAL_PLACES);

        // Act
        CalculationModel result = put.priceCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 4, 3, 3, 4 }; // d₁, d₂, N(-d₁), N(-d₂), price
        String[][] expectedStepSubstitutionContains = {
            { " 42 ", " 40 ", " 0.5 ", " 0.2 ", " 0.1 ", " 0 " },
            { " 42 ", " 40 ", " 0.5 ", " 0.2 ", " 0.1 ", " 0 " },
            { " -0.7693 " }, // -d₁
            { " -0.6278 " }, // -d₂
            { " 42 ", " 40 ", " 0.5 ", " 0.1 ", " 0 ", " - 0.7693 ", " - 0.6278" } // S, K, τ, r, q, -d₁, -d₂
        };
        String[] expectedStepAnswers = { "0.7693", "0.6278", "0.2209", "0.2651", null }; // price not given to 4.d.p.

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 0.81, 0.01);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(put.price());
    }

    // TODO put with dividend

    //----------------------------------------------------------------------
    //endregion price tests

    //region delta tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 427, section 19.4, Example 19.1.
     */
    @Test
    void Delta_for_call_with_no_dividend_Hull2014Ex191() {
        // Arrange
        EuropeanOption call = EuropeanOption.createCall(49, 50, 0.3846, 0.2, 0.05, 0);
        call.setCalculationStepPrecision(3, RoundingMethod.SIGNIFICANT_FIGURES);

        // Act
        CalculationModel result = call.deltaCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 3, 5 }; // d₁, N(d₁), Δ
        String[][] expectedStepSubstitutionContains = {
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            { " 0.0542 " }, // d₁
            { " 0.3846 ", " 0 ", " 0.0542 " } // τ, q, d₁
        };
        String[] expectedStepAnswers = { "0.0542", "0.522", "0.522" };

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 0.522, 0.001);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(call.delta());
    }

    // TODO call dividend, put no dividend

    /**
     * Hull (2014): page 445, section 19.13, Example 19.9.
     */
    @Test
    void Delta_for_put_with_dividend_Hull2014Ex199() {
        // Arrange
        EuropeanOption put = EuropeanOption.createPut(90, 87, 0.5, 0.25, 0.09, 0.03);
        put.setCalculationStepPrecision(4, RoundingMethod.DECIMAL_PLACES);

        // Act
        CalculationModel result = put.deltaCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 3, 5 }; // d₁, N(-d₁), Δ
        String[][] expectedStepSubstitutionContains = {
            { " 90 ", " 87 ", " 0.5 ", " 0.25 ", " 0.09 ", " 0.03 " },
            { " -0.4499 " }, // -d₁
            { " 0.5 ", " 0.03 ", " - 0.4499 " } // τ, q, -d₁
        };
        String[] expectedStepAnswers = { "0.4499", null, "-0.3215" };

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, -0.3215, 0.0001);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(put.delta());
    }

    //----------------------------------------------------------------------
    //endregion delta tests

    //region gamma tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 436, section 19.6, Example 19.4.
     */
    @Test
    void Gamma_for_option_with_no_dividend_Hull2014Ex194() {
        // Arrange
        Number S = 49, K = 50, τ = 0.3846, σ = 0.2, r = 0.05, q = 0;
        EuropeanOption call = EuropeanOption.createCall(S, K, τ, σ, r, q);
        call.setCalculationStepPrecision(3, RoundingMethod.DECIMAL_PLACES);
        EuropeanOption put = EuropeanOption.createPut(S, K, τ, σ, r, q);
        put.setCalculationStepPrecision(3, RoundingMethod.DECIMAL_PLACES);

        // Act
        CalculationModel callResult = call.gammaCalculation();
        CalculationModel putResult = put.gammaCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 3, 5 }; // d₁, N̕(d₁), Δ
        String[][] expectedStepSubstitutionContains = {
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            {}, // d₁ not given
            { " 49 ", " 0.3846 ", " 0.2 ", " 0 " } // S, τ, σ, q
        };
        String[] expectedStepAnswers = { null, null, "0.066" };

        this.assertCalculation(callResult, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 0.066, 0.001);
        this.assertCalculation(putResult, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 0.066, 0.001);
        assertThat(callResult.getAnswer()).as("model value same as double method").isEqualTo(call.gamma());
    }

    // TODO dividend

    //----------------------------------------------------------------------
    //endregion gamma tests

    //region vega tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 438, section 19.8, Example 19.6.
     */
    @Test
    void Vega_for_option_with_no_dividend_Hull2014Ex196() {
        // Arrange
        Number S = 49, K = 50, τ = 0.3846, σ = 0.2, r = 0.05, q = 0;
        EuropeanOption call = EuropeanOption.createCall(S, K, τ, σ, r, q);
        call.setCalculationStepPrecision(3, RoundingMethod.SIGNIFICANT_FIGURES);
        EuropeanOption put = EuropeanOption.createPut(S, K, τ, σ, r, q);
        put.setCalculationStepPrecision(3, RoundingMethod.SIGNIFICANT_FIGURES);

        // Act
        CalculationModel callResult = call.vegaCalculation();
        CalculationModel putResult = put.vegaCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 3, 5 }; // d₁, N̕(d₁), Δ
        String[][] expectedStepSubstitutionContains = {
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            {}, // d₁ not given
            { " 49 ", " 0.3846 ", " 0 " } // S, τ, q
        };
        String[] expectedStepAnswers = { null, null, "12.1" };

        this.assertCalculation(callResult, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 12.1, 0.1);
        this.assertCalculation(putResult, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 12.1, 0.1);
        assertThat(callResult.getAnswer()).as("model value same as double method").isEqualTo(call.vega());
    }

    // TODO dividend

    //----------------------------------------------------------------------
    //endregion vega tests

    //region theta tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 431, section 19.5, Example 19.2.
     */
    @Test
    void Theta_for_call_with_no_dividend_Hull2014Ex192() {
        // Arrange
        EuropeanOption call = EuropeanOption.createCall(49, 50, 0.3846, 0.2, 0.05, 0);
        call.setCalculationStepPrecision(3, RoundingMethod.SIGNIFICANT_FIGURES);

        // Act
        CalculationModel result = call.thetaCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 4, 3, 3, 3, 5 }; // d₁, d₂, N(-d₁), N(-d₂), N̕(d₁), ϴ
        String[][] expectedStepSubstitutionContains = {
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            { }, // d values not given
            { },
            { },
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
        };
        String[] expectedStepAnswers = { null, null, null, null, null, "-4.31" };

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, -4.31, 0.01);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(call.theta());
    }

    // TODO call dividend, put no/with dividend

    //----------------------------------------------------------------------
    //endregion theta tests

    //region rho tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 439, section 19.9, Example 19.7.
     */
    @Test
    void Rho_for_call_with_no_dividend_Hull2014Ex197() {
        // Arrange
        EuropeanOption call = EuropeanOption.createCall(49, 50, 0.3846, 0.2, 0.05, 0);
        call.setCalculationStepPrecision(3, RoundingMethod.SIGNIFICANT_FIGURES);

        // Act
        CalculationModel result = call.rhoCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 3, 5 }; // d₂, N(d₂), vega
        String[][] expectedStepSubstitutionContains = {
            { " 49 ", " 50 ", " 0.3846 ", " 0.2 ", " 0.05 ", " 0 " },
            {}, // d₂ not given
            { " 50 ", " 0.3846 ", " 0.05 " } // K, τ, r
        };
        String[] expectedStepAnswers = { null, null, "8.91" };

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 8.91, 0.01);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(call.rho());
    }

    // TODO call dividend, put no/with dividend

    //----------------------------------------------------------------------
    //endregion rho tests

    //region disabled tests
    //----------------------------------------------------------------------

    /**
     * Hull (2014): page 396, section 17.4, Example 17.1.
     */
    @Disabled("Likely rounding issue due to the time being given as a fraction. Confirm the results with another source. d₁: book = 0.5444 calc = 0.5445, N(d₁): book = 0.6782 calc = 0.6783")
    @Test
    void Price_for_call_with_dividend_Hull2014Ex171() {
        // Arrange
        EuropeanOption call = EuropeanOption.createCall(930, 900, 2 / 12d, 0.2, 0.08, 0.03);
        call.setCalculationStepPrecision(4, RoundingMethod.SIGNIFICANT_FIGURES);

        // Act
        CalculationModel result = call.priceCalculation();

        // Assert
        int[] expectedStepLengths = { 4, 4, 3, 3, 4 }; // d₁, d₂, N(d₁), N(d₂), price
        String[][] expectedStepSubstitutionContains = {
            { " 930 ", " 900 ", " 0.2 ", " 0.08 ", " 0.03 " }, // no τ because it's a fraction
            { " 930 ", " 900 ", " 0.2 ", " 0.08 ", " 0.03 " },
            { " 0.5444 " }, // d₁
            { " 0.4628 " }, // d₂
            { " 930 ", " 900 ", " 0.08 ", " 0.03 ", " 0.5444 ", " 0.4628" }, // S, K, r, q, d₁, d₂
        };
        String[] expectedStepAnswers = { "0.5444", "0.4628", "0.7069", "0.6782", "51.83" };

        this.assertCalculation(result, expectedStepLengths, expectedStepSubstitutionContains, expectedStepAnswers, 51.83, 0.01);
        assertThat(result.getAnswer()).as("model value same as double method").isEqualTo(call.price());
    }

    //----------------------------------------------------------------------
    //endregion disabled tests
}
