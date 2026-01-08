package com.ecmsp.statistics_service.api.grpc;

import com.ecmsp.statistics.v1.*;
import com.ecmsp.statistics_service.dto.StockLevelOverTimeDTO;
import com.ecmsp.statistics_service.dto.VariantInfoDTO;
import com.ecmsp.statistics_service.dto.VariantSalesOverTimeDTO;
import com.ecmsp.statistics_service.service.StatisticsService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@GrpcService
class StatisticsGrpcService extends StatisticsServiceGrpc.StatisticsServiceImplBase {

    private final StatisticsService statisticsService;
    private final StatisticsGrpcMapper statisticsGrpcMapper;

    public StatisticsGrpcService(StatisticsService statisticsService, StatisticsGrpcMapper statisticsGrpcMapper) {
        this.statisticsService = statisticsService;
        this.statisticsGrpcMapper = statisticsGrpcMapper;
    }

    @Override
    public void getAvailableVariants(GetAvailableVariantsRequest request, StreamObserver<GetAvailableVariantsResponse> responseObserver) {
        try {
            List<VariantInfoDTO> variants = statisticsService.getAvailableVariants();
            GetAvailableVariantsResponse response = statisticsGrpcMapper.toGetAvailableVariantsResponse(variants);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getVariantSalesOverTime(GetVariantSalesOverTimeRequest request, StreamObserver<GetVariantSalesOverTimeResponse> responseObserver) {
        try {
            UUID variantId = statisticsGrpcMapper.parseUuid(request.getVariantId());
            LocalDate fromDate = statisticsGrpcMapper.parseDate(request.getFromDate());
            LocalDate toDate = statisticsGrpcMapper.parseDate(request.getToDate());
            Integer trendDays = request.getTrendDays() > 0 ? request.getTrendDays() : 30;

            VariantSalesOverTimeDTO salesData = statisticsService.getVariantSalesOverTime(
                    variantId, fromDate, toDate, trendDays
            );

            GetVariantSalesOverTimeResponse response = statisticsGrpcMapper.toGetVariantSalesOverTimeResponse(salesData);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getStockLevelOverTime(GetStockLevelOverTimeRequest request, StreamObserver<GetStockLevelOverTimeResponse> responseObserver) {
        try {
            UUID variantId = statisticsGrpcMapper.parseUuid(request.getVariantId());
            LocalDate fromDate = statisticsGrpcMapper.parseDate(request.getFromDate());
            LocalDate toDate = statisticsGrpcMapper.parseDate(request.getToDate());
            Integer trendDays = request.getTrendDays() > 0 ? request.getTrendDays() : 30;

            StockLevelOverTimeDTO stockData = statisticsService.getStockLevelOverTime(
                    variantId, fromDate, toDate, trendDays
            );

            GetStockLevelOverTimeResponse response = statisticsGrpcMapper.toGetStockLevelOverTimeResponse(stockData);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
