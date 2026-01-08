package com.ecmsp.statistics_service.api.grpc;

import com.ecmsp.statistics.v1.*;
import com.ecmsp.statistics_service.dto.*;
import com.google.type.Decimal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class StatisticsGrpcMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public GetAvailableVariantsResponse toGetAvailableVariantsResponse(List<VariantInfoDTO> variants) {
        List<VariantInfo> variantInfos = variants.stream()
                .map(this::toVariantInfo)
                .collect(Collectors.toList());

        return GetAvailableVariantsResponse.newBuilder()
                .addAllVariants(variantInfos)
                .build();
    }

    public GetVariantSalesOverTimeResponse toGetVariantSalesOverTimeResponse(VariantSalesOverTimeDTO dto) {
        VariantSalesOverTime salesData = toVariantSalesOverTime(dto);

        return GetVariantSalesOverTimeResponse.newBuilder()
                .setSalesData(salesData)
                .build();
    }

    public GetStockLevelOverTimeResponse toGetStockLevelOverTimeResponse(StockLevelOverTimeDTO dto) {
        StockLevelOverTime stockData = toStockLevelOverTime(dto);

        return GetStockLevelOverTimeResponse.newBuilder()
                .setStockData(stockData)
                .build();
    }

    private VariantInfo toVariantInfo(VariantInfoDTO dto) {
        VariantInfo.Builder builder = VariantInfo.newBuilder()
                .setVariantId(dto.getVariantId().toString())
                .setProductId(dto.getProductId().toString())
                .setProductName(dto.getProductName())
                .setHasSalesData(dto.isHasSalesData())
                .setHasStockData(dto.isHasStockData());

        if (dto.getLastSaleDate() != null) {
            builder.setLastSaleDate(dto.getLastSaleDate().format(DATETIME_FORMATTER));
        }

        if (dto.getCurrentStock() != null) {
            builder.setCurrentStock(dto.getCurrentStock());
        }

        return builder.build();
    }

    private VariantSalesOverTime toVariantSalesOverTime(VariantSalesOverTimeDTO dto) {
        VariantSalesOverTime.Builder builder = VariantSalesOverTime.newBuilder()
                .setVariantId(dto.getVariantId().toString());

        if (dto.getProductName() != null) {
            builder.setProductName(dto.getProductName());
        }

        List<SalesDataPoint> dataPoints = dto.getDataPoints().stream()
                .map(this::toSalesDataPoint)
                .collect(Collectors.toList());
        builder.addAllDataPoints(dataPoints);

        List<LinearRegressionLine> regressionLines = dto.getRegressionLines().stream()
                .map(this::toLinearRegressionLine)
                .collect(Collectors.toList());
        builder.addAllRegressionLines(regressionLines);

        return builder.build();
    }

    private StockLevelOverTime toStockLevelOverTime(StockLevelOverTimeDTO dto) {
        StockLevelOverTime.Builder builder = StockLevelOverTime.newBuilder()
                .setVariantId(dto.getVariantId().toString());

        if (dto.getProductName() != null) {
            builder.setProductName(dto.getProductName());
        }

        List<StockDataPoint> dataPoints = dto.getDataPoints().stream()
                .map(this::toStockDataPoint)
                .collect(Collectors.toList());
        builder.addAllDataPoints(dataPoints);

        List<LinearRegressionLine> regressionLines = dto.getRegressionLines().stream()
                .map(this::toLinearRegressionLine)
                .collect(Collectors.toList());
        builder.addAllRegressionLines(regressionLines);

        if (dto.getTrendLine() != null) {
            builder.setTrendLine(toLinearRegressionLine(dto.getTrendLine()));
        }

        return builder.build();
    }

    private SalesDataPoint toSalesDataPoint(SalesDataPointDTO dto) {
        return SalesDataPoint.newBuilder()
                .setDate(dto.getDate().format(DATETIME_FORMATTER))
                .setQuantity(dto.getQuantity())
                .setTotalRevenue(toDecimal(dto.getTotalRevenue()))
                .build();
    }

    private StockDataPoint toStockDataPoint(StockDataPointDTO dto) {
        return StockDataPoint.newBuilder()
                .setDate(dto.getDate().format(DATETIME_FORMATTER))
                .setStockLevel(dto.getStockLevel())
                .build();
    }

    private LinearRegressionLine toLinearRegressionLine(LinearRegressionLineDTO dto) {
        LinearRegressionLine.Builder builder = LinearRegressionLine.newBuilder()
                .setSlope(dto.getSlope())
                .setIntercept(dto.getIntercept())
                .setRSquared(dto.getRSquared());

        if (dto.getValidFrom() != null) {
            builder.setValidFrom(dto.getValidFrom().format(DATETIME_FORMATTER));
        }

        if (dto.getValidTo() != null) {
            builder.setValidTo(dto.getValidTo().format(DATETIME_FORMATTER));
        }

        if (dto.getEstimatedDepletionDate() != null) {
            builder.setEstimatedDepletionDate(dto.getEstimatedDepletionDate().format(DATETIME_FORMATTER));
        }

        return builder.build();
    }

    private Decimal toDecimal(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return Decimal.newBuilder().setValue("0").build();
        }
        return Decimal.newBuilder()
                .setValue(bigDecimal.toPlainString())
                .build();
    }

    public UUID parseUuid(String uuidString) {
        return UUID.fromString(uuidString);
    }

    public LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
}
