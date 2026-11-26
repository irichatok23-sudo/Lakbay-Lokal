package com.lakbaylokal.service;

import com.lakbaylokal.dao.ReportDAO;
import com.lakbaylokal.dao.impl.ReportDAOImpl;
import com.lakbaylokal.model.ArrivalRecord;

import java.util.List;
import java.util.Map;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAOImpl();

    public Map<String, Object> getOverviewKPIs() {
        return reportDAO.getOverviewKPIs();
    }

    public Map<String, Object> getOverviewKPIs(Integer municipalityId) {
        return reportDAO.getOverviewKPIs(municipalityId);
    }

    public List<Map<String, Object>> getDestinationFootfallSummaries() {
        return reportDAO.getDestinationFootfallSummaries();
    }

    public List<Map<String, Object>> getDestinationFootfallSummaries(Integer municipalityId) {
        return reportDAO.getDestinationFootfallSummaries(municipalityId);
    }

    public List<Map<String, Object>> getStakeholderSummaries() {
        return reportDAO.getStakeholderSummaries();
    }

    public List<Map<String, Object>> getStakeholderSummaries(Integer municipalityId) {
        return reportDAO.getStakeholderSummaries(municipalityId);
    }

    public List<Map<String, Object>> getBarangaySummaries() {
        return reportDAO.getBarangaySummaries();
    }

    public List<Map<String, Object>> getBarangaySummaries(Integer municipalityId) {
        return reportDAO.getBarangaySummaries(municipalityId);
    }

    public List<ArrivalRecord> getArrivalLogs(Integer destinationId, String startDate, String endDate) {
        return reportDAO.getArrivalLogs(destinationId, startDate, endDate);
    }

    public int recordArrival(ArrivalRecord record) {
        return reportDAO.recordArrival(record);
    }
}
