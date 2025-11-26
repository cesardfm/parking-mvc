package com.g3.parking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.RoleDTO;
import com.g3.parking.repository.RoleRepository;

@Service
public class RoleService extends BaseService{
    @Autowired
    private RoleRepository roleRepo;

    public RoleDTO getByName(String nameRole){
        return convert(roleRepo.findByName(nameRole), RoleDTO.class);
    }
}
