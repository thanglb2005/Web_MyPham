package vn.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId")
    List<ChatMessage> findLatestByRoomId(@Param("roomId") String roomId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    long deleteByRoomId(String roomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 GROUP BY m2.roomId)")
    List<ChatMessage> findLatestPerRoom(Pageable pageable);

    @Query("SELECT m.customerName FROM ChatMessage m WHERE m.roomId = :roomId AND m.customerName IS NOT NULL ORDER BY m.sentAt DESC")
    List<String> findLatestCustomerName(@Param("roomId") String roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.customerName = :customerName AND m.roomId LIKE :roomIdPattern ORDER BY m.sentAt DESC")
    List<ChatMessage> findByCustomerNameAndRoomIdStartingWith(@Param("customerName") String customerName, @Param("roomIdPattern") String roomIdPattern);

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId LIKE :roomIdPattern ORDER BY m.sentAt DESC")
    List<ChatMessage> findByRoomIdStartingWith(@Param("roomIdPattern") String roomIdPattern);
}




