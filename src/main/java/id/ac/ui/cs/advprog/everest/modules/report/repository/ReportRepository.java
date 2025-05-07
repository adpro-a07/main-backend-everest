package id.ac.ui.cs.advprog.everest.modules.report.repository;


import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReportRepository {
    private final List<Report> reportData = new ArrayList<>();
    private int nextId = 1;

    public List<Report> findAll() {
        return new ArrayList<>(reportData);
    }

    public Optional<Report> findById(int id) {
        return reportData.stream()
                .filter(report -> report.getId() == id)
                .findFirst();
    }

    public Report save(Report report) {
        if (report.getId() == 0) {
            report.setId(Math.toIntExact(nextId++));
            reportData.add(report);
        } else {
            // Update existing report
            for (int i = 0; i < reportData.size(); i++) {
                if (reportData.get(i).getId() == report.getId()) {
                    reportData.set(i, report);
                    break;
                }
            }
        }
        return report;
    }

    public void deleteById(int id) {
        reportData.removeIf(report -> report.getId() == id);
    }

    public List<Report> findByTechnicianNameContainingIgnoreCase(String technicianName) {
        if (technicianName == null) {
            return new ArrayList<>();
        }

        String searchTerm = technicianName.toLowerCase();
        return reportData.stream()
                .filter(report ->
                        report.getTechnicianName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    public List<Report> findByStatus(ReportStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        return reportData.stream()
                .filter(report -> report.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Report> findByTechnicianNameContainingIgnoreCaseAndStatus(
            String technicianName, ReportStatus status) {
        if (technicianName == null || status == null) {
            return new ArrayList<>();
        }
        String nameLower = technicianName.toLowerCase();
        return reportData.stream()
                .filter(report -> report.getTechnicianName().toLowerCase().contains(nameLower)
                        && report.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Report> findByRepairDate(LocalDate date) {
        return reportData.stream()
                .filter(report -> report.getRepairDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Report> findByRepairDateBetween(LocalDate startDate, LocalDate endDate) {
        return reportData.stream()
                .filter(report ->
                        !report.getRepairDate().isBefore(startDate) &&
                                !report.getRepairDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
}