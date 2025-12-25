package com.example.demo.data.repository;

import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DinerRepository extends JpaRepository<Diner, Long> {
    Page<Diner> findByCategory(Pageable pageable, String category);
    Optional<Diner> findById(Long id);
    // Owner's diners 조회
    Optional<Diner> findByDinerName(String dinerName);
    //공백을 제거하고 식당이름 가져오기
    @Query("SELECT d FROM Diner d WHERE REPLACE(d.dinerName, ' ', '') = :dinerName")
    Optional<Diner> findByDinerNameIgnoreSpace(@Param("dinerName") String dinerName);


    @Query("""
        select new com.example.demo.data.dto.owner.OwnerDinerDto(
            d.id,
            d.dinerName
        )
        from Diner d
        where d.owner.id = :ownerId
    """)
    List<OwnerDinerDto> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        select d
        from Diner d
        join fetch d.owner m
        join fetch m.authorities a
        where a.authority = 'ROLE_OWNER'
    """)
    List<Diner> findOwnerDiners();

    @Query("select count(d) from Diner d")
    Long countAllDiners();

    List<Diner> findAllByOwner(Owner owner);

    @Query("""
        SELECT new com.example.demo.data.dto.owner.OwnerDinerDto(d.id, d.dinerName)
        FROM Diner d
        WHERE d.id = :dinerId AND d.owner.id = :ownerId
    """)
    Optional<OwnerDinerDto> findDinerByOwner(
            @Param("dinerId") Long dinerId,
            @Param("ownerId") Long ownerId
    );

    Optional<Diner> findByIdAndOwnerId(Long dinerId, Long ownerId);
}
