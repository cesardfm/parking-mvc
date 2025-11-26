package com.g3.parking.repository;

import com.g3.parking.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    // Buscar por posición específica
    @Query(
        value = "SELECT * FROM sites s WHERE s.pos_x = :posX AND s.pos_y = :posY LIMIT 1",
        nativeQuery = true
    )
    Optional<Site> findByPosXAndPosY(@Param("posX") Integer posX, @Param("posY") Integer posY);

    // Buscar por status
    @Query(
        value = "SELECT * FROM sites s WHERE s.status = :status",
        nativeQuery = true
    )
    List<Site> findByStatus(@Param("status") String status);

    // Buscar por status y parking (join con levels)
    @Query(
        value = """
                SELECT s.* 
                FROM sites s
                JOIN levels l ON s.level_id = l.id
                WHERE s.status = :status AND l.parking_id = :parkingId
                """,
        nativeQuery = true
    )
    List<Site> findByStatusAndLevelParkingId(
            @Param("status") String status,
            @Param("parkingId") Long parkingId
    );

    // Buscar sitios por nivel
    @Query(
        value = "SELECT * FROM sites s WHERE s.level_id = :levelId",
        nativeQuery = true
    )
    List<Site> findByLevelId(@Param("levelId") Long levelId);
}
