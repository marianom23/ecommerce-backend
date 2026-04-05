package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.SystemSetting;
import java.util.Optional;

public interface SystemSettingRepository extends BaseRepository<SystemSetting, Long> {
    Optional<SystemSetting> findByKey(String key);
}
