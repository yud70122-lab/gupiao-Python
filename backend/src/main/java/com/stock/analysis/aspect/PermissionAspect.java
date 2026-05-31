package com.stock.analysis.aspect;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.service.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(com.stock.analysis.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission annotation = signature.getMethod().getAnnotation(RequirePermission.class);
        String requiredPermission = annotation.value();

        if (!UserContext.hasPermission(requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "权限不足，需要: " + requiredPermission));
        }

        return joinPoint.proceed();
    }
}