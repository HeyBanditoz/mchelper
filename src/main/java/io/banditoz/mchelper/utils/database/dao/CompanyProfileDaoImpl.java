package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.investing.model.CompanyProfile;
import io.banditoz.mchelper.utils.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CompanyProfileDaoImpl extends Dao implements CompanyProfileDao {
    private final Logger LOGGER = LoggerFactory.getLogger(CompanyProfileDaoImpl.class);

    public CompanyProfileDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void addCompanyProfileIfNotExists(CompanyProfile companyProfile) {
        try (Connection c = DATABASE.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO company_profiles VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")) {
                ps.setString(1, companyProfile.getCountry());
                ps.setString(2, companyProfile.getExchange());
                ps.setString(3, companyProfile.getFinnhubIndustry());
                ps.setDate(4, companyProfile.getIpo());
                ps.setString(5, companyProfile.getLogo());
                ps.setFloat(6, companyProfile.getMarketCapitalization());
                ps.setString(7, companyProfile.getName());
                ps.setFloat(8, (float) companyProfile.getShareOutstanding());
                ps.setString(9, companyProfile.getTicker());
                ps.setString(10, companyProfile.getWeburl());
                ps.execute();
            }
        } catch (SQLException ex) {
            LOGGER.warn("Could not cache company profile " + companyProfile.getTicker(), ex);
        }
    }

    @Override
    public Optional<CompanyProfile> getCompanyProfile(String ticker) {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM company_profiles WHERE ticker=?");
            ps.setString(1, ticker);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Optional<CompanyProfile> companyProfile = buildCompanyProfileFromResultSet(rs);
                if (companyProfile.isPresent()) {
                    if (companyProfile.get().isExpired()) {
                        deleteCompanyProfile(companyProfile.get().getTicker());
                        companyProfile = Optional.empty();
                    }
                    return companyProfile;
                }
            }
        } catch (SQLException ex) {
            LOGGER.warn("Could not fetch company profile " + ticker, ex);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private void deleteCompanyProfile(String ticker) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM company_profiles WHERE ticker LIKE ?");
            ps.setString(1, ticker);
            ps.execute();
        }
    }

    private Optional<CompanyProfile> buildCompanyProfileFromResultSet(ResultSet rs) throws SQLException {
        CompanyProfile companyProfile = new CompanyProfile();
        companyProfile.setCountry(rs.getString("country"));
        companyProfile.setExchange(rs.getString("exchange"));
        companyProfile.setFinnhubIndustry(rs.getString("industry"));
        companyProfile.setIpo(rs.getDate("ipo"));
        companyProfile.setMarketCapitalization(rs.getFloat("market_capitalization"));
        companyProfile.setName(rs.getString("name"));
        companyProfile.setShareOutstanding(rs.getFloat("shares_outstanding"));
        companyProfile.setTicker(rs.getString("ticker"));
        companyProfile.setWeburl(rs.getString("weburl"));
        companyProfile.setAddedWhen(rs.getTimestamp("updated"));
        return Optional.of(companyProfile);
    }
}
