package dev.peterrhodes.optionpricing.options;

import dev.peterrhodes.optionpricing.enums.OptionType;
import dev.peterrhodes.optionpricing.models.AnalyticalCalculation;

import java.lang.Math;

import org.apache.commons.math3.distribution.NormalDistribution;

public class EuropeanOption implements IOption {

    private OptionType optionType;
    private double S;
    private double K;
    private double T;
    private double v;
    private double r;
    private double q;
    private NormalDistribution N;

    /**
     * Creates a European option with the specified parameters.
     * @param optionType type of the option (call or put)
     * @param S price of the underlying asset (spot price)
     * @param K strike price of the option (exercise price)
     * @param T time until option expiration (time from the start of the contract until maturity)
     * @param v (σ) underlying volatility (standard deviation of log returns)
     * @param r annualized risk-free interest rate, continuously compounded
     * @param q continuous dividend yield
     * @throws IllegalArgumentException if S, K, T, or v are not greater than zero
     */
    public EuropeanOption(OptionType optionType, double S, double K, double T, double v, double r, double q) throws IllegalArgumentException {
        this.optionType = optionType;
        this.S = this.checkGreaterThanZero(S, "S");
        this.K = this.checkGreaterThanZero(K, "K");
        this.T = this.checkGreaterThanZero(T, "T");
        this.v = this.checkGreaterThanZero(v, "v");
        this.r = r;
        this.q = q;
        this.N = new NormalDistribution();
    }

    private double checkGreaterThanZero(double value, String name) throws IllegalArgumentException {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
        return value;
    }

    private double d_i(int i) {
        double sign = i == 1 ? 1d : -1d;
        return 1d / (this.v * Math.sqrt(this.T)) * (Math.log(this.S / this.K) + (this.r - this.q + sign * Math.pow(this.v, 2d) / 2d) * this.T);
    }

    //region price
    //----------------------------------------------------------------------

    @Override
    public double price() {
        return this.optionType == OptionType.CALL ? this.callPrice() : this.putPrice();
    }

    private double callPrice() {
        return this.S * Math.exp(-this.q * this.T) * this.N.cumulativeProbability(this.d_i(1)) - this.K * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(this.d_i(2));
    }

    private double putPrice() {
        return this.K * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(-this.d_i(2)) - this.S * Math.exp(-this.q * this.T) * this.N.cumulativeProbability(-this.d_i(1));
    }

    //----------------------------------------------------------------------
    //endregion

    //region delta
    //----------------------------------------------------------------------

    public double delta() {
        return this.optionType == OptionType.CALL ? this.callDelta() : this.putDelta();
    }

    private double callDelta() {
        return Math.exp(-this.q * this.T) * this.N.cumulativeProbability(this.d_i(1));
    }

    private double putDelta() {
        return -Math.exp(-this.q * this.T) * this.N.cumulativeProbability(-this.d_i(1));
    }

    //----------------------------------------------------------------------
    //endregion

    public double gamma() {
        return Math.exp(-this.q * this.T) * this.N.density(this.d_i(1)) / (this.S * this.v * Math.sqrt(this.T));
    }

    public double vega() {
        return this.S * Math.exp(-this.q * this.T) * this.N.density(this.d_i(1)) * Math.sqrt(this.T);
    }

    //region theta
    //----------------------------------------------------------------------

    public double theta() {
        return this.optionType == OptionType.CALL ? this.callTheta() : this.putTheta();
    }

    private double callTheta() {
        double term1 = -Math.exp(-this.q * this.T) * (this.S * this.N.density(this.d_i(1)) * this.v) / (2d * Math.sqrt(this.T));
        double term2 = this.r * this.K * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(this.d_i(2));
        double term3 = this.q * this.S * Math.exp(-this.q * this.T) * this.N.cumulativeProbability(this.d_i(1));
        return term1 - term2 + term3;
    }

    private double putTheta() {
        double term1 = -Math.exp(-this.q * this.T) * (this.S * this.N.density(this.d_i(1)) * this.v) / (2d * Math.sqrt(this.T));
        double term2 = this.r * this.K * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(-this.d_i(2));
        double term3 = this.q * this.S * Math.exp(-this.q * this.T) * this.N.cumulativeProbability(-this.d_i(1));
        return term1 + term2 - term3;
    }

    //----------------------------------------------------------------------
    //endregion

    //region rho
    //----------------------------------------------------------------------

    public double rho() {
        return this.optionType == OptionType.CALL ? this.callRho() : this.putRho();
    }

    private double callRho() {
        return this.K * this.T * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(this.d_i(2));
    }

    private double putRho() {
        return -this.K * this.T * Math.exp(-this.r * this.T) * this.N.cumulativeProbability(-this.d_i(2));
    }

    //----------------------------------------------------------------------
    //endregion

    public AnalyticalCalculation analyticalCalculation() {
        double price = this.price();
        double delta = this.delta();
        double gamma = this.gamma();
        double vega = this.vega();
        double theta = this.theta();
        double rho = this.rho();
        return new AnalyticalCalculation(price, delta, gamma, vega, theta, rho);
    }
}
