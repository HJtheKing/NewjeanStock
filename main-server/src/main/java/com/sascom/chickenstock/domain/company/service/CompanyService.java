package com.sascom.chickenstock.domain.company.service;

import com.sascom.chickenstock.domain.company.dto.response.CompanyInfoResponse;
import com.sascom.chickenstock.domain.company.entity.Company;
import com.sascom.chickenstock.domain.company.error.code.CompanyErrorCode;
import com.sascom.chickenstock.domain.company.error.exception.CompanyNotFoundException;
import com.sascom.chickenstock.domain.company.repository.CompanyRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @PostConstruct
    public void test() {
        List<Company> list = companyRepository.findAll();
        for(Company company : list) {
            log.info("companyName : {}, companyCode : {}", company.getName(), company.getCode());
        }
    }

//    @PostConstruct
//    public void init() {
//        Company company1 = new Company("삼성전자", "005930");
//        Company company2 = new Company("삼성전기", "009150");
//        Company company3 = new Company("SK하이닉스", "000660");
//        Company company4 = new Company("셀리드", "299660");
//        Company company5 = new Company("한미반도체", "042700");
//        Company company6 = new Company("NAVER", "035420");
//        Company company7 = new Company("카카오", "035720");
//        Company company8 = new Company("HLB", "028300");
//        Company company9 = new Company("랩지노믹스", "084650");
//        Company company10 = new Company("실리콘투", "257720");
//        companyRepository.save(company1);
//        companyRepository.save(company2);
//        companyRepository.save(company3);
//        companyRepository.save(company4);
//        companyRepository.save(company5);
//        companyRepository.save(company6);
//        companyRepository.save(company7);
//        companyRepository.save(company8);
//        companyRepository.save(company9);
//        companyRepository.save(company10);
//    }

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyInfoResponse getCompanyInfo(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));
        return CompanyInfoResponse.builder()
                .id(company.getId())
                .code(company.getCode())
                .name(company.getName())
                .status(company.getStatus())
                .build();
    }

    public List<CompanyInfoResponse> searchCompany(String stockName) {
        List<Company> companyList = companyRepository.findByNameContains(stockName);
        return collectToCompanyInfoResponse(companyList);
    }

    public List<CompanyInfoResponse> getCompanyInfoList() {
        List<Company> companyList = companyRepository.findAll();
        return collectToCompanyInfoResponse(companyList);
    }

    private List<CompanyInfoResponse> collectToCompanyInfoResponse(List<Company> companyList) {
        return companyList.stream().map(company ->
                        CompanyInfoResponse.builder()
                                .id(company.getId()).code(company.getCode())
                                .name(company.getName()).status(company.getStatus()).build())
                .toList();
    }

    public Company findById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() ->  CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));
    }

    public Long getCompanyIdByCode(String companyCode) {
        Company company =  companyRepository.findByCode(companyCode)
                .orElseThrow(() -> CompanyNotFoundException.of(CompanyErrorCode.NOT_FOUND));
        return company.getId();
    }
}
