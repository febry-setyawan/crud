package com.example.crud.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AuditTrailAspect.class);

    /**
     * Pointcut ini akan menargetkan semua metode save() yang menerima satu argumen
     * (entity)
     * di dalam kelas mana pun yang mengimplementasikan GenericRepository.
     */
    @Before("execution(* com.example.crud.common.repository.GenericRepository.save(..)) && args(entity)")
    public void beforeSave(JoinPoint joinPoint, Object entity) {
        if (entity instanceof Auditable auditableEntity) {
            String currentUser = getCurrentUsername();
            LocalDateTime now = LocalDateTime.now();
            logger.debug("[AuditTrailAspect] beforeSave - currentUser: {}", currentUser);
            auditableEntity.setCreatedAt(now);
            auditableEntity.setCreatedBy(currentUser);
            auditableEntity.setUpdatedAt(now); // Saat create, updated = created
            auditableEntity.setUpdatedBy(currentUser);
        }
    }

    /**
     * Pointcut ini menargetkan metode update().
     */
    @org.aspectj.lang.annotation.Around("execution(* com.example.crud.common.repository.GenericRepository.update(..)) && args(entity)")
    public Object aroundUpdate(org.aspectj.lang.ProceedingJoinPoint pjp, Object entity) throws Throwable {
        if (entity instanceof Auditable auditableEntity) {
            String currentUser = getCurrentUsername();
            LocalDateTime now = LocalDateTime.now();
            logger.debug("[AuditTrailAspect] aroundUpdate - currentUser: {}", currentUser);
            auditableEntity.setUpdatedAt(now);
            auditableEntity.setUpdatedBy(currentUser);
        }
        return pjp.proceed();
    }

    /**
     * Mengambil username dari konteks keamanan Spring Security.
     * Untuk tujuan demo, kita akan hardcode jika tidak ada konteks keamanan.
     * 
     * @return Nama pengguna saat ini atau "SYSTEM" jika tidak ditemukan.
     */
    String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "SYSTEM";
        }
        return authentication.getName();
    }
}