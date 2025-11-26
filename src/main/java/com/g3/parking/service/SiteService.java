package com.g3.parking.service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.SiteDTO;
import com.g3.parking.model.Site;
import com.g3.parking.repository.SiteRepository;

@Service
public class SiteService extends BaseService {
    @Autowired
    private SiteRepository siteRepo;

    public SiteDTO findById(Long id) {
        Site site = siteRepo.getReferenceById(id);
        return convert(site, SiteDTO.class);
    }

    public SiteDTO create(SiteDTO siteDTO) {
        Site site = convert(siteDTO, Site.class);
        return convert(siteRepo.save(site), SiteDTO.class);
    }

    public void deleteAll(List<SiteDTO> sitesDtos) {
        List<Site> sites = sitesDtos.stream()
                .map(site -> convert(site, Site.class))
                .collect(Collectors.toList());
        siteRepo.deleteAll(sites);
    }

    public List<SiteDTO> findByStatusAndLevel_Parking_Id(String status, Long parkingId) {
        List<Site> sites = siteRepo.findByStatusAndLevel_Parking_Id(status, parkingId);
        return sites.stream()
                .map(site -> convert(site, SiteDTO.class))
                .collect(Collectors.toList());
    }

    public boolean changeStatus(Long id, String status){
        try {
            Site site = siteRepo.getReferenceById(id);
            site.setStatus(status);
            siteRepo.save(site);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
