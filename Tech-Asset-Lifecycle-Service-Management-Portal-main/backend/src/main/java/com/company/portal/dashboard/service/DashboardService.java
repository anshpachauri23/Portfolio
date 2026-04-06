package com.company.portal.dashboard.service;

import com.company.portal.dashboard.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final EntityManager em;

    public DashboardService(EntityManager em) {
        this.em = em;
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public DashboardSummary getSummary(Long managerUserId) {
        String requestJpql = "SELECT sr.status AS grp, COUNT(sr) AS cnt FROM ServiceRequest sr"
                + buildRequesterFilter(managerUserId)
                + " GROUP BY sr.status";
        Map<String, Long> requestCounts = countByGroup(requestJpql, managerUserId);

        Map<String, Long> assetCounts = countByGroup(
                "SELECT a.status AS grp, COUNT(a) AS cnt FROM Asset a GROUP BY a.status", null);

        return new DashboardSummary(requestCounts, assetCounts);
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<RequestTrend> getRequestTrends(Long managerUserId) {
        String sql;
        Query query;
        if (managerUserId != null) {
            sql = "SELECT TO_CHAR(sr.created_at, 'YYYY-MM') AS month, COUNT(*) AS cnt "
                    + "FROM service_requests sr "
                    + "WHERE sr.requester_id IN (SELECT u.id FROM users u WHERE u.manager_id = ?1) "
                    + "AND sr.created_at >= NOW() - INTERVAL '6 months' "
                    + "GROUP BY month ORDER BY month";
            query = em.createNativeQuery(sql);
            query.setParameter(1, managerUserId);
        } else {
            sql = "SELECT TO_CHAR(sr.created_at, 'YYYY-MM') AS month, COUNT(*) AS cnt "
                    + "FROM service_requests sr "
                    + "WHERE sr.created_at >= NOW() - INTERVAL '6 months' "
                    + "GROUP BY month ORDER BY month";
            query = em.createNativeQuery(sql);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(r -> new RequestTrend((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<StatusCount> getAssetsByStatus() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT a.status, COUNT(a) FROM Asset a GROUP BY a.status", Object[].class
        ).getResultList();

        return rows.stream()
                .map(r -> new StatusCount(r[0].toString(), ((Number) r[1]).longValue()))
                .toList();
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<SlaAgingBucket> getSlaAging(Long managerUserId) {
        String sql;
        Query query;

        String bucketCase = "CASE "
                + "WHEN EXTRACT(DAY FROM NOW() - sr.due_date) BETWEEN 0 AND 3 THEN '0-3 days' "
                + "WHEN EXTRACT(DAY FROM NOW() - sr.due_date) BETWEEN 4 AND 7 THEN '4-7 days' "
                + "ELSE '7+ days' END";

        if (managerUserId != null) {
            sql = "SELECT " + bucketCase + " AS bucket, COUNT(*) AS cnt "
                    + "FROM service_requests sr "
                    + "WHERE sr.requester_id IN (SELECT u.id FROM users u WHERE u.manager_id = ?1) "
                    + "AND sr.due_date IS NOT NULL "
                    + "AND sr.status NOT IN ('CLOSED', 'REJECTED', 'COMPLETED') "
                    + "AND sr.due_date < NOW() "
                    + "GROUP BY bucket ORDER BY bucket";
            query = em.createNativeQuery(sql);
            query.setParameter(1, managerUserId);
        } else {
            sql = "SELECT " + bucketCase + " AS bucket, COUNT(*) AS cnt "
                    + "FROM service_requests sr "
                    + "WHERE sr.due_date IS NOT NULL "
                    + "AND sr.status NOT IN ('CLOSED', 'REJECTED', 'COMPLETED') "
                    + "AND sr.due_date < NOW() "
                    + "GROUP BY bucket ORDER BY bucket";
            query = em.createNativeQuery(sql);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(r -> new SlaAgingBucket((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    private Map<String, Long> countByGroup(String jpql, Long managerUserId) {
        var query = em.createQuery(jpql, Tuple.class);
        if (managerUserId != null) {
            query.setParameter("managerId", managerUserId);
        }
        Map<String, Long> result = new LinkedHashMap<>();
        for (Tuple t : query.getResultList()) {
            result.put(t.get("grp").toString(), t.get("cnt", Long.class));
        }
        return result;
    }

    private String buildRequesterFilter(Long managerUserId) {
        if (managerUserId == null) return "";
        return " WHERE sr.requester.id IN (SELECT u.id FROM User u WHERE u.manager.id = :managerId)";
    }
}
