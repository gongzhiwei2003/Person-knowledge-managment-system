package com.learn.knowledgesystem.repository;

import com.learn.knowledgesystem.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    // 根据关键词搜索（标题或内容包含关键词）
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    List<Note> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    // 根据标签ID查找所有包含该标签的笔记
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.id = :tagId")
    List<Note> findNotesByTagId(@Param("tagId") Long tagId);

    Optional<Note> findByShareId(String shareId);

    //对于列表页
    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.user.id = :userId")
    List<Note> findByUserIdWithTags(@Param("userId") Long userId);

    //对于单个笔记
    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.id = :id")
    Optional<Note> findByIdWithTags(@Param("id") Long id);

    //通过用户id和标签id查找笔记
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.user.id = :userId AND t.id = :tagId")
    List<Note> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

}

