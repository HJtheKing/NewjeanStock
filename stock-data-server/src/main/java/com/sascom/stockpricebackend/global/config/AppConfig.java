package com.sascom.stockpricebackend.global.config;

import com.sascom.stockpricebackend.application.company.CompanyInfo;
import com.sascom.stockpricebackend.application.company.CompanyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    public final CompanyRepository companyRepository;

    public AppConfig(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Bean
    public Map<String, String> companyCodeMap() {
        ConcurrentHashMap<String, String> companyCodeMap = new ConcurrentHashMap<>();
        List<CompanyInfo> companies = companyRepository.findAll();
        for(CompanyInfo company : companies) {
            companyCodeMap.put(company.name(), company.code());
        }
        return companyCodeMap;
    }
}
