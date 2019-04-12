package org.lenskit.mooc.hybrid;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;

import java.io.Serializable;

/**
 * Model class for logistic regression, used to build models.
 */
@Shareable
@DefaultProvider(LogisticModelProvider.class)
public class LogisticModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double intercept;
    private final RealVector coefficients;

    /**
     * Create a logistic model.
     * @param icept The intercept.
     * @param coef The vector of coefficients.
     */
    public LogisticModel(double icept, RealVector coef) {
        intercept = icept;
        coefficients = coef;
    }

    /**
     * Create a logistic model from coefficient values.
     * @param icept The intercept.
     * @param coef The coefficient values.
     * @return A logistic model with the specified intercept and coefficients.
     */
    public static LogisticModel create(double icept, double... coef) {
        return new LogisticModel(icept, new ArrayRealVector(coef));
    }

    /**
     * Evaluate the logistic linear model with specified inputs.  It estimates the probability of obtaining the
     * specified value - &plusmn;1 - given the current model and specified inputs.
     * @param y The predicted value - 1 for {@code true} or -1 for {@code false}.
     * @param vars The explanatory variable values.
     * @return The probability of specified dependent value given the explanatory variable values.
     */
    public double evaluate(double y, RealVector vars) {
        return sigmoid(y * (intercept + coefficients.dotProduct(vars)));
    }

    /**
     * Evaluate the logistic linear model with specified inputs.  It estimates the probability of obtaining the
     * specified value - &plusmn;1 - given the current model and specified inputs.
     * @param y The predicted value - 1 for {@code true} or -1 for {@code false}.
     * @param vars The explanatory variable values.
     * @return The probability of specified dependent value given the explanatory variable values.
     */
    public double evaluate(double y, double... vars) {
        return evaluate(y, new ArrayRealVector(vars, false));
    }

    public double getIntercept() {
        return intercept;
    }

    public RealVector getCoefficients() {
        return coefficients;
    }

    @Override
    public String toString() {
        return "LogisticModel{" +
                "intercept=" + intercept +
                ", coefficients=" + coefficients +
                '}';
    }

    public static double sigmoid(double t) {
        return 1 / (1 + Math.exp(-t));
    }
}
