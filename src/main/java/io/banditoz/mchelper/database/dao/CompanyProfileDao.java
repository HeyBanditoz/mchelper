package io.banditoz.mchelper.database.dao;

import java.util.Optional;

import io.banditoz.mchelper.investing.model.CompanyProfile;

public interface CompanyProfileDao {
    void addCompanyProfileIfNotExists(CompanyProfile companyProfile);
    Optional<CompanyProfile> getCompanyProfile(String ticker);
}
