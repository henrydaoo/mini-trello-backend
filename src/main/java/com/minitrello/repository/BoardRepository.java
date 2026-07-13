package com.minitrello.repository;

import com.minitrello.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("select distinct b from Board b left join b.members m " +
            "where b.owner.id = :userId or m.user.id = :userId " +
            "order by b.createdAt desc")
    List<Board> findAllAccessibleByUser(@Param("userId") Long userId);
}
