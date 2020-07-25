package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.investing.model.CompanyProfile;

import java.util.Optional;

public interface CompanyProfileDao {
    void addCompanyProfileIfNotExists(CompanyProfile companyProfile);
    Optional<CompanyProfile> getCompanyProfile(String ticker);
}
