package com.g3.parking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.model.Level;
import com.g3.parking.repository.LevelRepository;

@Service
public class LevelService extends BaseService {
    @Autowired
    private LevelRepository levelRepo;

    public List<LevelDTO> findByParkingId(Long parkingId) {
        List<Level> levels = levelRepo.findByParkingId(parkingId);
        return levels.stream()
                .map(level -> convert(level, LevelDTO.class))
                .collect(Collectors.toList());
    }

    public LevelDTO create(LevelDTO levelDto){
        Level level = convert(levelDto, Level.class);
        return convert(levelRepo.save(level), LevelDTO.class);
    }

    public LevelDTO findById(Long id){
        return convert(levelRepo.findById(id), LevelDTO.class);
    }

    public void delete(LevelDTO levelDTO){
        levelRepo.deleteById(levelDTO.getId());
    }
}
