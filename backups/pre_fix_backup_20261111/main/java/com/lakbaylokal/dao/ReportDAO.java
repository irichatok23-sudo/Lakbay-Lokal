package com.lakbaylokal.dao;

import com.lakbaylokal.model.ArrivalRecord;

import java.util.List;
import java.util.Map;

public interface ReportDAO {
    List<Map<String, Object>> getDestinationFootfallSummaries();
    List<Map<String, Object>> getStakeholderSummaries();
    List<Map<String, Object>> getBarangaySummaries();
    List<ArrivalRecord> getArrivalLogs(Integer destinationId, String startDate, String endDate);
    int recordArrival(ArrivalRecord record);
    Map<String, Object> getOverviewKPIs();
}
