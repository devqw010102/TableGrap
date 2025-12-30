package com.example.demo.data.repository;

import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.enums.DinerStatus;
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
   // Page<Diner> findByCategory(Pageable pageable, String category);
    Page<Diner> findByCategoryAndStatusNot(Pageable pageable, String category, DinerStatus status);


    Optional<Diner> findById(Long id);
    //Optional<Diner> findById(Long id, DinerStatus status);

    //공백을 제거하고 식당이름 가져오기
    @Query("SELECT d FROM Diner d WHERE REPLACE(d.dinerName, ' ', '') = :dinerName AND d.status <> :status")
    Optional<Diner> findByDinerNameIgnoreSpaceStatusNot(@Param("dinerName") String dinerName, @Param("status") DinerStatus status);

  /*  @Query("""
        select new com.example.demo.data.dto.owner.OwnerDinerDto(
            d.id,
            d.dinerName,
            d.status
        )
        from Diner d
        where d.owner.id = :ownerId
    """)
    List<OwnerDinerDto> findByOwnerId(@Param("ownerId") Long ownerId); */

    @Query("""
        select new com.example.demo.data.dto.owner.OwnerDinerDto(
            d.id,
            d.dinerName,
            d.status
        )
        from Diner d
        where d.owner.id = :ownerId
        And d.status <> :status
    """)
    List<OwnerDinerDto> findByOwnerId(@Param("ownerId") Long ownerId, @Param("status") DinerStatus status);


  /*  @Query("""
        select d
        from Diner d
        join fetch d.owner m
        join fetch m.authorities a
        where a.authority = 'ROLE_OWNER'
    """)
    Page<Diner> findOwnerDiners(Pageable pageable); */


    @Query("""
        select d
        from Diner d
        join fetch d.owner m
        join fetch m.authorities a
        where a.authority = 'ROLE_OWNER'
        And d.status <> :status
    """)
    Page<Diner> findOwnerDiners(Pageable pageable, @Param("status") DinerStatus status);


    /*@Query("select count(d) from Diner d")
    Long countAllDiners(); */


    @Query("select count(d) from Diner d where d.status <> 'DELETED'")
    Long countAllDiners();

    List<Diner> findAllByOwner(Owner owner);
    //List<Diner> findAllByOwnerAndStatusNot(Owner owner, DinerStatus status);

    @Query("""
        SELECT new com.example.demo.data.dto.owner.OwnerDinerDto(d.id, d.dinerName, d.status)
        FROM Diner d
        WHERE d.id = :dinerId AND d.owner.id = :ownerId And d.status <> :status
    """)
    Optional<OwnerDinerDto> findDinerByOwner(
            @Param("dinerId") Long dinerId,
            @Param("ownerId") Long ownerId,
            @Param("status") DinerStatus status
    );

    /* @Query("""
        SELECT new com.example.demo.data.dto.owner.OwnerDinerDto(d.id, d.dinerName, d.status)
        FROM Diner d
        WHERE d.id = :dinerId AND d.owner.id = :ownerId
        And d.status <> :status
    """)
    Optional<OwnerDinerDto> findDinerByOwner(
            @Param("dinerId") Long dinerId,
            @Param("ownerId") Long ownerId
            @Param("status") DinerStatus status
    );*/

    //식당 삭제할 때 사용
    Optional<Diner> findByIdAndOwnerId(Long dinerId, Long ownerId);

    //식당 중복 업로드 방지
    boolean existsByDinerNameAndLocation(String dinerName, String location);
}
