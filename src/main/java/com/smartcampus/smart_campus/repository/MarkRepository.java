package com.smartcampus.smart_campus.repository;

import com.smartcampus.smart_campus.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    Optional<Mark> findBySubjectIdAndStudentId(Long subjectId, Long studentId);

    List<Mark> findBySubjectId(Long subjectId);

    List<Mark> findByStudentId(Long studentId);

    @Query("select m.student.id, count(m), avg(m.internalMarks + m.externalMarks), "
            + "sum(case when (m.internalMarks + m.externalMarks) >= :passMark then 1 else 0 end) "
            + "from Mark m where m.student.id in :ids group by m.student.id")
    List<Object[]> totalsByStudent(@Param("ids") Collection<Long> ids,
                                   @Param("passMark") int passMark);

    @Query("select m.subject.id, m.subject.code, m.subject.name, count(m), "
            + "avg(m.internalMarks + m.externalMarks), "
            + "sum(case when (m.internalMarks + m.externalMarks) >= :passMark then 1 else 0 end) "
            + "from Mark m group by m.subject.id, m.subject.code, m.subject.name")
    List<Object[]> totalsBySubject(@Param("passMark") int passMark);

    @Query("select m.internalMarks + m.externalMarks from Mark m")
    List<Integer> allTotals();
}