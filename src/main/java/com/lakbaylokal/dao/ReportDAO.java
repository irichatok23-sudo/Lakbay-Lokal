package com.lakbaylokal.dao;

import com.lakbaylokal.model.ArrivalRecord;

import java.util.List;
import java.util.Map;

public interface ReportDAO {
    List<Map<String, Object>> getDestinationFootfallSummaries();
    List<Map<String, Object>> getDestinationFootfallSummaries(Integer municipalityId);
    List<Map<String, Object>> getStakeholderSummaries();
    List<Map<String, Object>> getStakeholderSummaries(Integer municipalityId);
    List<Map<String, Object>> getBarangaySummaries();
    List<Map<String, Object>> getBarangaySummaries(Integer municipalityId);
    List<ArrivalRecord> getArrivalLogs(Integer destinationId, String startDate, String endDate);
    int recordArrival(ArrivalRecord record);
    Map<String, Object> getOverviewKPIs();
    Map<String, Object> getOverviewKPIs(Integer municipalityId);
}
