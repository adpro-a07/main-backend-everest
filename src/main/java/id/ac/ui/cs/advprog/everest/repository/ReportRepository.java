package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Report;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReportRepository {
    private final List<Report> reportData = new ArrayList<>();
    private Long nextId = 1L;

    public List<Report> findAll() {
        return new ArrayList<>(reportData);
    }

    public Optional<Report> findById(Long id) {
        return reportData.stream()
                .filter(report -> report.getId().equals(id))
                .findFirst();
    }

    public Report save(Report report) {
        if (report.getId() == null) {
            report.setId(nextId++);
            reportData.add(report);
        } else {
            // Update existing report
            for (int i = 0; i < reportData.size(); i++) {
                if (reportData.get(i).getId().equals(report.getId())) {
                    reportData.set(i, report);
                    break;
                }
            }
        }
        return report;
    }

    public void deleteById(Long id) {
        reportData.removeIf(report -> report.getId().equals(id));
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

    public List<Report> findByStatusIgnoreCase(String status) {
        if (status == null) {
            return new ArrayList<>();
        }

        String searchTerm = status.toLowerCase();
        return reportData.stream()
                .filter(report -> report.getStatus().toLowerCase().equals(searchTerm))
                .collect(Collectors.toList());
    }

    public List<Report> findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase(
            String technicianName, String status) {
        if (technicianName == null || status == null) {
            return new ArrayList<>();
        }

        String nameSearchTerm = technicianName.toLowerCase();
        String statusSearchTerm = status.toLowerCase();

        return reportData.stream()
                .filter(report ->
                        report.getTechnicianName().toLowerCase().contains(nameSearchTerm) &&
                                report.getStatus().toLowerCase().equals(statusSearchTerm))
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