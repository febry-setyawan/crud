package com.example.crud.aop;

import com.example.crud.common.model.Auditable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditTrailAspect {

    /**
     * Pointcut ini akan menargetkan semua metode save() yang menerima satu argumen (entity)
     * di dalam kelas mana pun yang mengimplementasikan GenericRepository.
     */
    @Before("execution(* com.example.crud.common.repository.GenericRepository.save(..)) && args(entity)")
    public void beforeSave(JoinPoint joinPoint, Object entity) {
        if (entity instanceof Auditable auditableEntity) {
            String currentUser = getCurrentUsername();
            LocalDateTime now = LocalDateTime.now();
            
            auditableEntity.setCreatedAt(now);
            auditableEntity.setCreatedBy(currentUser);
            auditableEntity.setUpdatedAt(now); // Saat create, updated = created
            auditableEntity.setUpdatedBy(currentUser);
        }
    }

    /**
     * Pointcut ini menargetkan metode update().
     */
    @Before("execution(* com.example.crud.common.repository.GenericRepository.update(..)) && args(entity)")
    public void beforeUpdate(JoinPoint joinPoint, Object entity) {
        if (entity instanceof Auditable auditableEntity) {
            String currentUser = getCurrentUsername();
            LocalDateTime now = LocalDateTime.now();

            auditableEntity.setUpdatedAt(now);
            auditableEntity.setUpdatedBy(currentUser);
        }
    }

    /**
     * Mengambil username dari konteks keamanan Spring Security.
     * Untuk tujuan demo, kita akan hardcode jika tidak ada konteks keamanan.
     * @return Nama pengguna saat ini atau "SYSTEM" jika tidak ditemukan.
     */
    private String getCurrentUsername() {
        // --- Implementasi Real dengan Spring Security ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "SYSTEM";
        }
        return authentication.getName();
    }
}