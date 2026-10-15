package com.safesphere.backend.frontend;

import com.safesphere.backend.account.UserAccountService;
import com.safesphere.backend.admin.AdminService;
import com.safesphere.backend.analytics.AnalyticsService;
import com.safesphere.backend.incident.IncidentReport;
import com.safesphere.backend.incident.IncidentService;
import com.safesphere.backend.risk.RiskScoreService;
import com.safesphere.backend.safezone.SafeZone;
import com.safesphere.backend.safezone.SafeZoneService;
import com.safesphere.backend.account.AccountRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Controller
public class DashboardController {

    private final IncidentService incidentService;
    private final AdminService adminService;
    private final SafeZoneService safeZoneService;
    private final RiskScoreService riskScoreService;
    private final AnalyticsService analyticsService;
    private final UserAccountService accounts;

    public DashboardController(
            IncidentService incidentService,
            AdminService adminService,
            SafeZoneService safeZoneService,
            RiskScoreService riskScoreService,
            AnalyticsService analyticsService,
            UserAccountService accounts) {
        this.incidentService = incidentService;
        this.adminService = adminService;
        this.safeZoneService = safeZoneService;
        this.riskScoreService = riskScoreService;
        this.analyticsService = analyticsService;
        this.accounts = accounts;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + AccountRole.ADMIN.name()));
        return administrator ? "redirect:/admin/dashboard" : "redirect:/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String category) {
        if (authentication == null) {
            return "redirect:/login";
        }
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + AccountRole.ADMIN.name()));
        if (!administrator) {
            return "redirect:/dashboard";
        }

        Map<String, Object> analytics = analyticsService.getDashboardSummary();
        List<IncidentReport> allIncidents = incidentService.getAllIncidents();
        List<IncidentReport> allPendingIncidents = adminService.getPendingIncidents();
        String searchTerm = search.trim().toLowerCase(Locale.ROOT);
        List<IncidentReport> pendingIncidents = allPendingIncidents.stream()
            .filter(incident -> category.isBlank() || category.equalsIgnoreCase(incident.getCategory()))
            .filter(incident -> searchTerm.isBlank() || List.of(
                    Objects.toString(incident.getCategory(), ""),
                    Objects.toString(incident.getLocation(), ""),
                    Objects.toString(incident.getDescription(), ""))
                .stream()
                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(searchTerm)))
            .toList();
        model.addAttribute("safeZones", safeZoneService.getAllActive());
        model.addAttribute("riskScores", riskScoreService.getAllRiskScores());
        model.addAttribute("pendingIncidents", pendingIncidents);
        model.addAttribute("dashboardAnalytics", analytics);
        model.addAttribute("incidentCount", analytics.get("totalIncidents"));
        model.addAttribute("pendingCount", pendingIncidents.size());
        model.addAttribute("reviewedCount", Math.max(
                0,
            ((Number) analytics.get("totalIncidents")).intValue() - allPendingIncidents.size()));
        model.addAttribute("incidentCategories", allIncidents.stream()
            .map(IncidentReport::getCategory)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList());
        model.addAttribute("incidentSearch", search);
        model.addAttribute("selectedCategory", category);
        return "index";
    }

    @PostMapping("/reports")
    public String submitReport(
            Authentication authentication,
            @RequestParam String category,
            @RequestParam(required = false) String location,
            @RequestParam String description,
            @RequestParam(defaultValue = "true") boolean anonymous,
            RedirectAttributes redirectAttributes) {
        IncidentReport report = new IncidentReport();
        report.setCategory(category);
        report.setLocation(location);
        report.setDescription(description);
        report.setAnonymous(anonymous);
        report.setOwner(accounts.requireByEmail(authentication.getName()));
        report.setReportedBy(anonymous ? null : authentication.getName());
        IncidentReport saved = incidentService.submitIncident(report);
        redirectAttributes.addFlashAttribute(
                "message",
                "Incident #" + saved.getId() + " was saved and sent for automated analysis.");
        return "redirect:/#view-report";
    }

    @PostMapping("/safe-zones")
    public String createSafeZone(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            RedirectAttributes redirectAttributes) {
        SafeZone zone = new SafeZone();
        zone.setName(name);
        zone.setType(type);
        zone.setAddress(address);
        zone.setContactNumber(contactNumber);
        zone.setLatitude(latitude);
        zone.setLongitude(longitude);
        SafeZone saved = safeZoneService.addSafeZone(zone);
        redirectAttributes.addFlashAttribute("message", "Safe zone \"" + saved.getName() + "\" was saved.");
        return "redirect:/#view-zones";
    }

    @PostMapping("/risk-scores")
    public String calculateRisk(
            @RequestParam String areaName,
            @RequestParam double latitude,
            @RequestParam double longitude,
            RedirectAttributes redirectAttributes) {
        var score = riskScoreService.calculateRiskForArea(areaName, latitude, longitude, 0.05);
        redirectAttributes.addFlashAttribute(
                "message",
                "Risk estimate for " + score.getAreaName() + ": " + score.getRiskLevel()
                        + " (" + score.getScore() + "/10). This is not a safety guarantee.");
        return "redirect:/#view-heatmap";
    }

    @PostMapping("/incidents/status")
    public String updateIncidentStatus(
            @RequestParam Long id,
            @RequestParam IncidentReport.IncidentStatus status,
            RedirectAttributes redirectAttributes) {
        incidentService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("message", "Incident #" + id + " updated to " + status + ".");
        return "redirect:/#view-admin";
    }
}
