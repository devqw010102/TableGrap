package com.example.demo.data.repository;

import com.example.demo.data.dto.OwnerDinerDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
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

    @Query("""
        select new com.example.demo.data.dto.OwnerDinerDto(
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
}
