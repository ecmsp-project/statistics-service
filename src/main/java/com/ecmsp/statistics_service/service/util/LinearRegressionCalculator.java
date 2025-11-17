package com.ecmsp.statistics_service.service.util;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LinearRegressionCalculator {

    @Data
    public static class DataPoint {
        private final LocalDateTime date;
        private final Double value;
    }

    @Data
    public static class RegressionResult {
        private final double slope;
        private final double intercept;
        private final double rSquared;
        private final LocalDateTime referenceDate;
    }

    public static RegressionResult calculateLinearRegression(List<DataPoint> dataPoints, LocalDateTime referenceDate) {
        if (dataPoints == null || dataPoints.size() < 2) {
            return null;
        }

        int n = dataPoints.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;

        for (DataPoint point : dataPoints) {
            long x = ChronoUnit.DAYS.between(referenceDate, point.getDate());
            double y = point.getValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        double yMean = sumY / n;
        double ssTotal = sumY2 - n * yMean * yMean;
        double ssResidual = 0;

        for (DataPoint point : dataPoints) {
            long x = ChronoUnit.DAYS.between(referenceDate, point.getDate());
            double predictedY = slope * x + intercept;
            double residual = point.getValue() - predictedY;
            ssResidual += residual * residual;
        }

        double rSquared = ssTotal != 0 ? 1 - (ssResidual / ssTotal) : 0;

        return new RegressionResult(slope, intercept, rSquared, referenceDate);
    }

    public static LocalDateTime estimateDepletionDate(RegressionResult regression) {
        if (regression == null || regression.getSlope() >= 0) {
            return null;
        }

        double daysToDepletion = -regression.getIntercept() / regression.getSlope();

        if (daysToDepletion < 0) {
            return null;
        }

        return regression.getReferenceDate().plusDays((long) Math.ceil(daysToDepletion));
    }

    public static LocalDateTime estimateZeroCrossingDate(RegressionResult regression) {
        if (regression == null) {
            return null;
        }

        if (Math.abs(regression.getSlope()) < 0.0001) {
            return null;
        }

        double daysToZero = -regression.getIntercept() / regression.getSlope();

        if (daysToZero < 0) {
            return null;
        }

        return regression.getReferenceDate().plusDays((long) Math.ceil(daysToZero));
    }
}
